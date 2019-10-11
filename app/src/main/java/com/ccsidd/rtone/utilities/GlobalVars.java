package com.ccsidd.rtone.utilities;

/**
 * Created by dung on 4/8/15.
 */
public class GlobalVars {

    public static boolean IS_SECURED = true;
    public static String SERVER_IP_ADDRESS_UDP = "rtone.ccsidd.com:50061";
    public static String SERVER_IP_ADDRESS_TLS = "rtone.ccsidd.com:5061";

//    public static String SERVER_IP_ADDRESS_UDP = "118.69.78.50:50061";
//    public static String SERVER_IP_ADDRESS_TLS = "118.69.78.50:5061";

//    public static String SERVER_IP_ADDRESS_UDP = "192.168.0.154:50061";
//    public static String SERVER_IP_ADDRESS_TLS = "192.168.0.154:5061";

//    public static String SERVER_IP_ADDRESS_UDP = "202.95.83.39:50061";
//    public static String SERVER_IP_ADDRESS_TLS = "202.95.83.39:5061";

    public static String PREFERENCES_DATA_FILE_NAME = "com.ccsidd.rtone.data.preferences";
    public static String PREFERENCES_DATA_SYNCHRONIZED_DB = "synchronizedDatabase";
    public static String PREFERENCES_DATA_LAST_TAB = "lastTab";
    public static String PREFERENCES_DATA_SERVER_IPv4_ADDRESS = "serverIPv4Address";
    //    public static String PREFERENCES_DATA_LAST_CONTACT_ID_KEY = "lastContactID";
    public static String PREFERENCES_DATA_LAST_DATE_CALL_LOG_KEY = "lastLogDate";
    public static String PREFERENCES_DATA_VQ_MODE = "VQMode";
    public static String PREFERENCES_DATA_APPLICATION_VOIP_OVER_3G = "VoIPOver3G";
    public static String PREFERENCES_DATA_APPLICATION_DTMF = "DTMFSound";
    public static String PREFERENCES_DATA_TRANSPORT = "Transport";
    public static String PREFERENCES_DATA_CODEC = "Codec";
    public static String PREFERENCES_DATA_RINGTONES = "Ringtones";
    public static String PREFERENCES_DATA_MEDIA = "MediaConfiguration";
    public static String PREFERENCES_DATA_MEDIA_ECHO = "EchoCancellation";
    public static String PREFERENCES_DATA_MEDIA_NOISE = "NoiseSuppression";
    public static String PREFERENCES_DATA_THEMES = "Themes";
    public static String PREFERENCES_DATA_DIAL = "DialRules";
    public static String PREFERENCES_DATA_DIAL_PLUS = "DialPlus";
    public static String PREFERENCES_DATA_DIAL_REMOVE = "Prefix2Remove";
    public static String PREFERENCES_DATA_DIAL_ADD = "Prefix2Add";
    public static String PREFERENCES_DATA_CALL_WAITING = "CallWaiting";
    public final static String PREFERENCES_DATA_ACCOUNT_CONFLICT = "ConflictState";
    public final static String PREFERENCES_DATA_ACCOUNT_DEFAULT = "DefaultAccount";
    public final static String PREFERENCES_DATA_STATUS_NETWORK = "StatusNetworks";
    public final static String PREFERENCES_EDIT_CONTACT = "EditContact";
    public final static String PREFERENCES_ADD_CONTACT = "AddContact";

    public final static String CONTACT_LAST_UPDATED_TIMESTAMP = "ContactTimestamp";

    public static String DIAL_PREMIUM_MODE_PLUS = "1505";
    public static String DIAL_PREMIUM_MODE_00x = "1505";
    public static String DIAL_PREMIUM_MODE_011 = "150562";
    public static String DIAL_PREMIUM_MODE_01x = "1505";
    public static String DIAL_PREMIUM_MODE_020 = "150560";
    public static String DIAL_PREMIUM_MODE_02x = "1505";
    public static String DIAL_PREMIUM_MODE_030 = "150560";
    public static String DIAL_PREMIUM_MODE_15xx = "1505";

    public static String DIAL_VQ_MODE_PLUS = "15050";
    public static String DIAL_VQ_MODE_00x = "15050";
    public static String DIAL_VQ_MODE_011 = "1505062";
    public static String DIAL_VQ_MODE_01x = "15050";
    public static String DIAL_VQ_MODE_020 = "1505060";
    public static String DIAL_VQ_MODE_02x = "15050";
    public static String DIAL_VQ_MODE_030 = "1505060";
    public static String DIAL_VQ_MODE_15xx = "15050";

    public static final String DIALOG_FUNCTION_CALL = "Call";
    public static final String DIALOG_FUNCTION_EDIT_CONTACT = "Edit contact";
    public static final String DIALOG_FUNCTION_DELETE_CONTACT = "Delete contact";
    public static final String DIALOG_FUNCTION_ADD_CONTACT = "Add to contact";
    public static final String DIALOG_FUNCTION_BLOCK_CONTACT = "Block contact";
    public static final String DIALOG_FUNCTION_UNBLOCK_CONTACT = "Unblock contact";
    public static final String DIALOG_FUNCTION_SEND_MESSAGE = "Send message";
    public static final String DIALOG_FUNCTION_CLEAR_RECENT = "Clear this call";
    public static final String DIALOG_FUNCTION_CLEAR_ALL_RECENT = "Clear calls of same No.";
    public static final String DIALOG_FUNCTION_ADD_NEW_CONTACT = "Add new";
    public static final String DIALOG_FUNCTION_CONTACT_SIM = "Sim";
    public static final String DIALOG_FUNCTION_CONTACT_DEVICE = "Device";
    public static final String DIALOG_FUNCTION_CONTACT_GOOGLE = "Google";
    public static final String DIALOG_FUNCTION_CONTACT_SKYPE = "Skype";

    public final static String BROADCAST_ACTION_DEFAULT = "com.ccsidd.app.rtone.default";
    public final static String BROADCAST_ACTION_SYNCED_CONTACT = "com.ccsidd.app.rtone.synced.contact";
//    public final static String BROADCAST_ACTION_SYNCED_MESSAGE = "com.ccsidd.app.rtone.synced.message";
//    public final static String BROADCAST_ACTION_SYNCED_CALLLOG = "com.ccsidd.app.rtone.synced.calllog";
    public final static String BROADCAST_ACTION_SIP_SERVICE = "com.ccsidd.app.rtone.sip.service";
    public final static String BROADCAST_ACTION_SIP_REGISTRATION = "com.ccsidd.app.rtone.sip.registration";
    public final static String BROADCAST_ACTION_SIP_CHECK_REGISTRATION = "com.ccsidd.app.rtone.check.registration";
    public final static String BROADCAST_ACTION_SIP_CALL = "com.ccsidd.app.rtone.sip.call";
//    public final static String BROADCAST_ACTION_SIP_CALL_AFTER_CREATE_ACTIVITY = "com.ccsidd.app.rtone.sip.call.after.create.activity";
    public final static String BROADCAST_ACTION_SIP_CALL_STATE = "com.ccsidd.app.rtone.sip.call.state";
    public final static String BROADCAST_ACTION_SIP_SECOND_CALL = "com.ccsidd.app.rtone.sip.call.second";
    public final static String BROADCAST_ACTION_SIP_CALL_MEDIA_STATE = "com.ccsidd.app.rtone.sip.call.media.state";
    public final static String BROADCAST_ACTION_SIP_CALL_END = "com.ccsidd.app.rtone.sip.call.end";
    public final static String BROADCAST_ACTION_SIP_CALL_REMOTE_HOLD_WITH_REASON = "com.ccsidd.app.rtone.sip.call.remote.hold.with.reason";
    public final static String BROADCAST_ACTION_SIP_ACCOUNT_KICKED = "com.ccsidd.app.rtone.sip.account.kicked";
    public final static String BROADCAST_ACTION_SIP_KICK_USER = "com.ccsidd.app.rtone.sip.kick.user";
    public final static String BROADCAST_ACTION_BADGE = "com.ccsidd.app.rtone.badge";

    public final static String SERVICE_METHOD_ACCOUNT_IS_REGISTERED = "com.ccsidd.app.rtone.service.method.account.is.registered";
    public final static String SERVICE_METHOD_REGISTER = "com.ccsidd.app.rtone.service.method.register";
    public final static String SERVICE_METHOD_UNREGISTER = "com.ccsidd.app.rtone.service.method.unRegister";
    public final static String SERVICE_METHOD_CALL_OUT = "com.ccsidd.app.rtone.service.method.call.out";
    public final static String SERVICE_METHOD_CALL_IN = "com.ccsidd.app.rtone.service.method.call.in";
    public final static String SERVICE_METHOD_CALL_ACCEPT = "com.ccsidd.app.rtone.service.method.call.accept";
    public final static String SERVICE_METHOD_CALL_REJECT = "com.ccsidd.app.rtone.service.method.call.reject";
    public final static String SERVICE_METHOD_SECONDCALL_ACCEPT = "com.ccsidd.app.rtone.service.method.secondcall.accept";
    public final static String SERVICE_METHOD_CALL_HANGUP = "com.ccsidd.app.rtone.service.method.call.hangup";
    public final static String SERVICE_METHOD_CALL_CANCEL = "com.ccsidd.app.rtone.service.method.call.cancel";
    public final static String SERVICE_METHOD_SEND_DTMF = "com.ccsidd.app.rtone.service.method.sendDTMF";

    public final static String SERVICE_METHOD_CALL_FUNCTION_SPEAKER = "com.ccsidd.app.rtone.service.method.call.function.speaker";
    public final static String SERVICE_METHOD_CALL_FUNCTION_MICRO = "com.ccsidd.app.rtone.service.method.call.function.micro";
    public final static String SERVICE_METHOD_CALL_FUNCTION_HOLD = "com.ccsidd.app.rtone.service.method.call.function.hold";
    public final static String SERVICE_METHOD_CALL_FUNCTION_HOLD_SD = "com.ccsidd.app.rtone.service.method.call.function.hold.sd";
    public final static String SERVICE_METHOD_CALL_FUNCTION_DIAL = "com.ccsidd.app.rtone.service.method.call.function.dial";
    public final static String SERVICE_METHOD_CALL_FUNCTION_BLUETOOTH = "com.ccsidd.app.rtone.service.method.call.function.bluetooth";
    public final static String SERVICE_METHOD_CHANGE_NETWORK = "com.ccsidd.app.rtone.service.method.change_network";

    public final static int SIP_CALL_MESSAGE_INCOMING_CALL = 0;
    public final static int SIP_CALL_MESSAGE_CALL_STATE = 1;

    public final static String SIP_MESSAGE_HEADER_HOLD_REASON_MANUALLY = "Manually";
    public final static String SIP_MESSAGE_HEADER_HOLD_REASON_INTERRUPTED = "Interrupted";

    public final static String KEY_SETTING_LOGIN_TOKEN = "key_setting_login_token";
    public final static String KEY_SETTING_TOKEN = "key_setting_token";
    public final static String KEY_SETTING_PUSH_TOKEN = "key_setting_push_token";
    public final static String IMEI_DEVICE = "imei_device";
    public final static String KEY_PHONE_CHATTING = "key_phone_chatting";
}
