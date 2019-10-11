package com.ccsidd.rtone.Interfaces.sip;

import com.ccsidd.rtone.objects.sip.RBuddy;
import com.ccsidd.rtone.objects.sip.RCall;

/**
 * Created by dung on 9/10/15.
 */
public interface ObserverSIPManagement {
    void notifyIncomingCall(RCall call);

    void notifyCallState(RCall call);

    void notifyCallMediaState(RCall call);

    void notifyBuddyState(RBuddy buddy);
}
