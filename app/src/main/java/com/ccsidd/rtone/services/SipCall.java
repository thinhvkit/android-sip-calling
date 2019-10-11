package com.ccsidd.rtone.services;

import com.ccsidd.rtone.objects.CallStateEvent;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;

import org.greenrobot.eventbus.EventBus;
import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.CallSetting;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnCallTsxStateParam;
import org.pjsip.pjsua2.SipEvent;
import org.pjsip.pjsua2.SipHeader;
import org.pjsip.pjsua2.SipHeaderVector;
import org.pjsip.pjsua2.SipRxData;
import org.pjsip.pjsua2.SipTxData;
import org.pjsip.pjsua2.SipTxOption;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_event_id_e;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_call_flag;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper around PJSUA2 Call object.
 *
 * @author gotev (Aleksandar Gotev)
 */
public class SipCall extends Call {

    private static final String LOG_TAG = SipCall.class.getSimpleName();

    private SipAccount account;
    private boolean localHold = false;
    private boolean localMute = false;
    private boolean incoming = false;
    private long data = 0;
    private long connectTimestamp = 0;

    /**
     * Incoming call constructor.
     *
     * @param account the account which own this call
     * @param callID  the id of this call
     */
    public SipCall(SipAccount account, int callID) {
        super(account, callID);
        this.account = account;
    }

    /**
     * Outgoing call constructor.
     *
     * @param account account which owns this call
     */
    public SipCall(SipAccount account) {
        super(account);
        this.account = account;
    }

    public pjsip_inv_state getCurrentState() {
        try {
            CallInfo info = getInfo();
            return info.getState();
        } catch (Exception exc) {
            Logger.error(getClass().getSimpleName(), "Error while getting call Info", exc);
            return pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED;
        }
    }

    public pjsip_status_code getLastStatusCode() {
        try {
            CallInfo info = getInfo();
            return info.getLastStatusCode();
        } catch (Exception exc) {
            Logger.error(getClass().getSimpleName(), "Error while getting call Info", exc);
            return pjsip_status_code.PJSIP_SC_UNAUTHORIZED;
        }
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        try {
            CallInfo info = getInfo();
            int callID = info.getId();
            pjsip_inv_state callState = info.getState();
            pjsip_status_code lastStatusCode = info.getLastStatusCode();

            String data = "";

            /**
             * From: http://www.pjsip.org/docs/book-latest/html/call.html#call-disconnection
             *
             * Call disconnection event is a special event since once the callback that
             * reports this event returns, the call is no longer valid and any operations
             * invoked to the call object will raise error exception.
             * Thus, it is recommended to delete the call object inside the callback.
             */

            if (callState == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {

//                account.getService().stopRingtone();
                data = getDataUsageAndTimeDuration();
                account.removeCall(callID);
            } else if (callState == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
//                account.getService().stopRingtone();
                connectTimestamp = System.currentTimeMillis();
            }

//            account.getService().getBroadcastEmitter()
//                    .callState(account.getData().getIdUri(), callID, callState.swigValue(), lastStatusCode.swigValue(),
//                            connectTimestamp, localHold, localMute, data);

            EventBus.getDefault().post(new CallStateEvent(account.getData().getIdUri(), callID, callState, lastStatusCode,
                    connectTimestamp, localHold, localMute, data));

            Logger.debug(LOG_TAG, callState.swigValue() + " - " + lastStatusCode.swigValue());

            if (callState == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                Runtime.getRuntime().gc();
                this.delete();

            }

        } catch (Exception exc) {
            Logger.error(LOG_TAG, "onCallState: error while getting call info", exc);
        }
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {

        CallInfo info;
        try {
            info = getInfo();
        } catch (Exception exc) {
            Logger.error(LOG_TAG, "onCallMediaState: error while getting call info", exc);
            return;
        }

        for (int i = 0; i < info.getMedia().size(); i++) {
            Media media = getMedia(i);
            CallMediaInfo mediaInfo = info.getMedia().get(i);

            if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
                    && media != null
                    && (mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE
                    || mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)) {
                AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);

//                account.getService().stopRingtone();

                // connect the call audio media to sound device
                try {
                    AudDevManager mgr = account.getService().getAudDevManager();

                    try {
                        audioMedia.adjustRxLevel((float) 1.5);
                        audioMedia.adjustTxLevel((float) 1.5);
                    } catch (Exception exc) {
                        Logger.error(LOG_TAG, "Error while adjusting levels", exc);
                    }

                    mgr.getCaptureDevMedia().startTransmit(audioMedia);

//                    Set<Integer> activeCallIDs = account.getCallIDs();
//                    if(activeCallIDs.size() > 1) {
//                        for (int callID : activeCallIDs) {
//                            if(callID != info.getId()) {
//                                try {
//                                    SipCall sipCall = account.getCall(callID);
//
//                                    if (sipCall == null) {
//                                        return;
//                                    }
//                                    info = sipCall.getInfo();
//                                    for (int j = 0; j < info.getMedia().size(); i++) {
//                                        mediaInfo = info.getMedia().get(j);
//                                        if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO) {
//                                            AudioMedia aud_med2 = (AudioMedia) sipCall.getMedia(i);
//                                            audioMedia.startTransmit(aud_med2);
//                                            aud_med2.startTransmit(audioMedia);
//                                        }
//                                    }
//                                } catch (Exception exc) {
//                                    Logger.error(LOG_TAG, "Error while holding call", exc);
//                                }
//                            }
//                        }
//                    }else{
                        audioMedia.startTransmit(mgr.getPlaybackDevMedia());
//                    }
                } catch (Exception exc) {
                    Logger.error(LOG_TAG, "Error while connecting audio media to sound device", exc);
                }
            }
        }
    }

    /**
     * Get the total duration of the call.
     *
     * @return the duration in milliseconds or 0 if the call is not connected.
     */
    public long getConnectTimestamp() {
        return connectTimestamp;
    }

    public void acceptIncomingCall() {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_OK);

        try {
            answer(param);
        } catch (Exception exc) {
            Logger.error(LOG_TAG, "Failed to accept incoming call", exc);
        }
    }

    public void sendBusyHereToIncomingCall() {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);

        try {
            hangup(param);
        } catch (Exception exc) {
            Logger.error(LOG_TAG, "Failed to send busy here", exc);
        }
    }

    public void declineIncomingCall() {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);

        try {
//            answer(param);
            hangup(param);
        } catch (Exception exc) {
            Logger.error(LOG_TAG, "Failed to decline incoming call", exc);
        }
    }

    public void hangUp() {
        CallOpParam param = new CallOpParam();
//        param.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);

        try {
            hangup(param);
        } catch (Exception exc) {
            Logger.error(LOG_TAG, "Failed to hangUp call", exc);
        }
    }

    /**
     * Utility method to mute/unmute the device microphone during a call.
     *
     * @param mute true to mute the microphone, false to un-mute it
     */
    public void setMute(boolean mute) {
        // return immediately if we are not changing the current state
        if ((localMute && mute) || (!localMute && !mute)) return;

        CallInfo info;
        try {
            info = getInfo();
        } catch (Exception exc) {
            Logger.error(LOG_TAG, "setMute: error while getting call info", exc);
            return;
        }

        for (int i = 0; i < info.getMedia().size(); i++) {
            Media media = getMedia(i);
            CallMediaInfo mediaInfo = info.getMedia().get(i);

            if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
                    && media != null
                    && mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);

                // connect or disconnect the captured audio
                try {
                    AudDevManager mgr = account.getService().getAudDevManager();

                    if (mute) {
                        mgr.getCaptureDevMedia().stopTransmit(audioMedia);
                        localMute = true;
                    } else {
                        mgr.getCaptureDevMedia().startTransmit(audioMedia);
                        localMute = false;
                    }

                } catch (Exception exc) {
                    Logger.error(LOG_TAG, "setMute: error while connecting audio media to sound device", exc);
                }
            }
        }
    }

    public boolean isLocalMute() {
        return localMute;
    }

    public boolean toggleMute() {
        if (localMute) {
            setMute(false);
            return !localHold;
        }

        setMute(true);
        return localHold;
    }

    /**
     * Utility method to transfer a call to a number in the same realm as the account to
     * which this call belongs to. If you want to transfer the call to a different realm, you
     * have to pass the full string in this format: sip:NUMBER@REALM. E.g. sip:200@mycompany.com
     *
     * @param destination destination to which to transfer the call.
     * @throws Exception if an error occurs during the call transfer
     */
    public void transferTo(String destination) throws Exception {
        String transferString;

        if (destination.startsWith("sip:")) {
            transferString = "<" + destination + ">";
        } else {
            if ("*".equals(account.getData().getRealm())) {
                transferString = "<sip:" + destination + ">";
            } else {
                transferString = "<sip:" + destination + "@" + account.getData().getRealm() + ">";
            }
        }

        CallOpParam param = new CallOpParam();

        xfer(transferString, param);
    }

    public void renewInvite() {
        CallOpParam param = new CallOpParam();
        try {
            Logger.debug(LOG_TAG, "re_new_invite call with ID " + getId());
            CallSetting opt = param.getOpt();
            opt.setAudioCount(1);
            opt.setVideoCount(0);
            opt.setFlag(pjsua_call_flag.PJSUA_CALL_UPDATE_CONTACT.swigValue());
            reinvite(param);
        } catch (Exception exc) {
            //
        }
    }

    public void setHold(boolean hold) {
        // return immediately if we are not changing the current state
        if ((localHold && hold) || (!localHold && !hold)) return;

        CallOpParam param = new CallOpParam();

        try {
            if (hold) {
                Logger.debug(LOG_TAG, "holding call with ID " + getId());

                SipTxOption option = new SipTxOption();
                SipHeader sipHeader = new SipHeader();
                sipHeader.setHName("Hold-Reason");
                sipHeader.setHValue(GlobalVars.SIP_MESSAGE_HEADER_HOLD_REASON_MANUALLY);
                SipHeaderVector headerVector = new SipHeaderVector();
                headerVector.add(sipHeader);
                option.setHeaders(headerVector);
                param.setTxOption(option);
                setHold(param);
                localHold = true;
            } else {
                // http://lists.pjsip.org/pipermail/pjsip_lists.pjsip.org/2015-March/018246.html
                Logger.debug(LOG_TAG, "un-holding call with ID " + getId());
                CallSetting opt = param.getOpt();
                opt.setAudioCount(1);
                opt.setVideoCount(0);
                opt.setFlag(pjsua_call_flag.PJSUA_CALL_UNHOLD.swigValue());
                reinvite(param);
                localHold = false;
            }
        } catch (Exception exc) {
            String operation = hold ? "hold" : "unhold";
            Logger.error(LOG_TAG, "Error while trying to " + operation + " call", exc);
        }
    }

    public boolean toggleHold() {
        if (localHold) {
            setHold(false);
            return !localHold;
        }

        setHold(true);
        return localHold;
    }

    public void reInvite() {
        try {
            CallOpParam param = new CallOpParam();
            CallSetting opt = param.getOpt();
            opt.setAudioCount(1);
            opt.setVideoCount(0);
            opt.setFlag(pjsua_call_flag.PJSUA_CALL_UPDATE_CONTACT.swigValue());
            reinvite(param);
        } catch (Exception exc) {
            Logger.error(LOG_TAG, "Error while trying to re-invite call", exc);
        }
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public boolean isLocalHold() {
        return localHold;
    }

    private SipRxData getRxData(SipEvent event) {
        if (event.getType().equals(pjsip_event_id_e.PJSIP_EVENT_RX_MSG))
            return event.getBody().getRxMsg().getRdata();

        if (event.getType().equals(pjsip_event_id_e.PJSIP_EVENT_TSX_STATE) && event.getBody().getTsxState().getType().equals(pjsip_event_id_e.PJSIP_EVENT_RX_MSG))
            return event.getBody().getTsxState().getSrc().getRdata();

        return null;
    }

    private SipTxData getTxData(SipEvent event) {
        if (event.getType().equals(pjsip_event_id_e.PJSIP_EVENT_TX_MSG))
            return event.getBody().getTxMsg().getTdata();

        if (event.getType().equals(pjsip_event_id_e.PJSIP_EVENT_TSX_STATE) && event.getBody().getTsxState().getType().equals(pjsip_event_id_e.PJSIP_EVENT_TX_MSG))
            return event.getBody().getTsxState().getSrc().getTdata();

        return null;
    }

    @Override
    public void onCallTsxState(OnCallTsxStateParam prm) {
        super.onCallTsxState(prm);

        SipRxData rxData = getRxData(prm.getE());
        SipTxData txData = getTxData(prm.getE());
        String RxWholeMsg = "";
        String TxWholeMsg = "";
        if (txData != null) {
            TxWholeMsg = txData.getWholeMsg();
        }

        if (rxData != null) {
            RxWholeMsg = rxData.getWholeMsg();
        }
        data = data + RxWholeMsg.length() + TxWholeMsg.length();
    }

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    public String getDataUsageAndTimeDuration() throws Exception {
        String dumpInfo = this.dump(true, " ||| ");
        String regex = "([\\d.]*)([^\\s\\d]+)\\s+\\+IP";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dumpInfo);
        long dataUsage = 0;
        Logger.debug(LOG_TAG, dumpInfo + "\n - " + data);
        while (matcher.find()) {
            if (matcher.group(2).contains("GB")) {
                dataUsage += Double.valueOf(matcher.group(1)) * 1024 * 1024 * 1024;
            } else if (matcher.group(2).contains("MB")) {
                dataUsage += Double.valueOf(matcher.group(1)) * 1024 * 1024;
            } else if (matcher.group(2).contains("KB")) {
                dataUsage += Double.valueOf(matcher.group(1)) * 1024;
            }
        }

        long timeSec = getInfo().getConnectDuration().getSec();
        String duration = String.format("%02d", timeSec / 3600) + ":"
                + String.format("%02d", (timeSec % 3600) / 60) + ":"
                + String.format("%02d", timeSec % 60);

        return Utility.convertToStringRepresentation(dataUsage + getData()) + " " + duration;
    }
}
