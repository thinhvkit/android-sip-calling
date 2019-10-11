package com.ccsidd.rtone;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ccsidd.rtone.Interfaces.sip.ObserverSIP;
//import com.ccsidd.rtone.gcm.QuickstartPreferences;
import com.ccsidd.rtone.objects.Setting;
import com.ccsidd.rtone.objects.sip.RAccount;
import com.ccsidd.rtone.objects.sip.RAccountConfig;
import com.ccsidd.rtone.objects.sip.RLogWriter;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;

import org.pjsip.pjsua2.CodecInfo;
import org.pjsip.pjsua2.CodecInfoVector;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.PresenceStatus;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pjsip_transport_type_e;
import org.pjsip.pjsua2.pjsua_buddy_status;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by dung on 9/9/15.
 */
public class SipModule {

    public static Endpoint ep = new Endpoint();
    public static ObserverSIP observerSIP;
    // set param
    private final int LOG_LEVEL = 7;
    private Context context;
    private int TransportUDP;
    private RLogWriter logWriter;
    private TelephonyManager telephonyManager;
    private EpConfig epConfig = new EpConfig();
    private TransportConfig sipTpConfig = new TransportConfig();
    private RAccount account;

    public SipModule(Context context) {
        this.context = context;
    }

    public void init(ObserverSIP obs) {
        init(obs, false);
    }

    public void init(ObserverSIP obs, boolean own_worker_thread) {
        observerSIP = obs;

	/* Create endpoint */
        try {
            ep.libCreate();
        } catch (Exception e) {
            return;
        }

	/* Set ua config. */
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        } catch (Exception ex) {
        }

        epConfig.getMedConfig().setClockRate(16000);
        epConfig.getMedConfig().setSndClockRate(0);
        epConfig.getMedConfig().setEcTailLen(256);

//        epConfig.getLogConfig().setFilename(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/logFile.log");
//        epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);
//        epConfig.getLogConfig().setLevel(LOG_LEVEL);
//        epConfig.getLogConfig().setMsgLogging(0);

        epConfig.getLogConfig().setLevel(LOG_LEVEL);
        epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);
//        epConfig.getLogConfig().setMsgLogging(1);

        LogConfig log_cfg = epConfig.getLogConfig();
        logWriter = new RLogWriter();
        log_cfg.setWriter(logWriter);
        log_cfg.setDecor(log_cfg.getDecor() &
                ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() |
                        pj_log_decoration.PJ_LOG_HAS_NEWLINE.swigValue()));

        epConfig.getUaConfig().setUserAgent(String.format("%s v%s Android %s", context.getString(R.string.app_name), version, android.os.Build.VERSION.RELEASE));
//        StringVector stun_servers = new StringVector();
//        stun_servers.add("stun.pjsip.org");
//        ua_cfg.setStunServer(stun_servers);
        if (own_worker_thread) {
            epConfig.getUaConfig().setThreadCnt(0);
            epConfig.getUaConfig().setMainThreadOnly(true);
        }

	/* Create transports. */
        try {
            TransportUDP = ep.transportCreate(GlobalVars.IS_SECURED ? pjsip_transport_type_e.PJSIP_TRANSPORT_TLS : pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
                    sipTpConfig);


        } catch (Exception e) {
            e.printStackTrace();
        }

	/* Init endpoint */
        try {
            ep.libInit(epConfig);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

//
//        try {
//            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
//                    sipTpConfig);
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//        try {
//            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS,
//                    sipTpConfig);
//        } catch (Exception e) {
//            System.out.println(e);
//        }


        // config account




	/* Start. */
        try {
            ep.libStart();
        } catch (Exception e) {
        }
    }

    public void createAcc(String username, String password, String endpoint, String token) {
        try {
            if (account != null) {
                Log.i("com.ccsidd.rtone", "delete account");
                account.delete();
                account = null;
            }

            Realm realm = Realm.getDefaultInstance();
            RealmResults<Setting> settings = realm.where(Setting.class).findAll();

            if (token != null && token.length() == 0) {
                if (settings.where().equalTo("key", GlobalVars.KEY_SETTING_TOKEN).count() > 0) {
                    token = settings.where().equalTo("key", GlobalVars.KEY_SETTING_TOKEN).findFirst().getValue();
                }
            }
            if (endpoint == null || endpoint.length() == 0) {
                if (settings.where().equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).count() > 0) {
                    endpoint = settings.where().equalTo("key", GlobalVars.KEY_SETTING_LOGIN_TOKEN).findFirst().getValue();
                } else {
                    endpoint = "";
                }
            }

            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
            String pushToken = "";

            Log.i("com.ccsidd.rtone", "Register: " + username + "; " + password + "; " + token + "\n" + pushToken + "\n" + endpoint + "\n" + telephonyManager.getDeviceId());
            RAccountConfig config = new RAccountConfig();
            config.setEndpoint(endpoint);
            config.setToken(token);
            config.setPushToken(pushToken.equalsIgnoreCase("") ? "11111" : pushToken);
            account = new RAccount(username, password);
            account.configure(config, telephonyManager.getDeviceId());

            account.setRegistration(true);
            PresenceStatus presenceStatus = new PresenceStatus();
            presenceStatus.setStatus(pjsua_buddy_status.PJSUA_BUDDY_STATUS_ONLINE);
            account.setOnlineStatus(presenceStatus);
            setCodec();

            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            account = null;
        }
    }

    public boolean isRegistered() {
        try {
            if (account == null)
                return false;
            else
                return account.getInfo().getRegIsActive();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getAccountStatus() {
        try {
            if (account == null)
                return 0;
            else
                return account.getInfo().getRegStatus().swigValue();
        } catch (Exception exception) {
            return 0;
        }
    }

    public void unregister() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("com.ccsidd.rtone", "Unregister");
                    if (account != null && account.getInfo().getRegIsActive()) {
                        PresenceStatus presenceStatus = new PresenceStatus();
                        presenceStatus.setStatus(pjsua_buddy_status.PJSUA_BUDDY_STATUS_OFFLINE);
                        account.setOnlineStatus(presenceStatus);
                        account.setRegistration(false);
                        account.delete();
                        account = null;
                    } else {
                        if (account != null) {
                            account.delete();
                            account = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } // This is your code
        };
        Utility.runOnMainThread(context, runnable);
    }

    public RAccount getAccount() {
        return account;
    }

    public void setCodec() {
        try {
            CodecInfoVector codVect = ep.codecEnum();
            CodecInfo codInfo;
            String codId;
            for (int i = 0; i < codVect.capacity(); i++) {
                codInfo = codVect.get(i);
                codId = codInfo.getCodecId();
                if (codId.contains("iLBC/8000"))
                    ep.codecSetPriority(codId, (short) 150);
                else
                    ep.codecSetPriority(codId, (short) 120);
            }

//            ep.audDevManager().setEcOptions(0, 0); default 800ms
//            ep.audDevManager().setInputLatency(0,true);
//            ep.audDevManager().setOutputLatency(0, true);
//
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deInit() {
    /* Try force GC to avoid late destroy of PJ objects as they should be
    * deleted before lib is destroyed.
	*/
        Runtime.getRuntime().gc();

	/* Shutdown pjsua. Note that Endpoint destructor will also invoke
    * libDestroy(), so this will be a test of double libDestroy().
	*/
        try {
            ep.libDestroy();
        } catch (Exception e) {
        }

	/* Force delete Endpoint here, to avoid deletion from a non-
	* registered thread (by GC?).
	*/
        ep.delete();
        ep = null;
    }
}

