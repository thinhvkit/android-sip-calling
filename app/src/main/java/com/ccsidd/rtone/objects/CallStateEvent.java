package com.ccsidd.rtone.objects;

import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

/**
 * Created by thinhvo on 9/28/16.
 */

public class CallStateEvent {
    private String accountID;
    private int callID;
    private pjsip_inv_state callStateCode;
    private pjsip_status_code lastStatusCode;
    private long connectTimestamp;
    private boolean isLocalHold;
    private boolean isLocalMute;
    private String data;

    public CallStateEvent(String accountID, int callID, pjsip_inv_state callStateCode, pjsip_status_code lastStatusCode, long connectTimestamp, boolean isLocalHold, boolean isLocalMute, String data) {
        this.accountID = accountID;
        this.callID = callID;
        this.callStateCode = callStateCode;
        this.lastStatusCode = lastStatusCode;
        this.connectTimestamp = connectTimestamp;
        this.isLocalHold = isLocalHold;
        this.isLocalMute = isLocalMute;
        this.data = data;
    }

    public String getAccountID() {
        return accountID;
    }

    public int getCallID() {
        return callID;
    }

    public pjsip_inv_state getCallStateCode() {
        return callStateCode;
    }

    public pjsip_status_code getLastStatusCode() {
        return lastStatusCode;
    }

    public long getConnectTimestamp() {
        return connectTimestamp;
    }

    public boolean isLocalHold() {
        return isLocalHold;
    }

    public boolean isLocalMute() {
        return isLocalMute;
    }

    public String getData() {
        return data;
    }
}
