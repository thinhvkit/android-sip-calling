package com.ccsidd.rtone.Interfaces.sip;

import com.ccsidd.rtone.objects.sip.RCall;

import org.pjsip.pjsua2.pjsip_status_code;

public interface ObserverSIPAccount
{
    void notifyRegState(pjsip_status_code code, String reason, int expiration);

    void notifyIncomingCall(RCall call);
}
