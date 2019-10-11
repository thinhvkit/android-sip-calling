package com.ccsidd.rtone.objects.sip;

import org.pjsip.pjsua2.AccountConfig;

/**
 * Created by dung on 1/25/16.
 */
public class RAccountConfig extends AccountConfig{
    private String endpoint = "";
    private String token = "";
    private String pushToken = "";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
}
