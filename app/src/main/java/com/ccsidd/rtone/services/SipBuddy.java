package com.ccsidd.rtone.services;

import android.util.Log;

import org.pjsip.pjsua2.Buddy;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.BuddyInfo;
import org.pjsip.pjsua2.SendInstantMessageParam;
import org.pjsip.pjsua2.pjsip_evsub_state;
import org.pjsip.pjsua2.pjsua_buddy_status;

/**
 * Created by thinhvo on 10/11/16.
 */

enum BuddyState {
    NOT_FOUND,
    OFF_LINE,
    ON_LINE,
    ON_HOLD,
    UNKNOWN,
}

public class SipBuddy extends Buddy {
    public BuddyConfig cfg;
    private static final String TAG = SipBuddy.class.getSimpleName();
    private BuddyState buddyState = BuddyState.NOT_FOUND;
    private String statusText = "?";

    public SipBuddy(BuddyConfig config) {
        super();
        cfg = config;
    }

    // refresh the status of this buddy
    void updateBuddyStatus() {
        BuddyInfo bi = null;
        Log.d(TAG, "Called updateBuddyStatus---");
        try {
            bi = getInfo();
        } catch (Exception e) {
            this.buddyState = BuddyState.NOT_FOUND;
            this.statusText = "Not Found";
            Log.d(TAG, "Buddy not found!!!");
            return;
        }


        if (bi.getSubState() == pjsip_evsub_state.PJSIP_EVSUB_STATE_ACTIVE) {

            // BUDDY IS ON LINE
            if (bi.getPresStatus().getStatus() == pjsua_buddy_status.PJSUA_BUDDY_STATUS_ONLINE) {
                statusText = bi.getPresStatus().getStatusText();

                this.buddyState = BuddyState.ON_LINE;

                if (statusText == null || statusText.isEmpty()) {
                    statusText = "Online";
                } else if (statusText != null && statusText.equalsIgnoreCase("On hold")) {
                    this.buddyState = BuddyState.ON_HOLD;
                }
            }

            // BUDDY IS OFFLINE
            else if (bi.getPresStatus().getStatus() == pjsua_buddy_status.PJSUA_BUDDY_STATUS_OFFLINE) {
                statusText = "Offline";
                this.buddyState = BuddyState.OFF_LINE;
            }
            // BUDDY STATUS UNKNOUN
            else {
                this.buddyState = BuddyState.UNKNOWN;
                statusText = "Unknown";
            }
        }
        Log.d(TAG, "(update) BuddyStatus: " + this.buddyState + " : Text:" + statusText);
    }

    @Override
    public void onBuddyState() {

        // update the status of this buddy because something is changed
        updateBuddyStatus();

//        Log.d(TAG, "\n\nON BUDDY STATE ---> " + this.getState());
        if (this.buddyState == BuddyState.ON_LINE) {
//            notifyEvent(new VoipEventBundle(VoipEventType.BUDDY_EVENT, VoipEvent.BUDDY_CONNECTED, "BuddyStateChanged:::" + this.statusText + " BuddyState:" + this.getState(), this));
        } else if (this.buddyState == BuddyState.ON_HOLD) {
//            notifyEvent(new VoipEventBundle(VoipEventType.BUDDY_EVENT, VoipEvent.BUDDY_HOLDING, "BuddyStateChanged:::" + this.statusText + " BuddyState:" + this.getState(), this));
        } else if (this.buddyState == BuddyState.OFF_LINE || this.buddyState == BuddyState.UNKNOWN) {
//            notifyEvent(new VoipEventBundle(VoipEventType.BUDDY_EVENT, VoipEvent.BUDDY_DISCONNECTED, "BuddyStateChanged:::" + this.statusText + " BuddyState:" + this.getState(), this));
        } else {
//            Log.d(TAG, "BUDDY STATE NOT HANDLED::---> " + this.getState());
        }
        Log.d(TAG, "-------END BUDDY STATE HANDLING ---------------\n\n");
    }

    public String getUri() {
        // TODO Auto-generated method stub
        return this.cfg.getUri();
    }

    public String getStatusText() {
        return this.statusText;
    }

    public void refreshStatus() {
        this.updateBuddyStatus();
    }

    public String getExtension() {
        String uri = this.cfg.getUri();
        return uri.substring(uri.indexOf(':') + 1, uri.indexOf('@'));
    }

}
