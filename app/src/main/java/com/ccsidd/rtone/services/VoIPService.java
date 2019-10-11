package com.ccsidd.rtone.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.objects.AccountStateEvent;
import com.ccsidd.rtone.objects.CallEvent;
import com.ccsidd.rtone.objects.CallStateEvent;
import com.ccsidd.rtone.objects.Setting;
import com.ccsidd.rtone.objects.User;
import com.ccsidd.rtone.objects.sip.RLogWriter;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CodecInfo;
import org.pjsip.pjsua2.CodecInfoVector;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.PresenceStatus;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsip_transport_type_e;
import org.pjsip.pjsua2.pjsua_buddy_status;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_ACCEPT_INCOMING_CALL;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_ADD_BUDDY;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_DECLINE_INCOMING_CALL;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_GET_CALL_STATUS;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_GET_CODEC_PRIORITIES;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_GET_REGISTRATION_STATUS;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_HANG_UP_CALL;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_HANG_UP_CALLS;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_HOLD_CALLS;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_MAKE_CALL;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_MERGE_AUDIO;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_REMOVE_ACCOUNT;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_REMOVE_BUDDY;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_RENEW_INVITE;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_RESTART_SIP_STACK;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_SEND_DTMF;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_SEND_MESSAGE;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_SET_ACCOUNT;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_SET_CODEC_PRIORITIES;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_SET_HOLD;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_SET_MUTE;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_TOGGLE_HOLD;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_TOGGLE_MUTE;
import static com.ccsidd.rtone.services.SipServiceCommand.ACTION_TRANSFER_CALL;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_ACCOUNT_DATA;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_ACCOUNT_ID;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_BODY;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_CALL_ID;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_CODEC_PRIORITIES;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_DTMF;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_HOLD;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_MUTE;
import static com.ccsidd.rtone.services.SipServiceCommand.PARAM_NUMBER;


/**
 * Created by dung on 9/8/15.
 */

public class VoIPService extends BackgroundService {

    //    public static final String PREFS_KEY_ACCOUNTS = "accounts";
    private static final String TAG = VoIPService.class.getSimpleName();
    public static final String PREFS_NAME = TAG + "prefs";
    private static final String PREFS_KEY_CODEC_PRIORITIES = "codec_priorities";
    private static ConcurrentHashMap<String, SipAccount> mActiveSipAccounts = new ConcurrentHashMap<>();

    static {
        try {
            System.loadLibrary("pjsua2");
            Logger.debug(TAG, "PJSIP pjsua2 loaded");
        } catch (UnsatisfiedLinkError error) {
            Logger.error(TAG, "Error while loading PJSIP pjsua2 native library", error);
            throw new RuntimeException(error);
        }
    }

    //    private static final long[] VIBRATOR_PATTERN = {0, 1000, 1000};
    private BroadcastReceiver networkReceiver;
    private boolean isFirstTime = false;
    private List<SipAccountData> mConfiguredAccounts = new ArrayList<>();
    /*private MediaPlayer mRingTone;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private Uri mRingtoneUri;*/
    private Endpoint mEndpoint;
    private int transportID;
    private RLogWriter logWriter;
    private volatile boolean mStarted;
//    private BroadcastEventEmitter mBroadcastEmitter;
    private Realm realm;

    private void startListeningNetwork() {

        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (!isFirstTime) {
                    isFirstTime = true;
                    return;
                }
                enqueueJob(new Runnable() {
                    @Override
                    public void run() {
                        if (mStarted)
                            mEndpoint.hangupAllCalls();
                        if (Utility.checkNetwork(VoIPService.this)) {
//                                handleRestartSipStack();
                            removeAllActiveAccounts();
                            addAllConfiguredAccounts();
                        }

                    }
                });

                Logger.debug(TAG, "Network changed");
            }
        };
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startListeningNetwork();
        enqueueJob(new Runnable() {
            @Override
            public void run() {
                Logger.debug(TAG, "Creating SipService with priority: " + Thread.currentThread().getPriority());

//                am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                /*mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(VoIPService.this, RingtoneManager.TYPE_RINGTONE);

                mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);*/
//                mBroadcastEmitter = new BroadcastEventEmitter(VoIPService.this);

                loadConfiguredAccounts();
//                Logger.info(TAG + "NetWork", Utility.checkNetwork(VoIPService.this) ? "true":"false");
//                if (Utility.checkNetwork(VoIPService.this)) {
                    addAllConfiguredAccounts();
//                }

                Logger.debug(TAG, "SipService created!");
            }
        });
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        enqueueJob(new Runnable() {
            @Override
            public void run() {
                if (intent == null) return;

                String action = intent.getAction();

                if (ACTION_SET_ACCOUNT.equals(action)) {
                    handleSetAccount(intent);

                } else if (ACTION_REMOVE_ACCOUNT.equals(action)) {
                    handleRemoveAccount(intent);

                } else if (ACTION_RESTART_SIP_STACK.equals(action)) {
                    handleRestartSipStack();

                } else if (ACTION_MAKE_CALL.equals(action)) {
                    handleMakeCall(intent);

                } else if (ACTION_HANG_UP_CALL.equals(action)) {
                    handleHangUpCall(intent);

                } else if (ACTION_HANG_UP_CALLS.equals(action)) {
                    handleHangUpActiveCalls(intent);

                } else if (ACTION_HOLD_CALLS.equals(action)) {
                    handleHoldActiveCalls(intent);

                } else if (ACTION_GET_CALL_STATUS.equals(action)) {
                    handleGetCallStatus(intent);

                } else if (ACTION_SEND_DTMF.equals(action)) {
                    handleSendDTMF(intent);

                } else if (ACTION_ACCEPT_INCOMING_CALL.equals(action)) {
                    handleAcceptIncomingCall(intent);

                } else if (ACTION_DECLINE_INCOMING_CALL.equals(action)) {
                    handleDeclineIncomingCall(intent);

                } else if (ACTION_SET_HOLD.equals(action)) {
                    handleSetCallHold(intent);

                } else if (ACTION_RENEW_INVITE.equals(action)) {
                    handleRenewInvite(intent);

                }else if (ACTION_TOGGLE_HOLD.equals(action)) {
                    handleToggleCallHold(intent);

                } else if (ACTION_SET_MUTE.equals(action)) {
                    handleSetCallMute(intent);

                } else if (ACTION_TOGGLE_MUTE.equals(action)) {
                    handleToggleCallMute(intent);

                } else if (ACTION_TRANSFER_CALL.equals(action)) {
                    handleTransferCall(intent);

                } else if (ACTION_GET_CODEC_PRIORITIES.equals(action)) {
                    handleGetCodecPriorities();

                } else if (ACTION_SET_CODEC_PRIORITIES.equals(action)) {
                    handleSetCodecPriorities(intent);

                } else if (ACTION_GET_REGISTRATION_STATUS.equals(action)) {
                    handleGetRegistrationStatus(intent);

                } else if (ACTION_SEND_MESSAGE.equals(action)) {
                    handleSendMessage(intent);

                } else if (ACTION_ADD_BUDDY.equals(action)) {
                    handleAddBuddy(intent);

                } else if (ACTION_REMOVE_BUDDY.equals(action)) {
                    handleRemoveBuddy(intent);

                }else if (ACTION_MERGE_AUDIO.equals(action)) {
                    handleMergeAudio(intent);

                }
                if (mConfiguredAccounts.isEmpty()) {
                    Logger.debug(TAG, "No more configured accounts. Shutting down service");
                    //stopSelf();
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(networkReceiver);
        enqueueJob(new Runnable() {
            @Override
            public void run() {
                Logger.debug(TAG, "Destroying SipService");
//                stopStack();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        super.onDestroy();
    }

    private SipCall getCall(String accountID, int callID) {
        SipAccount account = mActiveSipAccounts.get(accountID);

        if (account == null) return null;
        return account.getCall(callID);
    }

    private void notifyCallDisconnected(String accountID, int callID) {
//        mBroadcastEmitter.callState(accountID, callID,
//                pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED.swigValue(),
//                pjsip_status_code.PJSIP_SC_DECLINE.swigValue(),
//                0, false, false, "");
        EventBus.getDefault().post(new CallStateEvent(accountID, callID,
                pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED,
                pjsip_status_code.PJSIP_SC_DECLINE,
                0, false, false, ""));
    }

    private void handleGetCallStatus(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

//        mBroadcastEmitter.callState(accountID, callID, sipCall.getCurrentState().swigValue(),
//                sipCall.getLastStatusCode().swigValue(),
//                sipCall.getConnectTimestamp(), sipCall.isLocalHold(),
//                sipCall.isLocalMute(), "");
        EventBus.getDefault().post(new CallStateEvent(accountID, callID, sipCall.getCurrentState(),
                sipCall.getLastStatusCode(),
                sipCall.getConnectTimestamp(), sipCall.isLocalHold(),
                sipCall.isLocalMute(), ""));
    }

    private void handleSendDTMF(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);
        String dtmf = intent.getStringExtra(PARAM_DTMF);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.dialDtmf(dtmf);
        } catch (Exception exc) {
            Logger.error(TAG, "Error while dialing dtmf: " + dtmf + ". AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void handleAcceptIncomingCall(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.acceptIncomingCall();
        } catch (Exception exc) {
            Logger.error(TAG, "Error while accepting incoming call. AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void handleSetCallHold(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);
        boolean hold = intent.getBooleanExtra(PARAM_HOLD, false);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.setHold(hold);
        } catch (Exception exc) {
            Logger.error(TAG, "Error while setting hold. AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void handleRenewInvite(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.renewInvite();
        } catch (Exception exc) {
            Logger.error(TAG, "Error while setting hold. AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void handleToggleCallHold(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.toggleHold();
        } catch (Exception exc) {
            Logger.error(TAG, "Error while toggling hold. AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void handleSetCallMute(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);
        boolean mute = intent.getBooleanExtra(PARAM_MUTE, false);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.setMute(mute);
        } catch (Exception exc) {
            Logger.error(TAG, "Error while setting mute. AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void handleToggleCallMute(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.toggleMute();
        } catch (Exception exc) {
            Logger.error(TAG, "Error while toggling mute. AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void handleDeclineIncomingCall(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        SipCall sipCall = getCall(accountID, callID);
        if (sipCall == null) {
            notifyCallDisconnected(accountID, callID);
            return;
        }

        try {
            sipCall.declineIncomingCall();
        } catch (Exception exc) {
            Logger.error(TAG, "Error while declining incoming call. AccountID: "
                    + accountID + ", CallID: " + callID);
        }
    }

    private void handleHangUpCall(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);

        try {
            SipCall sipCall = getCall(accountID, callID);

            if (sipCall == null) {
                notifyCallDisconnected(accountID, callID);
                return;
            }
            EventBus.getDefault().post(new CallStateEvent(accountID, callID, pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED,
                    sipCall.getLastStatusCode(),
                    sipCall.getConnectTimestamp(), sipCall.isLocalHold(),
                    sipCall.isLocalMute(), sipCall.getDataUsageAndTimeDuration()));

            SipAccount account = mActiveSipAccounts.get(accountID);
            account.removeCall(callID);

            sipCall.delete();

//            }

        } catch (Exception exc) {
            Logger.error(TAG, "Error while hanging up call", exc);
            notifyCallDisconnected(accountID, callID);
        }
    }

    private void handleHangUpActiveCalls(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);

        SipAccount account = mActiveSipAccounts.get(accountID);
        if (account == null) return;

        Set<Integer> activeCallIDs = account.getCallIDs();

        if (activeCallIDs == null || activeCallIDs.isEmpty()) return;

        for (int callID : activeCallIDs) {
            try {
                SipCall sipCall = getCall(accountID, callID);

                if (sipCall == null) {
                    notifyCallDisconnected(accountID, callID);
                    return;
                }

                EventBus.getDefault().post(new CallStateEvent(accountID, callID, sipCall.getCurrentState(),
                        sipCall.getLastStatusCode(),
                        sipCall.getConnectTimestamp(), sipCall.isLocalHold(),
                        sipCall.isLocalMute(), sipCall.getDataUsageAndTimeDuration()));
                account.removeCall(callID);
                sipCall.delete();

            } catch (Exception exc) {
                Logger.error(TAG, "Error while hanging up call", exc);
                notifyCallDisconnected(accountID, callID);
            }
        }
    }

    private void handleHoldActiveCalls(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);

        SipAccount account = mActiveSipAccounts.get(accountID);
        if (account == null) return;

        Set<Integer> activeCallIDs = account.getCallIDs();

        if (activeCallIDs == null || activeCallIDs.isEmpty()) return;

        for (int callID : activeCallIDs) {
            try {
                SipCall sipCall = getCall(accountID, callID);

                if (sipCall == null) {
                    notifyCallDisconnected(accountID, callID);
                    return;
                }

                sipCall.setHold(true);
            } catch (Exception exc) {
                Logger.error(TAG, "Error while holding call", exc);
            }
        }
    }

    private void handleTransferCall(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        int callID = intent.getIntExtra(PARAM_CALL_ID, 0);
        String number = intent.getStringExtra(PARAM_NUMBER);

        try {
            SipCall sipCall = getCall(accountID, callID);

            if (sipCall == null) {
                notifyCallDisconnected(accountID, callID);
                return;
            }

            sipCall.transferTo(number);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while transferring call to " + number, exc);
            notifyCallDisconnected(accountID, callID);
        }
    }

    private void handleMakeCall(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        String number = intent.getStringExtra(PARAM_NUMBER);

        Logger.debug(TAG, "Making call to " + number);

        try {
            SipCall call = mActiveSipAccounts.get(accountID).addOutgoingCall(number);
            EventBus.getDefault().post(new CallEvent(accountID, call.getId(), number));
        } catch (Exception exc) {
            Logger.error(TAG, "Error while making outgoing call", exc);
            EventBus.getDefault().post(new CallEvent(accountID, -1, number));
        }
    }

    private void handleSendMessage(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        String number = intent.getStringExtra(PARAM_NUMBER);
        String body = intent.getStringExtra(PARAM_BODY);

        Logger.debug(TAG, "Send message to " + number);

        try {
            mActiveSipAccounts.get(accountID).sendInstantMessage(number, body);
        } catch (Exception exc) {
            Logger.error(TAG, "Error while making outgoing call", exc);
        }
    }

    private void handleRestartSipStack() {
        Logger.debug(TAG, "Restarting SIP stack");
        stopStack();
        loadConfiguredAccounts();
        addAllConfiguredAccounts();
    }

    private void handleResetAccounts() {
        Logger.debug(TAG, "Removing all the configured accounts");

        Iterator<SipAccountData> iterator = mConfiguredAccounts.iterator();

        while (iterator.hasNext()) {
            SipAccountData data = iterator.next();

            try {
                removeAccount(data.getIdUri());
                iterator.remove();

                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<User> users = realm.where(User.class).findAll();
                        if (users.size() > 0) {
                            users.first().setPassword("");
                        }
                    }
                });
                realm.close();
            } catch (Exception exc) {
                Logger.error(TAG, "Error while removing account " + data.getIdUri(), exc);
            }
        }

//        persistConfiguredAccounts();
    }

    private void handleRemoveAccount(Intent intent) {
        String accountIDtoRemove = intent.getStringExtra(PARAM_ACCOUNT_ID);

        Logger.debug(TAG, "Removing " + accountIDtoRemove);

        Iterator<SipAccountData> iterator = mConfiguredAccounts.iterator();

        while (iterator.hasNext()) {
            final SipAccountData data = iterator.next();

            if (data.getIdUri().equals(accountIDtoRemove)) {
                try {
                    removeAccount(accountIDtoRemove);
                    iterator.remove();
//                    persistConfiguredAccounts();
                    realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<User> users = realm.where(User.class).equalTo("username", data.getUsername()).findAll();
                            if (users.size() > 0 && !users.first().getPassword().equals("")) {
                                users.first().setPassword("");

                                RealmResults<Setting> settingLoginToken = realm.where(Setting.class).equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).findAll();
                                if (settingLoginToken.size() > 0) {
                                    settingLoginToken.first().setValue("");
                                }
                                RealmResults<Setting> settingToken = realm.where(Setting.class).equalTo("key", GlobalVars.KEY_SETTING_TOKEN).findAll();
                                if (settingToken.size() > 0) {
                                    settingToken.first().setValue("");
                                }
                            }
                        }
                    });
                    realm.close();
                } catch (Exception exc) {
                    Logger.error(TAG, "Error while removing account " + accountIDtoRemove, exc);
                }
                break;
            }
        }
    }

    private void handleSetAccount(Intent intent) {
        SipAccountData data = intent.getParcelableExtra(PARAM_ACCOUNT_DATA);

        int index = mConfiguredAccounts.indexOf(data);
        if (index == -1) {
            handleResetAccounts();
            Logger.debug(TAG, "Adding " + data.getIdUri());

            try {
                handleSetCodecPriorities(intent);
                addAccount(data);
                mConfiguredAccounts.add(data);
                //persistConfiguredAccounts();
            } catch (Exception exc) {
                Logger.error(TAG, "Error while adding " + data.getIdUri(), exc);
            }
        } else {
            Logger.debug(TAG, "Reconfiguring " + data.getIdUri());

            try {
                removeAccount(data.getIdUri());
                handleSetCodecPriorities(intent);
                addAccount(data);
                mConfiguredAccounts.set(index, data);
                //persistConfiguredAccounts();
            } catch (Exception exc) {
                Logger.error(TAG, "Error while reconfiguring " + data.getIdUri(), exc);
            }
        }
    }

    private void handleSetCodecPriorities(Intent intent) {
        ArrayList<CodecPriority> codecPriorities = intent.getParcelableArrayListExtra(PARAM_CODEC_PRIORITIES);

        if (codecPriorities == null) {
            return;
        }

        startStack();

        if (!mStarted) {
//            mBroadcastEmitter.codecPrioritiesSetStatus(false);
            return;
        }

        try {
            StringBuilder log = new StringBuilder();
            log.append("Codec priorities successfully set. The priority order is now:\n");

            for (CodecPriority codecPriority : codecPriorities) {
                mEndpoint.codecSetPriority(codecPriority.getCodecId(), (short) codecPriority.getPriority());
                log.append(codecPriority.toString()).append("\n");
            }

            persistConfiguredCodecPriorities(codecPriorities);
            Logger.debug(TAG, log.toString());
//            mBroadcastEmitter.codecPrioritiesSetStatus(true);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while setting codec priorities", exc);
//            mBroadcastEmitter.codecPrioritiesSetStatus(false);
        }
    }

    private void handleGetCodecPriorities() {
        ArrayList<CodecPriority> codecs = getCodecPriorityList();

        if (codecs != null) {
//            mBroadcastEmitter.codecPriorities(codecs);
        }
    }

    private void handleGetRegistrationStatus(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);

        if (!mStarted || mActiveSipAccounts.get(accountID) == null) {
//            mBroadcastEmitter.registrationState("", 400, "", "");
            EventBus.getDefault().post(new AccountStateEvent("", pjsip_status_code.PJSIP_SC_BAD_REQUEST, "", ""));
            return;
        }

        SipAccount account = mActiveSipAccounts.get(accountID);
        try {
//            mBroadcastEmitter.registrationState(accountID, account.getInfo().getRegStatus().swigValue(),
//                    account.getData().getToken(), account.getData().getEndpoint());
            EventBus.getDefault().post(new AccountStateEvent(accountID, account.getInfo().getRegStatus(),
                    account.getData().getToken(), account.getData().getEndpoint()));
        } catch (Exception exc) {
            Logger.error(TAG, "Error while getting registration status for " + accountID, exc);
        }
    }

    private void persistConfiguredCodecPriorities(ArrayList<CodecPriority> codecPriorities) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY_CODEC_PRIORITIES, new Gson().toJson(codecPriorities)).apply();
    }

    private ArrayList<CodecPriority> getConfiguredCodecPriorities() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String codecPriorities = prefs.getString(PREFS_KEY_CODEC_PRIORITIES, "");
        if (codecPriorities.isEmpty()) {
            return null;
        }

        Type listType = new TypeToken<ArrayList<CodecPriority>>() {
        }.getType();
        return new Gson().fromJson(codecPriorities, listType);
    }

    private void loadConfiguredAccounts() {
        /*SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String accounts = prefs.getString(PREFS_KEY_ACCOUNTS, "");

        if (accounts.isEmpty()) {
            mConfiguredAccounts = new ArrayList<>();
        } else {
            Type listType = new TypeToken<ArrayList<SipAccountData>>() {
            }.getType();
            mConfiguredAccounts = new Gson().fromJson(accounts, listType);
        }*/
        Utility.configureRealm(getApplicationContext(), Utility.getPref(getApplicationContext(),
                GlobalVars.PREFERENCES_DATA_FILE_NAME,
                GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));
        realm = Realm.getDefaultInstance();
        RealmResults<User> users = realm.where(User.class).findAll();
        String username;
        String password;
        if (users.size() > 0) {
            User user = users.first();
            username = user.getUsername();
            password = user.getPassword();
        } else {
            realm.close();
            return;
        }
        RealmResults<Setting> settings = realm.where(Setting.class).findAll();
        String token = "";
        if (settings.where().equalTo("key", GlobalVars.KEY_SETTING_TOKEN).count() > 0) {
            token = settings.where().equalTo("key", GlobalVars.KEY_SETTING_TOKEN).findFirst().getValue();
        }
        String endpoint = "";
        if (settings.where().equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).count() > 0) {
            endpoint = settings.where().equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).findFirst().getValue();
        }
        realm.close();

        if (password.equals("") || endpoint.equals("") || token.equals(""))
            mConfiguredAccounts = new ArrayList<>();
        else
            mConfiguredAccounts.add(new SipAccountData().setHost("rtone.ccsidd.com")
                    .setPort(5061)
                    .setTcpTransport(true)
                    .setUsername(username)
                    .setPassword(password)
                    .setToken(token)
                    .setEndpoint(endpoint)
                    .setRealm(GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls"));
    }

    private void addAllConfiguredAccounts() {
        if (!mConfiguredAccounts.isEmpty()) {
            for (SipAccountData accountData : mConfiguredAccounts) {
                try {
                    addAccount(accountData);
                } catch (Exception exc) {
                    Logger.error(TAG, "Error while adding " + accountData.getIdUri());
                }
            }
        }
    }

    /**
     * Adds a new SIP Account and performs initial registration.
     *
     * @param account SIP account to add
     */
    private void addAccount(SipAccountData account) throws Exception {
        String accountString = account.getIdUri();

        if (!mActiveSipAccounts.containsKey(accountString)) {
            startStack();
            SipAccount pjSipAndroidAccount = new SipAccount(this, account);
            pjSipAndroidAccount.create();
            pjSipAndroidAccount.setRegistration(true);
            PresenceStatus presenceStatus = new PresenceStatus();
            presenceStatus.setStatus(pjsua_buddy_status.PJSUA_BUDDY_STATUS_ONLINE);
            pjSipAndroidAccount.setOnlineStatus(presenceStatus);
            mActiveSipAccounts.put(accountString, pjSipAndroidAccount);
            Logger.debug(TAG, "SIP account " + account.getIdUri() + " successfully added");
        }
    }

    private void handleAddBuddy(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        String number = intent.getStringExtra(PARAM_NUMBER);

        if (!mStarted || mActiveSipAccounts.get(accountID) == null) {
            EventBus.getDefault().post(new AccountStateEvent("", pjsip_status_code.PJSIP_SC_BAD_REQUEST, "", ""));
            return;
        }

        SipAccount account = mActiveSipAccounts.get(accountID);
        if (account != null)
            account.addBuddy(number);
    }

    private void handleRemoveBuddy(Intent intent) {
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        String number = intent.getStringExtra(PARAM_NUMBER);

        if (!mStarted || mActiveSipAccounts.get(accountID) == null) {
            EventBus.getDefault().post(new AccountStateEvent("", pjsip_status_code.PJSIP_SC_BAD_REQUEST, "", ""));
            return;
        }

        SipAccount account = mActiveSipAccounts.get(accountID);
        if (account != null)
            account.removeBuddy(number);
    }

    /**
     * Removes a SIP Account and performs un-registration.
     */
    private void removeAccount(String accountID) throws Exception {
        SipAccount account = mActiveSipAccounts.remove(accountID);

        if (account == null) {
            Logger.error(TAG, "No account for ID: " + accountID);
            return;
        }

        Logger.debug(TAG, "Removing SIP account " + accountID);
        account.delete();
        Logger.debug(TAG, "SIP account " + accountID + " successfully removed");
    }

    private void removeAllActiveAccounts() {
        if (!mActiveSipAccounts.isEmpty()) {
            for (String accountID : mActiveSipAccounts.keySet()) {
                try {
                    removeAccount(accountID);
                } catch (Exception exc) {
                    Logger.error(TAG, "Error while removing " + accountID);
                }
            }
        }
    }

    /*private void persistConfiguredAccounts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY_ACCOUNTS, new Gson().toJson(mConfiguredAccounts)).apply();
    }*/

    /**
     * Starts PJSIP Stack.
     */
    private void startStack() {

        if (mStarted) return;

        try {
            Logger.debug(TAG, "Starting PJSIP");
            mEndpoint = new Endpoint();
            mEndpoint.libCreate();

            EpConfig epConfig = new EpConfig();

            String version = "";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
            } catch (Exception ex) {
            }

            epConfig.getMedConfig().setClockRate(16000);
            epConfig.getMedConfig().setSndClockRate(0);
            epConfig.getMedConfig().setEcTailLen(256);

            epConfig.getLogConfig().setLevel(5);
            epConfig.getLogConfig().setConsoleLevel(5);

            LogConfig log_cfg = epConfig.getLogConfig();
            logWriter = new RLogWriter();
            log_cfg.setWriter(logWriter);
            log_cfg.setDecor(log_cfg.getDecor() &
                    ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() |
                            pj_log_decoration.PJ_LOG_HAS_NEWLINE.swigValue()));

            epConfig.getUaConfig().setUserAgent(String.format("%s v%s Android %s", getString(R.string.app_name), version, android.os.Build.VERSION.RELEASE));
            epConfig.getUaConfig().setMaxCalls(2);

//            epConfig.getMedConfig().setHasIoqueue(true);
//            epConfig.getMedConfig().setQuality(10);
//            epConfig.getMedConfig().setEcOptions(1);
//            epConfig.getMedConfig().setThreadCnt(2);
            mEndpoint.libInit(epConfig);

//            TransportConfig udpTransport = new TransportConfig();
//            udpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
            TransportConfig tcpTransport = new TransportConfig();
//            tcpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);

//            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, udpTransport);
//            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP, tcpTransport);
            transportID = mEndpoint.transportCreate(GlobalVars.IS_SECURED ? pjsip_transport_type_e.PJSIP_TRANSPORT_TLS : pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
                    tcpTransport);
            mEndpoint.libStart();

            CodecInfoVector codVect = mEndpoint.codecEnum();
            CodecInfo codInfo;
            String codId;
            for (int i = 0; i < codVect.capacity(); i++) {
                codInfo = codVect.get(i);
                codId = codInfo.getCodecId();
                if (codId.contains("iLBC/8000"))
                    mEndpoint.codecSetPriority(codId, (short) 150);
                else
                    mEndpoint.codecSetPriority(codId, (short) 120);
            }

            /*ArrayList<CodecPriority> codecPriorities = getConfiguredCodecPriorities();
            if (codecPriorities != null) {
                Logger.debug(TAG, "Setting saved codec priorities...");
                for (CodecPriority codecPriority : codecPriorities) {
                    Logger.debug(TAG, "Setting " + codecPriority.getCodecId() + " priority to " + codecPriority.getPriority());
                    mEndpoint.codecSetPriority(codecPriority.getCodecId(), (short) codecPriority.getPriority());
                }
                Logger.debug(TAG, "Saved codec priorities set!");
            } else {
                mEndpoint.codecSetPriority("iLBC/8000", (short) CodecPriority.PRIORITY_MAX);
                mEndpoint.codecSetPriority("PCMA/8000", (short) (CodecPriority.PRIORITY_MAX - 1));
                mEndpoint.codecSetPriority("PCMU/8000", (short) (CodecPriority.PRIORITY_MAX - 2));
//                mEndpoint.codecSetPriority("G729/8000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("speex/8000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("speex/16000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("speex/32000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("GSM/8000", (short) CodecPriority.PRIORITY_DISABLED);
                mEndpoint.codecSetPriority("G722/16000", (short) CodecPriority.PRIORITY_DISABLED);
//                mEndpoint.codecSetPriority("G7221/16000", (short) CodecPriority.PRIORITY_DISABLED);
//                mEndpoint.codecSetPriority("G7221/32000", (short) CodecPriority.PRIORITY_DISABLED);
            }*/

            Logger.debug(TAG, "PJSIP started!");
            mStarted = true;
//            mBroadcastEmitter.stackStatus(true);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while starting PJSIP", exc);
            mStarted = false;
        }
    }

    /**
     * Shuts down PJSIP Stack
     *
     * @throws Exception if an error occurs while trying to shut down the stack
     */
    private void stopStack() {

        if (!mStarted) return;

        try {
            Logger.debug(TAG, "Stopping PJSIP");

            removeAllActiveAccounts();

            // try to force GC to do its job before destroying the library, since it's
            // recommended to do that by PJSUA examples
            Runtime.getRuntime().gc();

//            mEndpoint.hangupAllCalls();
            mEndpoint.libDestroy();
            mEndpoint.delete();
            mEndpoint = null;

            Logger.debug(TAG, "PJSIP stopped");
//            mBroadcastEmitter.stackStatus(false);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while stopping PJSIP", exc);

        } finally {
            mStarted = false;
            mEndpoint = null;
        }
    }

    private ArrayList<CodecPriority> getCodecPriorityList() {
        startStack();

        if (!mStarted) {
            Logger.error(TAG, "Can't get codec priority list! The SIP Stack has not been " +
                    "initialized! Add an account first!");
            return null;
        }

        try {
            CodecInfoVector codecs = mEndpoint.codecEnum();
            if (codecs == null || codecs.size() == 0) return null;

            ArrayList<CodecPriority> codecPrioritiesList = new ArrayList<>((int) codecs.size());

            for (int i = 0; i < (int) codecs.size(); i++) {
                CodecInfo codecInfo = codecs.get(i);
                CodecPriority newCodec = new CodecPriority(codecInfo.getCodecId(),
                        codecInfo.getPriority());
                if (!codecPrioritiesList.contains(newCodec))
                    codecPrioritiesList.add(newCodec);
                codecInfo.delete();
            }

            codecs.delete();

            Collections.sort(codecPrioritiesList);
            return codecPrioritiesList;

        } catch (Exception exc) {
            Logger.error(TAG, "Error while getting codec priority list!", exc);
            return null;
        }
    }
/*
    protected synchronized void startRingtone() {
        mVibrator.vibrate(VIBRATOR_PATTERN, 0);

        try {
            //mRingTone = MediaPlayer.create(this, mRingtoneUri);
            String ringtone = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_RINGTONES);
            switch (ringtone) {
                case "0":
                    mRingTone = MediaPlayer.create(this, R.raw.ringtone);
                    break;
                case "1":
                    mRingTone = MediaPlayer.create(this, R.raw.elegant);
                    break;
                case "2":
                    mRingTone = MediaPlayer.create(this, R.raw.oldschool);
                    break;
                default:
                    mRingTone = MediaPlayer.create(this, R.raw.ringtone);

            }
            mRingTone.setLooping(true);

            int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
            mRingTone.setVolume(volume, volume);

            mRingTone.start();
        } catch (Exception exc) {
            Logger.error(TAG, "Error while trying to play ringtone!", exc);
        }
    }

    protected synchronized void stopRingtone() {
        mVibrator.cancel();

        if (mRingTone != null) {
            try {
                if (mRingTone.isPlaying())
                    mRingTone.stop();
            } catch (Exception ignored) {
            }

            try {
                mRingTone.reset();
                mRingTone.release();
            } catch (Exception ignored) {
            }
        }
    }*/

    public void handleMergeAudio(Intent intent){
        String accountID = intent.getStringExtra(PARAM_ACCOUNT_ID);
        SipAccount account = mActiveSipAccounts.get(accountID);
        if (account == null) return;

        Set<Integer> activeCallIDs = account.getCallIDs();

        if (activeCallIDs == null || activeCallIDs.isEmpty()) return;

        AudioMedia aud_med = null;

        for (int callID : activeCallIDs) {
            try {
                SipCall sipCall = getCall(accountID, callID);

                if (sipCall == null) {
                    notifyCallDisconnected(accountID, callID);
                    return;
                }

                CallInfo info;
                try {
                    info = sipCall.getInfo();
                } catch (Exception exc) {
                    Logger.error(TAG, "onCallMediaState: error while getting call info", exc);
                    return;
                }

                for (int i = 0; i < info.getMedia().size(); i++) {
                    CallMediaInfo mediaInfo = info.getMedia().get(i);
                    if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO) {
                        AudioMedia aud_med2 = AudioMedia.typecastFromMedia(sipCall.getMedia(i));
                        if(aud_med == null){
                            aud_med = aud_med2;
                        }else {
                            if (aud_med2.getPortId() != aud_med.getPortId()) {
                                aud_med.startTransmit(aud_med2);
                                aud_med2.startTransmit(aud_med);
                                Logger.info(TAG, "Merged audio");
                            }
                        }
                    }
                }
            } catch (Exception exc) {
                Logger.error(TAG, "Error while holding call", exc);
            }
        }
    }

    protected synchronized AudDevManager getAudDevManager() {
        return mEndpoint.audDevManager();
    }

//    protected BroadcastEventEmitter getBroadcastEmitter() {
//        return mBroadcastEmitter;
//    }

}
