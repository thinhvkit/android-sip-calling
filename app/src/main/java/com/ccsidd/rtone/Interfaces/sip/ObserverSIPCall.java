package com.ccsidd.rtone.Interfaces.sip;

import com.ccsidd.rtone.objects.sip.RCall;

public interface ObserverSIPCall
{
    void notifyCallState(RCall call);

    void notifyCallMediaState(RCall call);
}
