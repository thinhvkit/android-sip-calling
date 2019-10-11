package com.ccsidd.rtone.objects.sip;

import android.os.Build;
import android.util.Log;

import com.ccsidd.rtone.SipModule;
import com.ccsidd.rtone.utilities.GlobalVars;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountMediaConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.SipHeader;
import org.pjsip.pjsua2.pjmedia_srtp_use;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dung on 9/8/15.
 */
public class RAccount extends Account
{
    private ArrayList<RBuddy> buddyList = new ArrayList<>();
    private RAccountConfig cfg;
//    private TransportConfig transportConfig;
    private AccountMediaConfig accMediaConfig = new AccountMediaConfig();
    private int lastStateCode = -1;
    private String endpoint;
    private String username, password;

    public RAccount(String username, String password) throws Exception {
        super();
        this.username = username;
        this.password = password;

    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public RAccountConfig getCfg() {
        return cfg;
    }

    public void configure(RAccountConfig config, String deviceID)
    {
        cfg = config;
        String endpointString;
        if (cfg.getEndpoint().length() == 0)
        {
            endpointString = String.format("mobile;%s Android %s;%s;%s", Build.MODEL, Build.VERSION.RELEASE, cfg.getPushToken(), deviceID);
        }
        else
        {
            endpointString = cfg.getEndpoint();
        }

        String domain = GlobalVars.IS_SECURED ? GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls" : GlobalVars.SERVER_IP_ADDRESS_UDP;
        SipHeader hdrEndpoint = new SipHeader();
        hdrEndpoint.setHName("Endpoint");
        hdrEndpoint.setHValue(endpointString);
        cfg.getRegConfig().getHeaders().add(hdrEndpoint);

        cfg.getRegConfig().setRegisterOnAdd(false);
        cfg.getRegConfig().setTimeoutSec(60);
        cfg.getSipConfig().getProxies().clear();
        cfg.getRegConfig().setRegistrarUri(String.format("sip:%s", domain));

        cfg.setIdUri(String.format("sip:%s@%s", username, domain));
        cfg.getSipConfig().getAuthCreds().clear();
        cfg.getSipConfig().getAuthCreds().add(new AuthCredInfo("Digest", "*", String.format("%s|%s", username, cfg.getToken()), 0,
                password));
        accMediaConfig.setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL);
        cfg.setMediaConfig(accMediaConfig);
        try {
            create(cfg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RBuddy addBuddy(BuddyConfig bud_cfg)
    {
	/* Create Buddy */
        RBuddy bud = new RBuddy(bud_cfg);
        try {
            bud.create(this, bud_cfg);
        } catch (Exception e) {
            bud.delete();
            bud = null;
        }

        if (bud != null) {
            buddyList.add(bud);
            if (bud_cfg.getSubscribe())
                try {
                    bud.subscribePresence(true);
                } catch (Exception e) {}
        }

        return bud;
    }

    public void delBuddy(RBuddy buddy)
    {
        buddyList.remove(buddy);
        buddy.delete();
    }

    public void delBuddy(int index)
    {
        RBuddy bud = buddyList.get(index);
        buddyList.remove(index);
        bud.delete();
    }

    @Override
    public void onRegState(OnRegStateParam prm)
    {
        endpoint = null;
        if (!prm.getReason().contains("CCS") && prm.getRdata() != null && !prm.getReason().contains("Wrong Endpoint"))
        {
            if (prm.getRdata().getWholeMsg().contains("Endpoint")) {

                String pa = "Endpoint:\\s*(.*)";
                Log.d("Endoind",prm.getRdata().getWholeMsg());
                Pattern pattern = Pattern.compile(pa);
                Matcher matcher = pattern.matcher(prm.getRdata().getWholeMsg());
                matcher.find();
                endpoint = matcher.group(1);
            }
        }

        if ((SipModule.observerSIP != null && lastStateCode != prm.getCode().swigValue()) || lastStateCode < 0)
        {
            SipModule.observerSIP.notifyRegState(prm.getCode(), prm.getReason(),
                    prm.getExpiration());
            lastStateCode = prm.getCode().swigValue();
        }
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm)
    {
//         System.out.println("======== Incoming call ======== ");
        RCall call = new RCall(this, prm.getCallId());
        if (SipModule.observerSIP != null)
            SipModule.observerSIP.notifyIncomingCall(call);
    }

    @Override
    public void onInstantMessage(OnInstantMessageParam prm)
    {
        System.out.println("======== Incoming pager ======== ");
        System.out.println("From     : " + prm.getFromUri());
        System.out.println("To       : " + prm.getToUri());
        System.out.println("Contact  : " + prm.getContactUri());
        System.out.println("Mimetype : " + prm.getContentType());
        System.out.println("Body     : " + prm.getMsgBody());
    }
}