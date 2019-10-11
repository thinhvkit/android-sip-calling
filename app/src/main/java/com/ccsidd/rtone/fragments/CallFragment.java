package com.ccsidd.rtone.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.MainActivity;
import com.ccsidd.rtone.activities.SplashScreenActivity;
import com.ccsidd.rtone.dialogs.AlertDialog;
//import com.ccsidd.rtone.gcm.QuickstartPreferences;
import com.ccsidd.rtone.listeners.AlertDialogListener;
import com.ccsidd.rtone.objects.AccountStateEvent;
import com.ccsidd.rtone.objects.CallLog;
import com.ccsidd.rtone.objects.sip.ConflictState;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.services.SipServiceCommand;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;
import com.innovattic.font.FontEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.pjsip.pjsua2.pjsip_status_code;

import io.realm.Realm;
import io.realm.RealmResults;


public class CallFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, Handler.Callback {

    private TextView tvAccount;
    private FontEditText edPhoneNumber;
    private LinearLayout lnParent;
    private LinearLayout lnNew;
    private LinearLayout lnCall;
    private ImageView imageCall;
    private BroadcastReceiver networkReceiver;
    private boolean isPressing0 = false;
    private boolean isRegistered = false;
    private BroadcastReceiver mOnScreenReceiver;
    private boolean wasScreenOn = true;

    public CallFragment() {
        // Required empty public constructor
    }

    public static CallFragment newInstance() {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountStateEvent event) {
        if (event.getRegistrationStateCode() == pjsip_status_code.PJSIP_SC_OK) {
            imageCall.setImageResource(R.drawable.call_normal);
            isRegistered = true;
        } else if (event.getRegistrationStateCode() == pjsip_status_code.PJSIP_SC_UNAUTHORIZED) {
            if (getActivity().isTaskRoot()) {
                SipServiceCommand.removeAccount(getActivity(), "sip:" + tvAccount.getText().toString() + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls");

                Intent splashScreen = new Intent(getActivity(), SplashScreenActivity.class);
                Utility.savePref(getActivity(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_CONFLICT, ConflictState.WarningNoShown.getStateString());
                getActivity().startActivity(splashScreen);
                getActivity().finish();
            }
        } else {
            isRegistered = false;
            imageCall.setImageResource(R.drawable.call_disable);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) view;
            switch (linearLayout.getTag().toString()) {
                case "delete":
                    removeNumber();
                    break;
                case "call":
                    AlertDialog alert = new AlertDialog(getActivity(), "", "");
                    if (!Utility.checkNetwork(getActivity())) {
                        alert.setTitle("Error");
                        alert.setMessage("Network is unreachable");
                        alert.setIsShowOK(true);
                        alert.setAlertDialogListener(new AlertDialogListener() {
                            @Override
                            public void btnYesPressed(AlertDialog alertDialog) {
                                alertDialog.dismiss();
                            }

                            @Override
                            public void btnNoPressed(AlertDialog alertDialog) {
                                alertDialog.dismiss();
                            }
                        });
                        alert.show();
                    } else if (!isRegistered) {
                        alert.setTitle("Information");
                        alert.setMessage("You need to re-login to call");
                        alert.setAlertDialogListener(new AlertDialogListener() {
                            @Override
                            public void btnYesPressed(AlertDialog alertDialog) {
                                SipServiceCommand.restartSipStack(getActivity());
                                alertDialog.dismiss();
                            }

                            @Override
                            public void btnNoPressed(AlertDialog alertDialog) {
                                alertDialog.dismiss();
                            }
                        });
                        alert.show();
                    } else if (edPhoneNumber.getText().length() == 0) {
                        Realm realm = Realm.getDefaultInstance();
                        RealmResults<CallLog> callLogs = realm.where(CallLog.class).greaterThan("id", -1).findAll();
                        if (callLogs.size() > 0) {
                            edPhoneNumber.setText(callLogs.last().getNumber());
                            edPhoneNumber.setSelection(edPhoneNumber.getText().length());
                        }
                        realm.close();
                    } else if (tvAccount.getText().toString().contains(edPhoneNumber.getText().toString().substring(1))) {
                        Toast.makeText(getActivity(), "Can not call yourself", Toast.LENGTH_SHORT).show();
                    } else {
                        Utility.callNumber(getActivity(), edPhoneNumber.getText().toString().trim());
                    }
                    break;
                case "new":
                    Intent editContactIntent = new Intent(Intent.ACTION_INSERT);
                    editContactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    editContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, edPhoneNumber.getText().toString().trim());
                    startActivity(editContactIntent);
                    break;
                case "0":
                    if (!isPressing0) {
                        insertNumber("0");
                    } else
                        isPressing0 = false;
                    break;
                default:
                    insertNumber(linearLayout.getTag().toString());
            }
        }
    }

    private boolean isDTMFOn() {
        String data = Utility.getPref(getActivity(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_APPLICATION_DTMF);
        if (data.isEmpty())
            data = "false";
        return Boolean.parseBoolean(data);
    }

    public void insertNumber(String number) {
        ToneGenerator toneGenerator = Utility.getTone("dtmf");
        toneGenerator.stopTone();
        if (edPhoneNumber.getSelectionStart() > -1) {
            int start = Math.max(edPhoneNumber.getSelectionStart(), 0);
            int end = Math.max(edPhoneNumber.getSelectionEnd(), 0);
            edPhoneNumber.getText().replace(Math.min(start, end), Math.max(start, end),
                    number, 0, number.length());
        } else
            edPhoneNumber.append(number);

        if (isDTMFOn()) {
            int toneTime = 200;
            switch (number) {
                case "0":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, toneTime);
                    break;
                case "1":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, toneTime);
                    break;
                case "2":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_2, toneTime);
                    break;
                case "3":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_3, toneTime);
                    break;
                case "4":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_4, toneTime);
                    break;
                case "5":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, toneTime);
                    break;
                case "6":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_6, toneTime);
                    break;
                case "7":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_7, toneTime);
                    break;
                case "8":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_8, toneTime);
                    break;
                case "9":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_9, toneTime);
                    break;
                case "*":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_S, toneTime);
                    break;
                case "#":
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_P, toneTime);
                    break;
            }
        }
    }

    public void removeNumber() {
        if (edPhoneNumber.getText().length() == 0)
            return;
        if (edPhoneNumber.getSelectionStart() > -1) {
            int start = Math.max(edPhoneNumber.getSelectionStart(), 0);
            int end = Math.max(edPhoneNumber.getSelectionEnd(), 0);
            if (start > 0 && start == end)
                start--;
            edPhoneNumber.getText().replace(Math.min(start, end), Math.max(start, end),
                    "", 0, "".length());
        } else
            edPhoneNumber.setText(edPhoneNumber.getText().subSequence(0, edPhoneNumber.getText().length() - 2));
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getTag().toString()) {
            case "delete":
                int start = 0;
                int end = Math.max(edPhoneNumber.getSelectionEnd(), 0);
                edPhoneNumber.getText().replace(Math.min(start, end), Math.max(start, end),
                        "", 0, "".length());
                break;
            case "0":
                insertNumber("+");
                isPressing0 = true;
                break;
        }
        return false;
    }

    public void changeStateButton() {
        LinearLayout lnNew = (LinearLayout) lnParent.findViewById(R.id.fm_call_lnNew);
        final LinearLayout lnDelete = (LinearLayout) lnParent.findViewById(R.id.fm_call_lnDelete);

        if (edPhoneNumber.getText().length() == 0) {
            lnNew.setEnabled(false);
            lnDelete.setEnabled(false);
            lnNew.getChildAt(0).setVisibility(View.GONE);
            lnDelete.getChildAt(0).setVisibility(View.GONE);
        } else {
            lnNew.setEnabled(true);
            lnDelete.setEnabled(true);
            lnNew.getChildAt(0).setVisibility(View.VISIBLE);
            lnDelete.getChildAt(0).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_call, container, false);
        LinearLayout linearLayout = (LinearLayout) ((FrameLayout) v).getChildAt(0);

        tvAccount = (TextView) v.findViewById(R.id.tvAccount);
        tvAccount.setText(Utility.getPref(getActivity(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));

        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            if (linearLayout.getChildAt(i) instanceof LinearLayout) {
                LinearLayout linearLayout1 = (LinearLayout) linearLayout.getChildAt(i);
                for (int j = 0; j < linearLayout1.getChildCount(); j++) {
                    if (linearLayout1.getChildAt(j).getTag() == null)
                        continue;
                    LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(j);
                    linearLayout2.setOnClickListener(this);
                    if (linearLayout2.getTag() != null && linearLayout2.getTag().toString().equals("delete")
                            || linearLayout2.getTag().toString().equals("0"))
                        linearLayout2.setOnLongClickListener(this);
                }
            }
        }

        edPhoneNumber = (FontEditText) v.findViewById(R.id.fm_call_edNumberPhone);
        if (Build.VERSION.SDK_INT >= 23)
            edPhoneNumber.setShowSoftInputOnFocus(false);
        edPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (edPhoneNumber.getText().length() == 0)
                    lnNew.setEnabled(false);
                else
                    lnNew.setEnabled(true);
            }
        });

        lnParent = (LinearLayout) v.findViewById(R.id.fm_call_ln);
        lnCall = (LinearLayout) v.findViewById(R.id.fm_call_lnCall);
        lnNew = (LinearLayout) v.findViewById(R.id.fm_call_lnNew);
        lnNew.setEnabled(false);

        for (int i = 0; i < lnCall.getChildCount(); i++) {
            if (lnCall.getChildAt(i) instanceof ImageView) {
                imageCall = (ImageView) lnCall.getChildAt(i);
                isRegistered = false;
                imageCall.setImageResource(R.drawable.call_disable);
            }
        }

        if (networkReceiver == null) {
            networkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (!Utility.checkNetwork(context)) {
                        isRegistered = false;
                        imageCall.setImageResource(R.drawable.call_disable);
                    }
                }
            };
            getActivity().registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*if(mOnScreenReceiver == null){
            mOnScreenReceiver = new BroadcastReceiver() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor edit = sharedPreferences.edit();
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                        edit.putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true);
                        edit.apply();
                        // DO WHATEVER YOU NEED TO DO HERE
                        wasScreenOn = false;
                    } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                        // AND DO WHATEVER YOU NEED TO DO HERE
                        wasScreenOn = true;
                        edit.putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true);
                        edit.apply();
                    }
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);

            getActivity().registerReceiver(mOnScreenReceiver, filter);
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(networkReceiver);
//            getActivity().unregisterReceiver(mOnScreenReceiver);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lnParent.setFocusable(true);
        lnParent.setFocusableInTouchMode(true);
        edPhoneNumber.setFocusable(false);
        edPhoneNumber.setFocusableInTouchMode(false);
        lnParent.requestFocus();
        edPhoneNumber.setInputType(InputType.TYPE_NULL);
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            edPhoneNumber.setRawInputType(InputType.TYPE_CLASS_TEXT);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                edPhoneNumber.setFocusable(true);
                edPhoneNumber.setFocusableInTouchMode(true);
                edPhoneNumber.requestFocus();
            }
        }, 700);


        EventBus.getDefault().register(this);

        PowerManager powerManager = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        wasScreenOn = (Build.VERSION.SDK_INT < 20? powerManager.isScreenOn():powerManager.isInteractive());
        Logger.info("SCREEN", wasScreenOn?"ON":"OFF");
        if(wasScreenOn) {
            SipServiceCommand.getRegistrationStatus(getActivity(), "sip:" + tvAccount.getText().toString() + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }
}
