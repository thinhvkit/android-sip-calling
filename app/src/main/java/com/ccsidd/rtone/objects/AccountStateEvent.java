package com.ccsidd.rtone.objects;

import org.pjsip.pjsua2.pjsip_status_code;

/**
 * Created by thinhvo on 9/29/16.
 */

public class AccountStateEvent {

    private String accountID;
    private pjsip_status_code registrationStateCode;
    private String registrationReason;
    private String endpoint;

    public AccountStateEvent(String accountID, pjsip_status_code registrationStateCode, String registrationReason, String endpoint) {
        this.accountID = accountID;
        this.registrationStateCode = registrationStateCode;
        this.registrationReason = registrationReason;
        this.endpoint = endpoint;
    }

    public String getAccountID() {
        return accountID;
    }

    public pjsip_status_code getRegistrationStateCode() {
        return registrationStateCode;
    }

    public String getRegistrationReason() {
        return registrationReason;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
