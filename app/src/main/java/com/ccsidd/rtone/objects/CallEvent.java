package com.ccsidd.rtone.objects;

/**
 * Created by thinhvo on 9/28/16.
 */

public class CallEvent {

    private String accountID;
    private int callID;
    private String phone;

    public CallEvent(String accountID, int callID, String phone) {
        this.accountID = accountID;
        this.callID = callID;
        this.phone = phone;
    }

    public String getAccountID() {
        return accountID;
    }

    public int getCallID() {
        return callID;
    }

    public String getPhone() {
        return phone;
    }
}
