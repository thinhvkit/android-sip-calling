package com.ccsidd.rtone.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

//import com.ccsidd.rtone.gcm.QuickstartPreferences;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.google.firebase.iid.FirebaseInstanceId;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AccountMediaConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.SipHeader;
import org.pjsip.pjsua2.pjmedia_srtp_use;

/**
 * Contains the account's configuration data.
 *
 * @author gotev (Aleksandar Gotev)
 */
public class SipAccountData implements Parcelable {

    public static final String AUTH_TYPE_DIGEST = "digest";
    public static final String AUTH_TYPE_PLAIN = "plain";
    // This is used to regenerate the object.
    // All Parcelables must have a CREATOR that implements these two methods
    public static final Creator<SipAccountData> CREATOR =
            new Creator<SipAccountData>() {
                @Override
                public SipAccountData createFromParcel(final Parcel in) {
                    return new SipAccountData(in);
                }

                @Override
                public SipAccountData[] newArray(final int size) {
                    return new SipAccountData[size];
                }
            };
    private String username;
    private String password;
    private String token;
    private String endpoint;
    private String realm;
    private String host;
    private long port = 5060;
    private boolean tcpTransport = false;
    private String authenticationType = AUTH_TYPE_DIGEST;

    public SipAccountData() {
    }

    private SipAccountData(Parcel in) {
        username = in.readString();
        password = in.readString();
        token = in.readString();
        endpoint = in.readString();
        realm = in.readString();
        host = in.readString();
        port = in.readLong();
        tcpTransport = in.readByte() == 1;
        authenticationType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(token);
        parcel.writeString(endpoint);
        parcel.writeString(realm);
        parcel.writeString(host);
        parcel.writeLong(port);
        parcel.writeByte((byte) (tcpTransport ? 1 : 0));
        parcel.writeString(authenticationType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUsername() {
        return username;
    }

    public SipAccountData setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SipAccountData setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getToken() {
        return token;
    }

    public SipAccountData setToken(String token) {
        this.token = token;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public SipAccountData setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public SipAccountData setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public String getHost() {
        return host;
    }

    public SipAccountData setHost(String host) {
        this.host = host;
        return this;
    }

    public long getPort() {
        return port;
    }

    public SipAccountData setPort(long port) {
        this.port = port;
        return this;
    }

    public boolean isTcpTransport() {
        return tcpTransport;
    }

    public SipAccountData setTcpTransport(boolean tcpTransport) {
        this.tcpTransport = tcpTransport;
        return this;
    }

    public String getIdUri() {
        if ("*".equals(realm))
            return "sip:" + username;

        return "sip:" + username + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls";
    }

    public String getRegistrarUri() {
        return "sip:" + host + ":" + port;
    }

    public String getProxyUri() {
        StringBuilder proxyUri = new StringBuilder();

        proxyUri.append("sip:").append(host).append(":").append(port);

        if (tcpTransport) {
            proxyUri.append(";transport=tls");
        }

        return proxyUri.toString();
    }

    public boolean isValid() {
        return ((username != null) && !username.isEmpty()
                && (password != null) && !password.isEmpty()
                && (token != null) && !token.isEmpty()
                && (endpoint != null) && !endpoint.isEmpty()
                && (host != null) && !host.isEmpty()
                && (realm != null) && !realm.isEmpty());
    }

    protected AccountConfig getAccountConfig(Context context) {
        String pushToken;
//        SharedPreferences sharedPreferences =
//                PreferenceManager.getDefaultSharedPreferences(context);
//        pushToken = sharedPreferences
//                .getString(QuickstartPreferences.PUSH_TOKEN_TO_SERVER, "");
//        SharedPreferences sharedPreferences =
//                PreferenceManager.getDefaultSharedPreferences(context);
//        pushToken = sharedPreferences
//                .getString(GlobalVars.KEY_SETTING_TOKEN, "");
        pushToken = FirebaseInstanceId.getInstance().getToken();

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceID = telephonyManager.getDeviceId();

        Logger.debug("SipAccountData", "Register: " + username + "; " + password + "; " + token + "\n" + pushToken + "\n" + endpoint + "\n" + deviceID);

        AccountConfig accountConfig = new AccountConfig();
//        accountConfig.getMediaConfig().getTransportConfig().setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
//        accountConfig.setIdUri(getIdUri());
//        accountConfig.getRegConfig().setRegistrarUri(getRegistrarUri());
//
//        AuthCredInfo cred = new AuthCredInfo(authenticationType, getRealm(),
//                                             getUsername(), 0, getPassword());
//        accountConfig.getSipConfig().getAuthCreds().add(cred);
//        accountConfig.getSipConfig().getProxies().add(getProxyUri());

        //
        String endpointString;
        if (endpoint == null || endpoint.length() == 0) {
            endpointString = String.format("mobile;%s Android %s;%s;%s",
                    Build.MODEL,
                    Build.VERSION.RELEASE,
                    pushToken.equalsIgnoreCase("") ? "11111" : pushToken,
                    deviceID);
        } else {
            endpointString = endpoint;
        }

        String domain = GlobalVars.IS_SECURED ? GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls" : GlobalVars.SERVER_IP_ADDRESS_UDP;
        SipHeader hdrEndpoint = new SipHeader();
        hdrEndpoint.setHName("Endpoint");
        hdrEndpoint.setHValue(endpointString);
        accountConfig.getRegConfig().getHeaders().add(hdrEndpoint);

        accountConfig.getRegConfig().setRegisterOnAdd(false);
        accountConfig.getRegConfig().setTimeoutSec(60);
        accountConfig.getSipConfig().getProxies().clear();
        accountConfig.getRegConfig().setRegistrarUri(String.format("sip:%s", domain));

        accountConfig.setIdUri(String.format("sip:%s@%s", username, domain));
        accountConfig.getSipConfig().getAuthCreds().clear();
        accountConfig.getSipConfig().getAuthCreds().add(new AuthCredInfo("Digest", "*",
                String.format("%s|%s", username, token), 0, password));
        AccountMediaConfig accountMediaConfig = new AccountMediaConfig();
        accountMediaConfig.setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL);
        accountConfig.setMediaConfig(accountMediaConfig);

        return accountConfig;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public SipAccountData setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SipAccountData that = (SipAccountData) o;

        return getIdUri().equals(that.getIdUri());

    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + token.hashCode();
        result = 31 * result + endpoint.hashCode();
        result = 31 * result + realm.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + (int) (port ^ (port >>> 32));
        result = 31 * result + (tcpTransport ? 1 : 0);
        return result;
    }
}

