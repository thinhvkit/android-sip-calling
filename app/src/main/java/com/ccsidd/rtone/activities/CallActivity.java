/* $Id: CallActivity.java 5138 2015-07-30 06:23:35Z ming $ */
/*
 * Copyright (C) 2013 Teluu Inc. (http://www.teluu.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.ccsidd.rtone.activities;

import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.percent.PercentFrameLayout;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.objects.CallEvent;
import com.ccsidd.rtone.objects.CallLog;
import com.ccsidd.rtone.objects.CallStateEvent;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.services.SipServiceCommand;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;
import com.innovattic.font.FontEditText;
import com.innovattic.font.TypefaceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmResults;

public class CallActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "BluetoothService";
    private static final String LOG_TAG = CallActivity.class.getSimpleName();
    private BroadcastReceiver callSecondReceiver;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private PercentFrameLayout layoutFunction;
    private PercentFrameLayout layoutControl;
    private PercentFrameLayout layoutInfo;
    private LinearLayout llDialPad;
    private TextView tvName;
    private TextView tvTime;
    private ImageView ivAvatar;
    private ToggleButton btnSpeaker;
    private ToggleButton btnMicro;
    private ToggleButton btnHold;
    private ToggleButton btnDial;
    private ToggleButton btnBluetooth;
    private ToggleButton btnAdd;
    private Button btnAccept;
    private Button btnReject;
    private Button btnHangup;
    //T
    private PercentFrameLayout layoutCallSub;
    private TextView tvSubName;
    private TextView tvSubPhone;
    private ImageView ivAvatarSub;
    private Button btnSubAccept;
    private Button btnSubReject;
    //V
    private FontEditText edPhoneNumber;
    private boolean isInCommunication = false;
    private MediaPlayer ringtonePlayer;
    private Vibrator vibrator;
    private Timer timer;
    private SimpleTimerTask simpleTimerTask;

    private boolean isPressing0 = false;
    private boolean isConference = false;
    private int mCallID;
    private String defaultAccount = "";
    private AudioManager am;
    private Timer blinkTimer;
    private SettingsContentObserver systemObserver;
    private HashMap<Integer, CallState> mCall;
    private TelephonyManager telephony;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHeadset mBluetoothHeadset;
    private PowerManager.WakeLock partialWakeLock;
    private PowerManager.WakeLock fullWakeLock;
    private PowerManager.WakeLock screenOffWakeLock;
    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d(TAG, "Connecting HeadsetService...");
                mBluetoothHeadset = (BluetoothHeadset) proxy;
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d(TAG, "Unexpected Disconnect of HeadsetService...");
                mBluetoothHeadset = null;
            }
        }
    };
    private BroadcastReceiver mProfileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED.equals(action)) {
                notifyAudioState(intent);
            }
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                notifyConnectState(intent);
            }
            if (BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT.equals(action)) {
                notifyATEvent(intent);
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final CallStateEvent event) {
        if (!mCall.containsKey(event.getCallID())) {
            return;
        }
        if (event.getCallStateCode().equals(pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED)) {
            Logger.debug(LOG_TAG, "Call ID disconnect :" + event.getCallID());
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    long id = 0;
                    RealmResults<CallLog> result = realm.where(CallLog.class).findAll();
                    if (result.size() > 0) {
                        id = result.where().max("id").longValue();
                    }
                    CallLog callLog = new CallLog();
                    callLog.setId((int) id + 1);
                    callLog.setDate(new Date().getTime());
                    callLog.setNumberLabel("VoIP");
                    callLog.setName("");
                    callLog.setNumber(Utility.formatNumberphone(mCall.get(event.getCallID()).phone));
                    callLog.setNumberType(0);
                    callLog.setDataUsage(event.getData());
                    if (mCall.get(event.getCallID()).isCalling)
                        callLog.setType(android.provider.CallLog.Calls.OUTGOING_TYPE);
                    else
                        callLog.setType(android.provider.CallLog.Calls.INCOMING_TYPE);
                    realm.copyToRealm(callLog);
//                    Intent syncCallLog = new Intent(GlobalVars.BROADCAST_ACTION_SYNCED_CALLLOG);
//                    sendBroadcast(syncCallLog);
                }
            });
            realm.close();
            mCall.remove(event.getCallID());

            stopRingTone();
            stopRingBack();

            if (mCall.size() == 0) {
//              am.setMode(AudioManager.MODE_NORMAL);
                stopBluetoothHeadset();
                Logger.debug(LOG_TAG, "Not have call");
                deInit();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnHangup.setEnabled(true);
                        Toast.makeText(CallActivity.this, "Call has stopped", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            for (final Integer iCallID : mCall.keySet()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        layoutControl.setVisibility(View.VISIBLE);
                        if (layoutCallSub.getVisibility() == View.VISIBLE) {
                            layoutCallSub.setVisibility(View.GONE);
                        }

                        if (mCall.get(iCallID).isReceive) {
                            simpleTimerTask.current.set((int) (System.currentTimeMillis() - mCall.get(iCallID).timeStamp) / 1000);
                            layoutFunction.setVisibility(View.VISIBLE);
                        } else {
                            if (!mCall.get(iCallID).isCalling) {
                                stopTimer();

                                tvTime.setText("Incoming...");

                                btnAccept.setEnabled(true);
                                btnReject.setEnabled(true);
                                btnAccept.setVisibility(View.VISIBLE);
                                btnReject.setVisibility(View.VISIBLE);
                                btnHangup.setVisibility(View.GONE);
                                layoutFunction.setVisibility(View.GONE);
                                playRingTone();
                            }
                        }
                    }
                });

                Contact contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(mCall.get(iCallID).phone));
                Utility.loadAvatar(this, ivAvatar, contact);
                tvName.setText(contact.getDisplayName());
                btnHold.setChecked(false);


                mCallID = iCallID;
                SipServiceCommand.setCallHold(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", iCallID, false);
            }
        }
        if (event.getLastStatusCode().equals(pjsip_status_code.PJSIP_SC_PROXY_AUTHENTICATION_REQUIRED)) {
            Logger.debug(LOG_TAG, "PJSIP_SC_PROXY_AUTHENTICATION_REQUIRED");
            deInit();
        }
        if (event.getCallStateCode().equals(pjsip_inv_state.PJSIP_INV_STATE_EARLY)) {
            if (event.getLastStatusCode().equals(pjsip_status_code.PJSIP_SC_PROGRESS))
                stopRingBack();
            else if (mCall != null && mCall.containsKey(event.getCallID()) && mCall.get(event.getCallID()).isCalling)
                playRingBack();
        } else {
            if (event.getCallStateCode().equals(pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED)) {
                mCallID = event.getCallID();
                mCall.get(event.getCallID()).isReceive = true;

                isInCommunication = true;
//                am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                btnSpeaker.setChecked(false);
                am.setSpeakerphoneOn(false);

                stopRingTone();
                stopRingBack();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layoutFunction.setVisibility(View.VISIBLE);

                        btnAccept.setVisibility(View.GONE);
                        btnReject.setVisibility(View.GONE);
                        btnHangup.setVisibility(View.VISIBLE);

                        btnHold.setEnabled(true);
                        btnDial.setEnabled(true);

                        layoutControl.setVisibility(View.VISIBLE);
                    }
                });
                for (Integer key : mCall.keySet()) {
                    if (key != event.getCallID()) {
                        if(isConference) {
                            layoutCallSub.setVisibility(View.GONE);
                            SipServiceCommand.mergeAudio(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls");
                        }else {
                            if (mCall.get(key).isReceive) {
                                SipServiceCommand.setCallHold(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", key, true);
                            } else {
                                Contact contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(mCall.get(key).phone));
                                Utility.loadAvatar(this, ivAvatarSub, contact);
                                tvSubName.setText(contact.getDisplayName());
                                tvSubPhone.setText("Incoming...");
                                btnSubAccept.setEnabled(true);
                                btnSubReject.setEnabled(true);
                                layoutCallSub.setVisibility(View.VISIBLE);
                                layoutFunction.setVisibility(View.GONE);
                                layoutControl.setVisibility(View.GONE);
                                playRingToneSecond();
                            }
                        }
                    }
                }
            }
        }

        if (event.getCallStateCode().equals(pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED)) {
            if (timer == null) {
                timer = new Timer();
                simpleTimerTask = new SimpleTimerTask(new AtomicInteger());
                timer.scheduleAtFixedRate(simpleTimerTask, 0, 1000);
            }
            simpleTimerTask.current.set((int) (System.currentTimeMillis() - event.getConnectTimestamp()) / 1000);
            mCall.get(event.getCallID()).timeStamp = event.getConnectTimestamp();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final CallEvent event) {

        if (!event.getAccountID().equals("") && mCall.isEmpty()) {
            //Outgoing call event
            mCall.put(event.getCallID(), new CallState(event.getPhone(), false, true, 0));
            mCallID = event.getCallID();
        } else {
            //Second call event
            Logger.debug(LOG_TAG, "Second call");
            tvSubName = (TextView) findViewById(R.id.call_activity_sub_name);
            tvSubPhone = (TextView) findViewById(R.id.call_activity_sub_phone);
            ivAvatarSub = (ImageView) findViewById(R.id.call_activity_sub_avatar);
            btnSubAccept = (Button) findViewById(R.id.call_activity_sub_accept);
            btnSubReject = (Button) findViewById(R.id.call_activity_sub_reject);

            final Contact contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(event.getPhone()));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layoutCallSub.setVisibility(View.VISIBLE);
                    layoutControl.setVisibility(View.GONE);
                    layoutFunction.setVisibility(View.GONE);

                    tvSubName.setText(contact.getDisplayName());
                    if(isConference) {
                        tvSubPhone.setText("Outgoing...");
                        btnSubAccept.setEnabled(false);
                        btnSubReject.setEnabled(true);
                    }else{
                        tvSubPhone.setText("Incoming...");
                        btnSubAccept.setEnabled(true);
                        btnSubReject.setEnabled(true);
                    }

                }
            });
            Utility.loadAvatar(this, ivAvatarSub, contact);

            mCall.put(event.getCallID(), new CallState(event.getPhone(), false, false, 1));

            if (mCall.containsKey(mCallID)) {
                if (mCall.get(mCallID).isReceive)
                    playRingToneSecond();
                else
                    playRingTone();
            }
            btnSubAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    btnSubAccept.setEnabled(false);
                    if (!mCall.containsKey(event.getCallID())) {
                        return;
                    }

                    if (!mCall.get(event.getCallID()).isReceive) {
                        Logger.debug(LOG_TAG, "ACCEPT CALL ID SECOND: " + event.getCallID());
                        SipServiceCommand.acceptIncomingCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", event.getCallID());
                        Contact contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(mCall.get(event.getCallID()).phone));
                        Utility.loadAvatar(CallActivity.this, ivAvatar, contact);
                        tvName.setText(contact.getDisplayName());

                        for (final Integer key : mCall.keySet()) {
                            if (key != event.getCallID()) {
                                if (!mCall.get(key).isReceive && mCall.get(key).isCalling) {
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            long id = 0;
                                            if (realm.where(CallLog.class).findAll().size() > 0)
                                                id = realm.where(CallLog.class).max("id").longValue();
                                            CallLog callLog = new CallLog();
                                            callLog.setId((int) id + 1);
                                            callLog.setDate(new Date().getTime());
                                            callLog.setNumberLabel("VoIP");
                                            callLog.setName("");
                                            callLog.setNumber(Utility.formatNumberphone(mCall.get(key).phone));
                                            callLog.setNumberType(0);
                                            callLog.setType(android.provider.CallLog.Calls.OUTGOING_TYPE);
                                            realm.copyToRealm(callLog);
                                        }
                                    });
                                    realm.close();

                                    SipServiceCommand.declineIncomingCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", key);
                                    mCall.remove(key);
                                }
                            }
                        }

                    } else {
                        SipServiceCommand.setCallHold(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", event.getCallID(), true);
                        for (final Integer key : mCall.keySet()) {
                            if (key != event.getCallID()) {
                                if (!mCall.get(key).isReceive) {
                                    Logger.debug(LOG_TAG, "ACCEPT CALL FIRST: " + key);
                                    SipServiceCommand.acceptIncomingCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", key);
                                    Contact contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(mCall.get(key).phone));
                                    Utility.loadAvatar(CallActivity.this, ivAvatar, contact);
                                    tvName.setText(contact.getDisplayName());
                                }
                            }
                        }
                    }

                    stopRingTone();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layoutCallSub.setVisibility(View.GONE);
                        }
                    });
                }
            });

            btnSubReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    btnSubReject.setEnabled(false);

                    if (!mCall.containsKey(event.getCallID())) {
                        return;
                    }

                    if (!mCall.get(event.getCallID()).isReceive) {
                        Logger.debug(LOG_TAG, "REJECT CALL ID: " + event.getCallID());
                        if(mCall.get(event.getCallID()).isCalling){
                            SipServiceCommand.hangUpCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", event.getCallID());
                        }else
                            SipServiceCommand.declineIncomingCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", event.getCallID());
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layoutControl.setVisibility(View.VISIBLE);
                                layoutFunction.setVisibility(View.VISIBLE);
                                layoutCallSub.setVisibility(View.GONE);
                            }
                        });*/

                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                long id = 0;
                                if (realm.where(CallLog.class).findAll().size() > 0)
                                    id = realm.where(CallLog.class).max("id").longValue();
                                CallLog callLog = new CallLog();
                                callLog.setId((int) id + 1);
                                callLog.setName("");
                                callLog.setNumber(Utility.formatNumberphone(event.getPhone()));
                                callLog.setNumberLabel("VoIP");
                                callLog.setDate(new Date().getTime());
                                callLog.setNumberType(0);
                                callLog.setType(android.provider.CallLog.Calls.INCOMING_TYPE);
                                realm.copyToRealm(callLog);
                            }
                        });
                        realm.close();

                    } else
                        for (final Integer key : mCall.keySet()) {
                            if (key != event.getCallID()) {
                                if (!mCall.get(key).isReceive) {
                                    Logger.debug(LOG_TAG, "REJECT CALL ID: " + key);
                                    if(mCall.get(key).isCalling){
                                        SipServiceCommand.hangUpCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", key);
                                    }else
                                        SipServiceCommand.declineIncomingCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", key);

                                    Realm realm = Realm.getDefaultInstance();
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            long id = 0;
                                            if (realm.where(CallLog.class).findAll().size() > 0)
                                                id = realm.where(CallLog.class).max("id").longValue();
                                            CallLog callLog = new CallLog();
                                            callLog.setId((int) id + 1);
                                            callLog.setName("");
                                            callLog.setNumber(Utility.formatNumberphone(mCall.get(key).phone));
                                            callLog.setNumberLabel("VoIP");
                                            callLog.setDate(new Date().getTime());
                                            callLog.setNumberType(0);
                                            if (mCall.get(key).isCalling)
                                                callLog.setType(android.provider.CallLog.Calls.OUTGOING_TYPE);
                                            else
                                                callLog.setType(android.provider.CallLog.Calls.INCOMING_TYPE);
                                            realm.copyToRealm(callLog);
                                        }
                                    });
                                    realm.close();

                                }
                            }
                        }
                    stopRingTone();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        TypefaceManager.initialize(this, R.xml.fonts);
        setContentView(R.layout.activity_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);

        defaultAccount = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
//        Utility.configureRealm(getApplicationContext(), defaultAccount);

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Establish connection to the proxy.
        mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET);
        //Monitor profile events
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        registerReceiver(mProfileReceiver, filter);

        if (vibrator == null) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        createWakeLocks();

        if (callSecondReceiver == null) {
            callSecondReceiver = new SecondCallBroadcast();
            registerReceiver(callSecondReceiver, new IntentFilter(GlobalVars.BROADCAST_ACTION_SIP_SECOND_CALL));
        }

        if (systemObserver == null) {
            systemObserver = new SettingsContentObserver(this, new Handler());
            getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, systemObserver);
        }

        EventBus.getDefault().register(this);

        layoutFunction = (PercentFrameLayout) findViewById(R.id.fm_call_screen_function);
        layoutInfo = (PercentFrameLayout) findViewById(R.id.fm_call_screen_info);
        layoutControl = (PercentFrameLayout) findViewById(R.id.fm_call_screen_control);
        layoutCallSub = (PercentFrameLayout) findViewById(R.id.call_activity_sub_view);
        llDialPad = (LinearLayout) findViewById(R.id.fm_call_ln);
        tvName = (TextView) findViewById(R.id.fm_call_screen_name);
        tvTime = (TextView) findViewById(R.id.fm_call_screen_phone);
        ivAvatar = (ImageView) findViewById(R.id.fm_call_screen_avatar);
        btnSpeaker = (ToggleButton) findViewById(R.id.fm_call_screen_speaker);
        btnMicro = (ToggleButton) findViewById(R.id.fm_call_screen_micro);
        btnHold = (ToggleButton) findViewById(R.id.fm_call_screen_hold);
        btnDial = (ToggleButton) findViewById(R.id.fm_call_screen_dialPad);
        btnBluetooth = (ToggleButton) findViewById(R.id.fm_call_screen_bluetooth);
        btnAdd = (ToggleButton) findViewById(R.id.fm_call_screen_add);
        btnAccept = (Button) findViewById(R.id.fm_call_screen_accept);
        btnReject = (Button) findViewById(R.id.fm_call_screen_reject);
        btnHangup = (Button) findViewById(R.id.fm_call_screen_hang_up);

        edPhoneNumber = (FontEditText) findViewById(R.id.fm_call_screen_edNumberPhone);

        String kindCall = getIntent().getStringExtra("kind") != null ? getIntent().getStringExtra("kind") : "";

        Contact contact = new Contact();
        mCall = new HashMap<>();
        if (kindCall.equals("out")) {

            String real = getIntent().getStringExtra("real") != null ? getIntent().getStringExtra("real") : "";
            contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(real));
            SipServiceCommand.makeCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", real);
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);

            tvName.setText(contact.getDisplayName());
            tvTime.setText("Calling...");
            btnHold.setEnabled(false);
            btnDial.setEnabled(false);
            btnHangup.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnHangup.setEnabled(true);
                }
            }, 300);
        } else if (kindCall.equals("in")) {
            mCallID = getIntent().getIntExtra("callID", -1);
            String phone = getIntent().getStringExtra("phoneNumber") != null ? getIntent().getStringExtra("phoneNumber") : "";
            contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(phone));
            mCall.put(mCallID, new CallState(phone, false, false, 0));
            btnHangup.setVisibility(View.GONE);
            layoutFunction.setVisibility(View.GONE);

            tvName.setText(contact.getDisplayName());
            tvTime.setText("Incoming...");

            playRingTone();
        }

        Utility.loadAvatar(this, ivAvatar, contact);

        setOnClickForTextView();
        //

        final Intent intent = new Intent(GlobalVars.BROADCAST_ACTION_SIP_SERVICE);
        intent.putExtra("function", GlobalVars.SERVICE_METHOD_CALL_IN);
        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                am.setSpeakerphoneOn(btnSpeaker.isChecked());
            }
        });
        btnMicro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (Integer key : mCall.keySet())
                    SipServiceCommand.toggleCallMute(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", key);

                btnMicro.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnMicro.setEnabled(true);
                    }
                }, 500);

            }
        });
        btnHold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SipServiceCommand.toggleCallHold(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID);
                for (Integer key : mCall.keySet()) {
                    if (key != mCallID) {
                        mCallID = key;
                        Contact contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(mCall.get(key).phone));
                        Utility.loadAvatar(CallActivity.this, ivAvatar, contact);
                        tvName.setText(contact.getDisplayName());

                        simpleTimerTask.current.set((int) (System.currentTimeMillis() - mCall.get(key).timeStamp) / 1000);

                        SipServiceCommand.setCallHold(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID, false);
                        break;
                    }
                    //SipServiceCommand.toggleCallHold(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", key);
                }

                btnHold.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnHold.setEnabled(true);
                    }
                }, 500);
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAccept.setEnabled(false);
                Logger.debug(LOG_TAG, "ACCEPT CALL ID: " + mCallID);
                SipServiceCommand.acceptIncomingCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID);

                stopRingTone();
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnReject.setEnabled(false);
                Logger.debug(LOG_TAG, "REJECT CALL ID: " + mCallID);
                SipServiceCommand.declineIncomingCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID);

                {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            long id = 0;
                            if (realm.where(CallLog.class).findAll().size() > 0)
                                id = realm.where(CallLog.class).max("id").longValue();
                            CallLog callLog = new CallLog();
                            callLog.setId((int) id + 1);
                            callLog.setName("");
                            callLog.setNumber(Utility.formatNumberphone(mCall.get(mCallID).phone));
                            callLog.setDataUsage("");
                            callLog.setNumberLabel("VoIP");
                            callLog.setDate(new Date().getTime());
                            callLog.setNumberType(0);
                            callLog.setType(android.provider.CallLog.Calls.MISSED_TYPE);
                            realm.copyToRealm(callLog);
                        }
                    });
                    realm.close();
                }

                deInit();
            }
        });

        btnHangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnHangup.setEnabled(false);
                layoutFunction.setVisibility(View.GONE);
                Logger.debug(LOG_TAG, "HANGUP CALL ID: " + mCallID);
                SipServiceCommand.hangUpCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID);
            }
        });

        btnDial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnDial.isChecked())
                    showDialPad();
                else
                    hideDialPad();

            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnBluetooth.isChecked()) {
                    btnBluetooth.setChecked(false);
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                                Toast.LENGTH_LONG).show();
                    } else {
                        if (mBluetoothHeadset == null || mBluetoothHeadset.getConnectedDevices().isEmpty()) {
                            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                        } else {
                            startBluetoothHeadset();
                        }
                    }
                } else {
                    stopBluetoothHeadset();
                }
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isConference = true;

                SipServiceCommand.renewInvite(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID);

                SipServiceCommand.makeCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", "6531641937");

                btnAdd.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnAdd.setEnabled(true);
                    }
                }, 300);
                //
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(CallActivity.this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    protected void createWakeLocks() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Loneworker - PARTIAL WAKE LOCK");

        try {
            Field f = PowerManager.class.getDeclaredField("PROXIMITY_SCREEN_OFF_WAKE_LOCK");
            int proximityScreenOffWakeLock = (Integer) f.get(null);
            screenOffWakeLock = powerManager.newWakeLock(proximityScreenOffWakeLock, "com.ccsidd.rconnect.proximity");
        } catch (Exception exception) {
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void setupRingTone() {
        String ringtone = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_RINGTONES);
        switch (ringtone) {
            case "0":
                ringtonePlayer = MediaPlayer.create(this, R.raw.ringtone);
                break;
            case "1":
                ringtonePlayer = MediaPlayer.create(this, R.raw.elegant);
                break;
            case "2":
                ringtonePlayer = MediaPlayer.create(this, R.raw.oldschool);
                break;
            default:
                ringtonePlayer = MediaPlayer.create(this, R.raw.ringtone);

        }
        int volume = am.getStreamVolume(AudioManager.STREAM_RING);
        ringtonePlayer.setVolume(volume, volume);
    }

    private void setupRingToneSecond() {
        ringtonePlayer = MediaPlayer.create(this, R.raw.incoming_call);
        int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        ringtonePlayer.setVolume(volume, volume);
    }

    private void startBlink() {
        if (blinkTimer == null) {
            blinkTimer = new Timer();
            blinkTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tvTime.getText().toString().contains("Calling") && blinkTimer != null)
                                tvTime.setVisibility(tvTime.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                        }
                    });
                }
            }, 0, 600);
        }
    }

    private void stopBlink() {
        if (blinkTimer != null) {
            blinkTimer.cancel();
            blinkTimer.purge();
            blinkTimer = null;
            tvTime.setVisibility(View.VISIBLE);
        }
    }

    private void playRingBack() {
        ToneGenerator ringbackTone = Utility.getTone("ringback");
        ringbackTone.stopTone();
        ringbackTone.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK);
    }

    private void stopRingBack() {
        ToneGenerator ringbackTone = Utility.getTone("ringback");
        ringbackTone.stopTone();
    }

    private void playRingTone() {
        long pattern[] = {1000, 500};
        vibrator.vibrate(pattern, 0);
        am.setSpeakerphoneOn(true);

        if (!requestAudioFocusForMyApp()) {
            return;
        }

        if (ringtonePlayer != null) {
            ringtonePlayer.reset();
            ringtonePlayer.release();
            ringtonePlayer = null;
        }
        setupRingTone();

        //ringtonePlayer.setVolume(am.getStreamVolume(AudioManager.STREAM_MUSIC), am.getStreamVolume(AudioManager.STREAM_MUSIC));
        ringtonePlayer.setLooping(true);
        ringtonePlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        ringtonePlayer.start();
    }

    private void playRingToneSecond() {
        if (!requestAudioFocusForMyApp()) {
            return;
        }
        if (ringtonePlayer != null) {
            ringtonePlayer.reset();
            ringtonePlayer.release();
            ringtonePlayer = null;
        }
        setupRingToneSecond();

        //ringtonePlayer.setVolume(am.getStreamVolume(AudioManager.STREAM_MUSIC), am.getStreamVolume(AudioManager.STREAM_MUSIC));
        ringtonePlayer.setLooping(true);
        ringtonePlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        ringtonePlayer.start();

    }

    private void stopRingTone() {
        if (vibrator != null)
            vibrator.cancel();
        if (ringtonePlayer != null) {
            try {
                if (ringtonePlayer.isPlaying())
                    ringtonePlayer.stop();
            } catch (Exception ignored) {
            }

            try {
                ringtonePlayer.reset();
                ringtonePlayer.release();
                ringtonePlayer = null;
            } catch (Exception ignored) {
            }
        }

        releaseAudioFocusForMyApp();
    }

    public void wakeDevice() {
        fullWakeLock.acquire();

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
    }

    private void setOnClickForTextView() {
        for (int i = 0; i < llDialPad.getChildCount(); i++) {
            if (llDialPad.getChildAt(i) instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) llDialPad.getChildAt(i)).getChildCount(); j++) {
                    if (((LinearLayout) llDialPad.getChildAt(i)).getChildAt(j) instanceof LinearLayout) {
                        LinearLayout ll1 = (LinearLayout) ((LinearLayout) llDialPad.getChildAt(i)).getChildAt(j);
                        if (ll1.getTag() != null) {
                            ll1.setOnClickListener(this);
                            if (ll1.getTag().equals("0"))
                                ll1.setOnLongClickListener(this);
                        }
                    }
                }
            }
        }
    }

    private void showDialPad() {
//        topBackgroundlayout.setVisibility(View.VISIBLE);
        llDialPad.setVisibility(View.VISIBLE);
        ivAvatar.setVisibility(View.GONE);
        tvTime.setVisibility(View.GONE);
//        tvPhone2.setVisibility(View.GONE);
        tvName.setVisibility(View.GONE);
    }

    private void hideDialPad() {
//        topBackgroundlayout.setVisibility(View.GONE);
        llDialPad.setVisibility(View.GONE);
        ivAvatar.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);
//        tvPhone2.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (partialWakeLock.isHeld()) {
            partialWakeLock.release();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        partialWakeLock.acquire();
    }

    public void deInit() {
        try {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
                mSensorManager = null;
            }
        } catch (Exception ex) {
        }

        ToneGenerator toneGenerator = Utility.getTone("dtmf");
        toneGenerator.stopTone();

        if (vibrator != null)
            vibrator.cancel();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        try {
            getContentResolver().unregisterContentObserver(systemObserver);
            systemObserver = null;
        } catch (Exception ex) {
        }
        Intent intent = new Intent(GlobalVars.BROADCAST_ACTION_SIP_CALL_END);
        sendBroadcast(intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("com.ccsidd.rtone", "CallActivity is destroy");

        for (Integer key : mCall.keySet())
            SipServiceCommand.hangUpCall(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", key);

        unregisterReceiver(callSecondReceiver);

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().removeAllStickyEvents();

        if (fullWakeLock != null && fullWakeLock.isHeld()) {
            fullWakeLock.release();
            fullWakeLock = null;
        }
        if (partialWakeLock != null && partialWakeLock.isHeld()) {
            partialWakeLock.release();
            partialWakeLock = null;
        }
        if (screenOffWakeLock != null && screenOffWakeLock.isHeld()) {
            screenOffWakeLock.release();
            screenOffWakeLock = null;
        }

        try {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
            unregisterReceiver(mProfileReceiver);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Logger.debug(LOG_TAG, isTaskRoot() ? "true" : "false");

        if (isTaskRoot()) {
            Intent iMainActivity = new Intent(this, MainActivity.class);
            iMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(iMainActivity);
        }
        stopRingBack();
        stopRingTone();
    }

    @Override
    public void onBackPressed() {
    }

    public void insertNumber(String number) {
        if (edPhoneNumber.getSelectionStart() > -1) {
            int start = Math.max(edPhoneNumber.getSelectionStart(), 0);
            int end = Math.max(edPhoneNumber.getSelectionEnd(), 0);
            edPhoneNumber.getText().replace(Math.min(start, end), Math.max(start, end),
                    number, 0, number.length());
        } else
            edPhoneNumber.append(number);

        if (!mCall.containsKey(mCallID))
            return;
//        Intent dtmfIntent = new Intent(GlobalVars.BROADCAST_ACTION_SIP_SERVICE);
//        dtmfIntent.putExtra("function", GlobalVars.SERVICE_METHOD_SEND_DTMF);
//        dtmfIntent.putExtra("phoneNumber", mCall.get(mCallID).phone);
        int toneTime = 200;
        ToneGenerator toneGenerator = Utility.getTone("dtmf");
        switch (number) {
            case "0":
//                dtmfIntent.putExtra("data", "0");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, toneTime);
                break;
            case "1":
//                dtmfIntent.putExtra("data", "1");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, toneTime);
                break;
            case "2":
//                dtmfIntent.putExtra("data", "2");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_2, toneTime);
                break;
            case "3":
//                dtmfIntent.putExtra("data", "3");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_3, toneTime);
                break;
            case "4":
//                dtmfIntent.putExtra("data", "4");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_4, toneTime);
                break;
            case "5":
//                dtmfIntent.putExtra("data", "5");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, toneTime);
                break;
            case "6":
//                dtmfIntent.putExtra("data", "6");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_6, toneTime);
                break;
            case "7":
//                dtmfIntent.putExtra("data", "7");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_7, toneTime);
                break;
            case "8":
//                dtmfIntent.putExtra("data", "8");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_8, toneTime);
                break;
            case "9":
//                dtmfIntent.putExtra("data", "9");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_9, toneTime);
                break;
            case "*":
//                dtmfIntent.putExtra("data", "*");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_S, toneTime);
                break;
            case "#":
//                dtmfIntent.putExtra("data", "#");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_P, toneTime);
                break;
        }
        SipServiceCommand.sendDTMF(this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID, number);
//        sendBroadcast(dtmfIntent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getTag().toString()) {
            case "0":
                if (!isPressing0) {
                    insertNumber("0");
                } else
                    isPressing0 = false;
                break;
            default:
                insertNumber(view.getTag().toString());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getTag().toString()) {
            case "0":
                insertNumber("+");
                isPressing0 = true;
                break;
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        if (distance == 0 && !screenOffWakeLock.isHeld() && isInCommunication) {
            screenOffWakeLock.acquire();
        } else if (screenOffWakeLock.isHeld())
            screenOffWakeLock.release();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Logger.debug(LOG_TAG, "Low memory");
        deInit();
    }

    private void notifyAudioState(Intent intent) {
        final int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1);
        String message;
        switch (state) {
            case BluetoothHeadset.STATE_AUDIO_CONNECTED:
                message = "Bluetooth Audio Connected";
                btnBluetooth.setChecked(true);
                btnSpeaker.setChecked(false);
                btnSpeaker.setEnabled(false);
                break;
            case BluetoothHeadset.STATE_AUDIO_CONNECTING:
                message = "Bluetooth Audio Connecting";
                break;
            case BluetoothHeadset.STATE_AUDIO_DISCONNECTED:
                btnBluetooth.setChecked(false);
                btnSpeaker.setEnabled(true);
                message = "Bluetooth Audio Disconnected";
                break;
            default:
                message = "Audio Unknown";
                break;
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void notifyConnectState(Intent intent) {
        final int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1);
        String message;
        Intent intentBT = new Intent(GlobalVars.BROADCAST_ACTION_SIP_SERVICE);
        intentBT.putExtra("function", GlobalVars.SERVICE_METHOD_CALL_IN);
        switch (state) {
            case BluetoothHeadset.STATE_CONNECTED:

                startBluetoothHeadset();

                message = "Bluetooth Headset Connected";
                break;
            case BluetoothHeadset.STATE_CONNECTING:
                message = "Bluetooth Headset Connecting";
                break;
            case BluetoothHeadset.STATE_DISCONNECTING:
                message = "Bluetooth Headset Disconnecting";
                break;
            case BluetoothHeadset.STATE_DISCONNECTED:

                stopBluetoothHeadset();

                btnBluetooth.setChecked(false);
                btnSpeaker.setEnabled(true);
                message = "Bluetooth Headset Disconnected";
                break;
            default:
                message = "Connect Unknown";
                break;
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void notifyATEvent(Intent intent) {
        String command = intent.getStringExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD);
        int type = intent.getIntExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE, -1);

        String typeString;
        switch (type) {
            case BluetoothHeadset.AT_CMD_TYPE_ACTION:
                typeString = "AT Action";
                break;
            case BluetoothHeadset.AT_CMD_TYPE_READ:
                typeString = "AT Read";
                break;
            case BluetoothHeadset.AT_CMD_TYPE_TEST:
                typeString = "AT Test";
                break;
            case BluetoothHeadset.AT_CMD_TYPE_SET:
                typeString = "AT Set";
                break;
            case BluetoothHeadset.AT_CMD_TYPE_BASIC:
                typeString = "AT Basic";
                break;
            default:
                typeString = "AT Unknown";
                break;
        }

        Toast.makeText(this, typeString + ": " + command, Toast.LENGTH_SHORT).show();
    }

    public boolean startBluetoothHeadset() {

        if (am.isBluetoothScoAvailableOffCall()) {
            if (am.isBluetoothScoOn()) {
                am.stopBluetoothSco();
                am.startBluetoothSco();
            } else {
                am.startBluetoothSco();
            }

        }

        return true;
    }

    public boolean stopBluetoothHeadset() {

        if (am.isBluetoothScoAvailableOffCall()) {
            if (am.isBluetoothScoOn()) {
                am.stopBluetoothSco();
            }
        }

        return true;

    }

    private boolean requestAudioFocusForMyApp() {

        // Request audio focus for playback
        int result = am.requestAudioFocus(null,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("AudioFocus", "Audio focus received");
            return true;
        } else {
            Log.d("AudioFocus", "Audio focus NOT received");
            return false;
        }
    }

    void releaseAudioFocusForMyApp() {
        am.abandonAudioFocus(null);
    }

    private class SimpleTimerTask extends TimerTask {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        private AtomicInteger current;

        public SimpleTimerTask(AtomicInteger current) {
            this.current = current;
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTime.setText(sdf.format(new Date(current.intValue() * 1000)));
                    current.addAndGet(1);
                }
            });
        }
    }

    private class CallState {
        String phone = "";
        Boolean isReceive = false;
        Boolean isCalling = false;
        long timeStamp = 0;

        public CallState(String phone, Boolean isReceive, Boolean isCalling, long timeStamp) {
            this.phone = phone;
            this.isReceive = isReceive;
            this.isCalling = isCalling;
            this.timeStamp = timeStamp;
        }
    }

    public class SecondCallBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {

            /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());*/

        }
    }

    public class MyPhoneStateListener extends PhoneStateListener {

        private boolean onCall = false;

        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(LOG_TAG, "IDLE");
                    if (onCall) {
                        if (mCall.containsKey(mCallID)) {
                            SipServiceCommand.setCallHold(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID, false);
                        }
                        onCall = false;
                    }
                    Log.d(LOG_TAG, "MODE: " + am.getMode());
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(LOG_TAG, "OFFHOOK");
                    onCall = true;
                    if (mCall.containsKey(mCallID)) {
                        SipServiceCommand.setCallHold(CallActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls", mCallID, true);
                    }
                    Log.d(LOG_TAG, "MODE: " + am.getMode());

                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(LOG_TAG, "RINGING");

                    break;
            }
        }
    }

    public class SettingsContentObserver extends ContentObserver {
        int previousVolume;
//        Context context;

        public SettingsContentObserver(Context c, Handler handler) {
            super(handler);
//            context = c;

            previousVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

            int delta = previousVolume - currentVolume;

            if (delta > 0) {
                previousVolume = currentVolume;
                stopRingTone();
            } else if (delta < 0) {
                previousVolume = currentVolume;
                stopRingTone();
            }
        }
    }
}
