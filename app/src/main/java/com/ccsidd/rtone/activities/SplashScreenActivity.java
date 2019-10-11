package com.ccsidd.rtone.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.dialogs.AlertDialog;
//import com.ccsidd.rtone.gcm.RegistrationIntentService;
import com.ccsidd.rtone.listeners.AlertDialogListener;
import com.ccsidd.rtone.objects.AccountStateEvent;
import com.ccsidd.rtone.objects.Setting;
import com.ccsidd.rtone.objects.User;
import com.ccsidd.rtone.objects.sip.ConflictState;
import com.ccsidd.rtone.services.SipAccountData;
import com.ccsidd.rtone.services.SipServiceCommand;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.innovattic.font.FontEditText;
import com.innovattic.font.FontTextView;
import com.innovattic.font.TypefaceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;

enum LoginState {
    FirstLogin, Login, OTP, Data
}

public class SplashScreenActivity extends RuntimePermissionsActivity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;
    // login view component
    LinearLayout loginView;
    FontEditText edUsername;
    FontEditText edPassword;
    FontTextView btnDiffAccount;
    // otp component
    LinearLayout otpView;
    FontEditText edOTP;
    TextView tvResend;
    TextView tvBack;
    private boolean registered = false;
    private Timer timer;
    private LoginState currentState = null;
    private SipAccountData mSipAccount;
    private String mEndpoint = "";
    private ProgressDialog progress;
    private Realm realm;

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(final AccountStateEvent event) {
        if (!registered)
            return;
        registered = false;
        if (timer != null)
            timer.cancel();
        final String data = event.getRegistrationStateCode().toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progress.isShowing())
                    progress.dismiss();
                if (data.equalsIgnoreCase(pjsip_status_code.PJSIP_SC_OK.toString())) {
                    Utility.removeTrash(getApplicationContext(), edUsername.getText().toString());
                    changeState(LoginState.Data);
                    Utility.savePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_CONFLICT, ConflictState.None.toString());
                    Utility.savePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT, edUsername.getText().toString());
                    realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<User> users = realm.where(User.class).findAll();
                            User user;
                            if (users.size() > 0) {
                                user = users.first();
                                user.setUsername(edUsername.getText().toString());
                                user.setPassword(edPassword.getText().toString());
                            } else {
                                user = new User();
                                user.setUsername(edUsername.getText().toString());
                                user.setPassword(edPassword.getText().toString());
                                realm.copyToRealm(user);
                            }
                            RealmResults<Setting> settingLoginTokens = realm.where(Setting.class).equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).findAll();
                            Setting settingLoginToken;
                            if (settingLoginTokens.size() > 0) {
                                settingLoginToken = settingLoginTokens.first();
                                settingLoginToken.setKey(GlobalVars.KEY_SETTING_LOGIN_TOKEN);
                                settingLoginToken.setValue(event.getEndpoint());
                            } else {
                                settingLoginToken = new Setting();
                                settingLoginToken.setKey(GlobalVars.KEY_SETTING_LOGIN_TOKEN);
                                settingLoginToken.setValue(event.getEndpoint());
                                realm.copyToRealm(settingLoginToken);
                            }
                            RealmResults<Setting> settingTokens = realm.where(Setting.class).equalTo("key", GlobalVars.KEY_SETTING_TOKEN).findAll();
                            Setting settingToken;
                            if (settingTokens.size() > 0) {
                                if (edOTP.getText().length() > 0) {
                                    settingToken = settingTokens.first();
                                    settingToken.setKey(GlobalVars.KEY_SETTING_TOKEN);
                                    settingToken.setValue(edOTP.getText().toString());
                                }
                            } else {
                                if (edOTP.getText().length() > 0) {
                                    settingToken = new Setting();
                                    settingToken.setKey(GlobalVars.KEY_SETTING_TOKEN);
                                    settingToken.setValue(edOTP.getText().toString());
                                    realm.copyToRealm(settingToken);
                                }
                            }
                            Utility.removePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.KEY_SETTING_LOGIN_TOKEN);
                            Utility.removePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.KEY_SETTING_TOKEN);
                        }
                    });
                    realm.close();
                    LinearLayout llLogin = (LinearLayout) findViewById(R.id.slash_login_view);
                    llLogin.setVisibility(View.GONE);
                    startApplication();
                } else if (data.equalsIgnoreCase(pjsip_status_code.PJSIP_SC_UNAUTHORIZED.toString()) ||
                        data.equalsIgnoreCase(pjsip_status_code.PJSIP_SC_SERVICE_UNAVAILABLE.toString())) {
                    realm = Realm.getDefaultInstance();
                    RealmResults<Setting> settings = realm.where(Setting.class).equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).findAll();
                    if (currentState != LoginState.OTP && settings.size() > 0 && settings.first() != null
                            && settings.first().getValue() != null && settings.first().getValue().length() > 0
                            && event.getRegistrationReason().contains("Wrong Endpoint")) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                RealmResults<Setting> settings = realm.where(Setting.class).findAll();
                                settings.where().equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).findFirst().deleteFromRealm();
                                settings.where().equalTo("key", GlobalVars.KEY_SETTING_TOKEN).findFirst().deleteFromRealm();
                            }
                        });
                        changeState(LoginState.OTP);
                        registerAcc(edUsername.getText().toString(), edPassword.getText().toString(), "", "");
                        return;
                    }
                    realm.close();
                    if (event.getRegistrationReason().equalsIgnoreCase("Need OTP Token")) {
                        mEndpoint = event.getEndpoint();
                        changeState(LoginState.OTP);
                        return;
                    }
                    if (currentState == LoginState.OTP)
                        edOTP.setText("");
                    AlertDialog alertDialog = new AlertDialog(SplashScreenActivity.this, "Error", currentState == LoginState.OTP ? "The OTP is wrong" :
                            event.getRegistrationReason().contains("Unauthorized") ? "Your username/password is wrong" : event.getRegistrationReason());
                    alertDialog.setIsShowOK(true);
                    alertDialog.setCanceledOnTouchOutside(false);
                    if (data.equals(pjsip_status_code.PJSIP_SC_SERVICE_UNAVAILABLE.toString()))
                        alertDialog.setMessage(getResources().getString(R.string.login_network_unavailable));
                    alertDialog.setAlertDialogListener(new AlertDialogListener() {
                        @Override
                        public void btnYesPressed(AlertDialog alertDialog) {
                            alertDialog.dismiss();
                        }

                        @Override
                        public void btnNoPressed(AlertDialog alertDialog) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                    changeState(currentState);
                }
            }
        });
    }

    private void registerAcc(String username, String password, String endpoint, String token) {
        if (registered)
            return;
        if (!Utility.checkNetwork(this)) {
            AlertDialog alertDialog = new AlertDialog(this, "Error", "Please make sure your network is available");
            alertDialog.setIsShowOK(true);
            alertDialog.setAlertDialogListener(new AlertDialogListener() {
                @Override
                public void btnYesPressed(AlertDialog alertDialog) {
                    alertDialog.dismiss();
                }

                @Override
                public void btnNoPressed(AlertDialog alertDialog) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
            return;
        }
        if (username.isEmpty() || password.isEmpty()) {
            AlertDialog alertDialog = new AlertDialog(this, "Error", "Please enter username and password");
            alertDialog.setIsShowOK(true);
            alertDialog.setAlertDialogListener(new AlertDialogListener() {
                @Override
                public void btnYesPressed(AlertDialog alertDialog) {
                    alertDialog.dismiss();
                    registered = false;
                }

                @Override
                public void btnNoPressed(AlertDialog alertDialog) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
            return;
        }
        registered = true;
        if (timer != null)
            timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog(SplashScreenActivity.this, "Error", "Time out. Please try again");
                        alertDialog.setIsShowOK(true);
                        alertDialog.setAlertDialogListener(new AlertDialogListener() {
                            @Override
                            public void btnYesPressed(AlertDialog alertDialog) {
                                alertDialog.dismiss();
                            }

                            @Override
                            public void btnNoPressed(AlertDialog alertDialog) {
                                alertDialog.dismiss();
                            }
                        });
                        registered = false;
                        alertDialog.show();
                        progress.dismiss();
                    }
                });
                this.cancel();
            }
        }, 10000);
        progress.show();
        Utility.configureRealm(getApplicationContext(), username);
        realm = Realm.getDefaultInstance();
        RealmResults<Setting> settings = realm.where(Setting.class).findAll();
        if (token == null || token.length() == 0) {
            if (settings.where().equalTo("key", GlobalVars.KEY_SETTING_TOKEN).count() > 0) {
                token = settings.where().equalTo("key", GlobalVars.KEY_SETTING_TOKEN).findFirst().getValue();
            }
        }
        if (endpoint == null || endpoint.length() == 0) {
            if (settings.where().equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).count() > 0) {
                endpoint = settings.where().equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).findFirst().getValue();
            }
        }
        realm.close();
        mSipAccount.setHost("rtone.ccsidd.com")
                .setPort(5061)
                .setTcpTransport(true)
                .setUsername(username)
                .setPassword(password)
                .setToken(token)
                .setEndpoint(endpoint)
                .setRealm(GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls");
        SipServiceCommand.setAccount(this, mSipAccount);
        SipServiceCommand.getCodecPriorities(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        TypefaceManager.initialize(getApplicationContext(), R.xml.fonts);
        setContentView(R.layout.activity_splash);

        /*if (Utility.checkPlayServices(this)) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }*/

        if (progress == null) {
            progress = new ProgressDialog(this, R.style.StyledDialog);
            progress.setCancelable(false);
            progress.setMessage("");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
        }
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int buttonSize = width * 3 / 4;
        otpView = (LinearLayout) findViewById(R.id.splash_otp_view);
        edOTP = (FontEditText) findViewById(R.id.otp_ed_otp);
        edOTP.setWidth(buttonSize);
        edOTP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    registerAcc(edUsername.getText().toString(), edPassword.getText().toString(), mEndpoint, edOTP.getText().toString());
                }
                return false;
            }
        });
        Button btnSubmit = (Button) findViewById(R.id.otp_btn_submit);
        btnSubmit.setWidth(buttonSize);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAcc(edUsername.getText().toString(), edPassword.getText().toString(), mEndpoint, edOTP.getText().toString());
            }
        });
        tvBack = (TextView) findViewById(R.id.otp_tv_back);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edOTP.setText("");
                edUsername.setText("");
                edPassword.setText("");
                changeState(LoginState.FirstLogin);
            }
        });
        tvResend = (TextView) findViewById(R.id.otp_tv_resend);
        tvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAcc(edUsername.getText().toString(), edPassword.getText().toString(), "", "");
            }
        });
        loginView = (LinearLayout) findViewById(R.id.slash_login_view);
        edUsername = (FontEditText) findViewById(R.id.login_ed_username);
        edPassword = (FontEditText) findViewById(R.id.login_ed_password);
        btnDiffAccount = (FontTextView) findViewById(R.id.login_btn_diff_acc);
        btnDiffAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edOTP.setText("");
                edUsername.setText("");
                edPassword.setText("");
                edUsername.requestFocus();
                String defaultAccount = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
                SipServiceCommand.removeAccount(SplashScreenActivity.this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls");
                changeState(LoginState.FirstLogin);
            }
        });
        Button btnLogin = (Button) findViewById(R.id.login_btn_login);
        btnLogin.setWidth(buttonSize);
        edUsername.setWidth(buttonSize);
        edPassword.setWidth(buttonSize);
        edPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    registerAcc(edUsername.getText().toString(), edPassword.getText().toString(), "", "");
                }
                return false;
            }
        });
        changeState(LoginState.FirstLogin);
        String defaultAccount = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
        if (defaultAccount == null || defaultAccount.length() == 0) {
            edUsername.setText("");
            edPassword.setText("");
            changeState(LoginState.FirstLogin);
        } else {
            Utility.configureRealm(getApplicationContext(), defaultAccount);
            changeState(LoginState.Data);
            new CheckApplicationState().execute();
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAcc(edUsername.getText().toString(), edPassword.getText().toString(), "", "");
            }
        });
        mSipAccount = new SipAccountData();
        SplashScreenActivity.super.requestAppPermissions(new String[]{
                        Manifest.permission.READ_PHONE_STATE
                }, R.string.runtime_permissions_txt
                , REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        //
    }

    private void changeState(LoginState state) {
        if (state != LoginState.Data) {
            currentState = state;
        }
        switch (state) {
            case FirstLogin:
                otpView.setVisibility(View.INVISIBLE);
                loginView.setVisibility(View.VISIBLE);
                btnDiffAccount.setVisibility(View.INVISIBLE);
                edUsername.setEnabled(true);
                break;
            case Login:
                otpView.setVisibility(View.INVISIBLE);
                loginView.setVisibility(View.VISIBLE);
                btnDiffAccount.setVisibility(View.VISIBLE);
                edUsername.setEnabled(false);
                break;
            case OTP:
                otpView.setVisibility(View.VISIBLE);
                loginView.setVisibility(View.INVISIBLE);
                edOTP.requestFocus();
                break;
            case Data:
                otpView.setVisibility(View.INVISIBLE);
                loginView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void startApplication() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, 0);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null)
            timer.cancel();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private class CheckApplicationState extends AsyncTask<Void, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(Void... voids) {
            LoginState applicationState = LoginState.Data;
            ArrayList<Object> result = new ArrayList<>();
            realm = Realm.getDefaultInstance();
            RealmResults<User> users = realm.where(User.class).findAll();
            if (users.size() > 0) {
                result.add(users.first().getUsername());
                RealmResults<Setting> settings = realm.where(Setting.class).equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).findAll();
                if (settings.size() == 0) {
                    applicationState = LoginState.FirstLogin;
                    result.add(ConflictState.None);
                } else {
                    Setting loginTokenSetting = settings.first();
                    String stateString = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_CONFLICT);
                    ConflictState state;
                    if (stateString.length() > 0) {
                        state = ConflictState.fromString(stateString);
                    } else {
                        state = ConflictState.None;
                    }
                    result.add(state);
                    if (loginTokenSetting.getValue() != null && loginTokenSetting.getValue().length() == 0) {
                        if (state == ConflictState.WarningNoShown) {
                            //
                        }
                        applicationState = LoginState.Login;
                    } else if (users.first().getPassword().length() > 0) {
                        applicationState = LoginState.Data;
                    } else {
                        applicationState = LoginState.Login;
                    }
                }
            }
            if (result.size() > 1) {
                result.add(applicationState);
            }
            realm.close();
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> objects) {
            super.onPostExecute(objects);
            if (objects.size() == 0) {
                changeState(LoginState.FirstLogin);
                return;
            }
            LoginState state = (LoginState) objects.get(2);
            ConflictState conflictState = (ConflictState) objects.get(1);
            switch (conflictState) {
                case WarningNoShown:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alert = new AlertDialog(SplashScreenActivity.this, "Warning", "Your account logged at other device or your password changed");
                            alert.setCanceledOnTouchOutside(false);
                            alert.setIsShowOK(true);
                            alert.setCanceledOnTouchOutside(false);
                            alert.setAlertDialogListener(new AlertDialogListener() {
                                @Override
                                public void btnYesPressed(AlertDialog alertDialog) {
                                    alertDialog.dismiss();
                                    Utility.savePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_CONFLICT, ConflictState.WarningShown.toString());
                                }

                                @Override
                                public void btnNoPressed(AlertDialog alertDialog) {
                                    alertDialog.dismiss();
                                }
                            });
                            alert.show();
                        }
                    });
                    break;
            }
            changeState(state);
            switch (state) {
                case FirstLogin:
                    edUsername.setText("");
                    break;
                case Login:
                    edUsername.setText((String) objects.get(0));
                    break;
                case Data:
                    if (conflictState == ConflictState.None)
                        startApplication();
                    else {
                        changeState(LoginState.Login);
                        edUsername.setText((String) objects.get(0));
                    }
                    break;
            }
        }
    }
}