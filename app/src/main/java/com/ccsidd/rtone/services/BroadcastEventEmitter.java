package com.ccsidd.rtone.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

/**
 * Emits the sip service broadcast intents.
 * @author gotev (Aleksandar Gotev)
 */
public class BroadcastEventEmitter {

    public static String NAMESPACE = "com.ccsidd.rtone";

    private Context mContext;

    /**
     * Enumeration of the broadcast actions
     */
    public enum BroadcastAction {
        REGISTRATION,
        INCOMING_CALL,
        CALL_STATE,
        OUTGOING_CALL,
        STACK_STATUS,
        CODEC_PRIORITIES,
        CODEC_PRIORITIES_SET_STATUS
    }

    /**
     * Parameters passed in the broadcast intents.
     */
    public class BroadcastParameters {
        public static final String ACCOUNT_ID = "account_id";
        public static final String CALL_ID = "call_id";
        public static final String CODE = "code";
        public static final String REASON = "reason";
        public static final String ENPOINT = "endpoint";
        public static final String REMOTE_URI = "remote_uri";
        public static final String DISPLAY_NAME = "display_name";
        public static final String CALL_STATE = "call_state";
        public static final String LAST_STATUS_CODE = "last_status_code";
        public static final String NUMBER = "number";
        public static final String CONNECT_TIMESTAMP = "connectTimestamp";
        public static final String STACK_STARTED = "stack_started";
        public static final String CODEC_PRIORITIES_LIST = "codec_priorities_list";
        public static final String LOCAL_HOLD = "local_hold";
        public static final String LOCAL_MUTE = "local_mute";
        public static final String DATA_TOTAL = "data_total";
        public static final String SUCCESS = "success";
    }

    public BroadcastEventEmitter(Context context) {
        mContext = context;
    }

    public static String getAction(BroadcastAction action) {
        return NAMESPACE + "." + action;
    }

    /**
     * Emit an incoming call broadcast intent.
     * @param accountID call's account IdUri
     * @param callID call ID number
     * @param displayName the display name of the remote party
     * @param remoteUri the IdUri of the remote party
     */
    public void incomingCall(String accountID, int callID, String displayName, String remoteUri) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.INCOMING_CALL));
        intent.putExtra(BroadcastParameters.ACCOUNT_ID, accountID);
        intent.putExtra(BroadcastParameters.CALL_ID, callID);
        intent.putExtra(BroadcastParameters.DISPLAY_NAME, displayName);
        intent.putExtra(BroadcastParameters.REMOTE_URI, remoteUri);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * Emit a registration state broadcast intent.
     * @param accountID account IdUri
     * @param registrationStateCode SIP registration status code
     * @param registrationReason SIP registration reason
     */
    public void registrationState(String accountID, int registrationStateCode, String registrationReason, String endpoint) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.REGISTRATION));
        intent.putExtra(BroadcastParameters.ACCOUNT_ID, accountID);
        intent.putExtra(BroadcastParameters.CODE, registrationStateCode);
        intent.putExtra(BroadcastParameters.REASON, registrationReason);
        intent.putExtra(BroadcastParameters.ENPOINT, endpoint);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * Emit a call state broadcast intent.
     * @param accountID call's account IdUri
     * @param callID call ID number
     * @param callStateCode SIP call state code
     * @param connectTimestamp call start timestamp
     * @param isLocalHold true if the call is held locally
     * @param isLocalMute true if the call is muted locally
     */
    public void callState(String accountID, int callID, int callStateCode, int lastStatusCode, long connectTimestamp,
                          boolean isLocalHold, boolean isLocalMute, String data) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.CALL_STATE));
        intent.putExtra(BroadcastParameters.ACCOUNT_ID, accountID);
        intent.putExtra(BroadcastParameters.CALL_ID, callID);
        intent.putExtra(BroadcastParameters.CALL_STATE, callStateCode);
        intent.putExtra(BroadcastParameters.LAST_STATUS_CODE, lastStatusCode);
        intent.putExtra(BroadcastParameters.CONNECT_TIMESTAMP, connectTimestamp);
        intent.putExtra(BroadcastParameters.LOCAL_HOLD, isLocalHold);
        intent.putExtra(BroadcastParameters.LOCAL_MUTE, isLocalMute);
        intent.putExtra(BroadcastParameters.DATA_TOTAL, data);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void outgoingCall(String accountID, int callID, String number) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.OUTGOING_CALL));
        intent.putExtra(BroadcastParameters.ACCOUNT_ID, accountID);
        intent.putExtra(BroadcastParameters.CALL_ID, callID);
        intent.putExtra(BroadcastParameters.NUMBER, number);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void stackStatus(boolean started) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.STACK_STATUS));
        intent.putExtra(BroadcastParameters.STACK_STARTED, started);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void codecPriorities(ArrayList<CodecPriority> codecPriorities) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.CODEC_PRIORITIES));
        intent.putParcelableArrayListExtra(BroadcastParameters.CODEC_PRIORITIES_LIST, codecPriorities);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

   /* public void codecPrioritiesSetStatus(boolean success) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.CODEC_PRIORITIES_SET_STATUS));
        intent.putExtra(BroadcastParameters.SUCCESS, success);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }*/
}
