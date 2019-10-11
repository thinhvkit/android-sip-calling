package com.ccsidd.rtone.objects.sip;

import com.ccsidd.rtone.objects.CallLog;

/**
 * Created by dung on 2/27/16.
 */
public class CallInformation {
    private RCall call;
    private CallLog callLog;
    private Long dataLength;
    private String callID;

    public RCall getCall() {
        return call;
    }

    public void setCall(RCall call) {
        this.call = call;
    }

    public CallLog getCallLog() {
        return callLog;
    }

    public void setCallLog(CallLog callLog) {
        this.callLog = callLog;
    }

    public Long getDataLength() {
        return dataLength;
    }

    public void setDataLength(Long dataLength) {
        this.dataLength = dataLength;
    }

    public String getCallID() {
        return callID;
    }

    public void setCallID(String callID) {
        this.callID = callID;
    }
}
