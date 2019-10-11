package com.ccsidd.rtone.objects.sip;

import android.util.Log;

import com.ccsidd.rtone.SipModule;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnCallTsxStateParam;
import org.pjsip.pjsua2.SipEvent;
import org.pjsip.pjsua2.SipRxData;
import org.pjsip.pjsua2.SipTxData;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_event_id_e;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dung on 9/8/15.
 */
public class RCall extends org.pjsip.pjsua2.Call
{
    public VideoWindow vidWin;
    public VideoPreview vidPrev;
    private AudioMedia _am;

    public RCall(RAccount acc, int call_id)
    {
        super(acc, call_id);
        vidWin = null;
    }

    @Override
    public void onCallState(OnCallStateParam prm)
    {
        if (SipModule.observerSIP != null)
            SipModule.observerSIP.notifyCallState(this);
//        try {
//            CallInfo ci = getInfo();
//            if (ci.getState() ==
//                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED)
//            {
//                this.delete();
//            }
//        } catch (Exception e) {
//            return;
//        }
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm)
    {
        CallInfo ci;
        try {
            ci = getInfo();
        } catch (Exception e) {
            return;
        }

        CallMediaInfoVector cmiv = ci.getMedia();

        for (int i = 0; i < cmiv.size(); i++) {
            CallMediaInfo cmi = cmiv.get(i);
            if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                    (cmi.getStatus() ==
                            pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                            cmi.getStatus() ==
                                    pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD))
            {
                // unfortunately, on Java too, the returned Media cannot be
                // downcasted to AudioMedia
                Media m = getMedia(i);
                AudioMedia am = AudioMedia.typecastFromMedia(m);

                // connect ports
                try {
                    SipModule.ep.audDevManager().getCaptureDevMedia().
                            startTransmit(am);
                    am.startTransmit(SipModule.ep.audDevManager().
                            getPlaybackDevMedia());
                    _am = am;
                } catch (Exception e) {
                    continue;
                }
            } else if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                    cmi.getStatus() ==
                            pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
                    cmi.getVideoIncomingWindowId() != pjsua2.INVALID_ID)
            {
                vidWin = new VideoWindow(cmi.getVideoIncomingWindowId());
                vidPrev = new VideoPreview(cmi.getVideoCapDev());
            }
        }

        if (SipModule.observerSIP != null)
            SipModule.observerSIP.notifyCallMediaState(this);
    }

    private SipRxData getRxData(SipEvent event)
    {
        if (event.getType().equals(pjsip_event_id_e.PJSIP_EVENT_RX_MSG))
            return event.getBody().getRxMsg().getRdata();

        if (event.getType().equals(pjsip_event_id_e.PJSIP_EVENT_TSX_STATE) && event.getBody().getTsxState().getType().equals(pjsip_event_id_e.PJSIP_EVENT_RX_MSG))
            return event.getBody().getTsxState().getSrc().getRdata();

        return null;
    }

    private SipTxData getTxData(SipEvent event)
    {
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
        String pa;
        Pattern pattern;
        Matcher matcher;
        String callID;
        if(txData != null) {
            TxWholeMsg = txData.getWholeMsg();
        }

        if (rxData != null) {
            RxWholeMsg = rxData.getWholeMsg();
            pa = "Hold-Reason:\\s*(.*)";
//        if (callInfo.getRemoteContact().matches(pa))
//            uri = callInfo.getRemoteContact();
//        else if (callInfo.getRemoteUri().matches(pa))
//            uri = callInfo.getRemoteUri();
            pattern = Pattern.compile(pa);
            matcher = pattern.matcher(RxWholeMsg);
            String holdReason = "";
            while (matcher.find()) {
                holdReason= matcher.group(1);
            }

            if (SipModule.observerSIP != null && holdReason.length() > 0) {
                SipModule.observerSIP.notifyRemoteHold(holdReason);
            }
        }
        if (SipModule.observerSIP != null) {
            pa = "Call-ID:\\s*(.*)";
            pattern = Pattern.compile(pa);
            matcher = pattern.matcher(RxWholeMsg + TxWholeMsg);
            callID = "";
            while (matcher.find()) {
                callID = matcher.group(1);
            }

            SipModule.observerSIP.setDataLengh(callID, RxWholeMsg.length() + TxWholeMsg.length());
        }
    }

    public void enableMicro(boolean enable) {
        try {
            if (enable)
                SipModule.ep.audDevManager().getCaptureDevMedia().startTransmit(_am);
            else
                SipModule.ep.audDevManager().getCaptureDevMedia().stopTransmit(_am);
        }catch (Exception exception){}
    }

    public void setSpeaker(boolean enable)
    {
        CallInfo ci;
        try {
            ci = getInfo();
        } catch (Exception e) {
            return;
        }
        Log.e("audio count", ci.getSetting().getAudioCount()+ "");

        CallMediaInfoVector cmiv = ci.getMedia();

        for (int i = 0; i < cmiv.size(); i++) {
            CallMediaInfo cmi = cmiv.get(i);
            Log.e("type", cmi.getType().toString());
        }
    }
}
