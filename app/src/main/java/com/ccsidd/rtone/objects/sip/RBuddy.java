package com.ccsidd.rtone.objects.sip;

import com.ccsidd.rtone.SipModule;

import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.BuddyInfo;
import org.pjsip.pjsua2.pjsip_evsub_state;
import org.pjsip.pjsua2.pjsua_buddy_status;

/**
 * Created by dung on 9/8/15.
 */
public class RBuddy extends org.pjsip.pjsua2.Buddy {
    private BuddyConfig cfg;

    public BuddyConfig getCfg() {
        return cfg;
    }

    public void setCfg(BuddyConfig cfg) {
        this.cfg = cfg;
    }

    RBuddy(BuddyConfig config)
    {
        super();
        cfg = config;
    }

    String getStatusText()
    {
        BuddyInfo bi;

        try {
            bi = getInfo();
        } catch (Exception e) {
            return "?";
        }

        String status = "";
        if (bi.getSubState() == pjsip_evsub_state.PJSIP_EVSUB_STATE_ACTIVE) {
            if (bi.getPresStatus().getStatus() ==
                    pjsua_buddy_status.PJSUA_BUDDY_STATUS_ONLINE)
            {
                status = bi.getPresStatus().getStatusText();
                if (status == null || status.length()==0) {
                    status = "Online";
                }
            } else if (bi.getPresStatus().getStatus() ==
                    pjsua_buddy_status.PJSUA_BUDDY_STATUS_OFFLINE)
            {
                status = "Offline";
            } else {
                status = "Unknown";
            }
        }
        return status;
    }

    @Override
    public void onBuddyState()
    {
        if (SipModule.observerSIP != null)
            SipModule.observerSIP.notifyBuddyState(this);
    }

}
