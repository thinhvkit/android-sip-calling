package com.ccsidd.rtone.Interfaces.sip;

import com.ccsidd.rtone.objects.sip.RBuddy;
import com.ccsidd.rtone.objects.sip.RCall;

import org.pjsip.pjsua2.pjsip_status_code;

/**
 * Created by dung on 9/9/15.
 */
public interface ObserverSIP {
    void notifyRegState(pjsip_status_code code, String reason,
                        int expiration);

    void notifyIncomingCall(RCall call);

    void notifyCallState(RCall call);

    void notifyCallMediaState(RCall call);

    void notifyBuddyState(RBuddy buddy);

    void notifyRemoteHold(String holdReason);

//    abstract void updateCallLog(int missCall);

    void setDataLengh(String callID, int length);
}
