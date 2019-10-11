package com.ccsidd.rtone.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ccsidd.rtone.Foreground;
import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.CallActivity;
import com.ccsidd.rtone.activities.MainActivity;
import com.ccsidd.rtone.activities.MessageActivity;
import com.ccsidd.rtone.objects.AccountStateEvent;
import com.ccsidd.rtone.objects.CallEvent;
import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.objects.Message;
import com.ccsidd.rtone.objects.sip.ConflictState;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;

import org.greenrobot.eventbus.EventBus;
import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.SendInstantMessageParam;
import org.pjsip.pjsua2.pjsip_status_code;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Wrapper around PJSUA2 Account object.
 *
 * @author gotev (Aleksandar Gotev)
 */
public class SipAccount extends Account {

    private static final String LOG_TAG = SipAccount.class.getSimpleName();
    public HashMap<String, SipBuddy> buddyList = new HashMap<>();
    private HashMap<Integer, SipCall> activeCalls = new HashMap<>();
    private SipAccountData data;
    private VoIPService service;

    protected SipAccount(VoIPService service, SipAccountData data) {
        super();
        this.service = service;
        this.data = data;
    }

    public VoIPService getService() {
        return service;
    }

    public SipAccountData getData() {
        return data;
    }

    public void create() throws Exception {
        create(data.getAccountConfig(service));
    }

    protected void removeCall(int callId) {
        SipCall call = activeCalls.get(callId);

        if (call != null) {
            Logger.debug(LOG_TAG, "Removing call with ID: " + callId);
            activeCalls.remove(callId);
        }
    }

    public SipCall getCall(int callId) {
        return activeCalls.get(callId);
    }

    public Set<Integer> getCallIDs() {
        return activeCalls.keySet();
    }

    public SipCall addIncomingCall(int callId) {

        SipCall call = new SipCall(this, callId);
        activeCalls.put(callId, call);
        Logger.debug(LOG_TAG, "Added incoming call with ID " + callId + " to " + data.getIdUri());
        return call;
    }

    public SipCall addOutgoingCall(final String numberToDial) {
        SipCall call = new SipCall(this);

        CallOpParam callOpParam = new CallOpParam();
        try {
            if (numberToDial.startsWith("sip:")) {
                call.makeCall(numberToDial, callOpParam);
            } else {
                if ("*".equals(data.getRealm())) {
                    call.makeCall("sip:" + numberToDial, callOpParam);
                } else {
                    call.makeCall("sip:" + numberToDial + "@" + data.getRealm(), callOpParam);
                }
            }
            activeCalls.put(call.getId(), call);
            Logger.debug(LOG_TAG, "New outgoing call with ID: " + call.getId());

            return call;

        } catch (Exception exc) {
            Logger.error(LOG_TAG, "Error while making outgoing call", exc);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SipAccount that = (SipAccount) o;

        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        String endpoint = null;
        if (!prm.getReason().contains("CCS") && prm.getRdata() != null && !prm.getReason().contains("Wrong Endpoint")) {
            if (prm.getRdata().getWholeMsg().contains("Endpoint")) {

                String pa = "Endpoint:\\s*(.*)";
                Logger.debug(LOG_TAG, "Endpoint " + prm.getRdata().getWholeMsg());
                Pattern pattern = Pattern.compile(pa);
                Matcher matcher = pattern.matcher(prm.getRdata().getWholeMsg());
                while (matcher.find()) {
                    endpoint = matcher.group(1);
                }
            }
        }

        if (prm.getCode().equals(pjsip_status_code.PJSIP_SC_UNAUTHORIZED)) {
            if (prm.getReason().equals("Wrong Endpoint")) {
                Utility.savePref(service, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_CONFLICT, ConflictState.WarningNoShown.getStateString());
            }
        }

        Logger.debug(LOG_TAG, "Reg state: " + prm.getCode().swigValue());

//        service.getBroadcastEmitter()
//                .registrationState(data.getIdUri(), prm.getCode().swigValue(), prm.getReason(), endpoint);

        EventBus.getDefault().post(new AccountStateEvent(data.getIdUri(), prm.getCode(), prm.getReason(), endpoint));
    }

    //Buddy------

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {

        SipCall call = addIncomingCall(prm.getCallId());
        call.setIncoming(true);

        if (activeCalls.size() > 2) {
            call.sendBusyHereToIncomingCall();
            Logger.debug(LOG_TAG, "sending busy to call ID: " + prm.getCallId());
            //TODO: notification of missed call
            return;
        }

        try {
            // Answer with 180 Ringing
            CallOpParam callOpParam = new CallOpParam();
            callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
            call.answer(callOpParam);
            Logger.debug(LOG_TAG, "Sending 180 ringing");

            CallerInfo contactInfo = new CallerInfo(call.getInfo());

            if (activeCalls.size() == 1) {
                Intent waitingIntent = new Intent(service, CallActivity.class);
                waitingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                waitingIntent.putExtra("kind", "in");
                waitingIntent.putExtra("callID", prm.getCallId());
                waitingIntent.putExtra("phoneNumber", contactInfo.getPhone());
                service.startActivity(waitingIntent);

                Logger.debug(LOG_TAG, "Call ID: " + prm.getCallId());
            } else if (activeCalls.size() == 2) {
//                Intent intent = new Intent(GlobalVars.BROADCAST_ACTION_SIP_SECOND_CALL);
//                intent.putExtra("callID", prm.getCallId());
//                intent.putExtra("phone", contactInfo.getPhone());
//                service.sendBroadcast(intent);
                Thread.sleep(500);
                EventBus.getDefault().post(new CallEvent("", prm.getCallId(), contactInfo.getPhone()));
                Logger.debug(LOG_TAG, "Call ID: " + prm.getCallId());
            }

//            service.startRingtone();

//            service.getBroadcastEmitter()
//                    .incomingCall(data.getIdUri(), prm.getCallId(),
//                            contactInfo.getPhone(), contactInfo.getRemoteUri());

        } catch (Exception exc) {
            Logger.error(LOG_TAG, "Error while getting call info", exc);
        }
    }

    /*public IBuddy[] getBuddies() {
        return this.buddyList.values().toArray(new IBuddy[0]);
    }*/

    public boolean hasBuddy(String uri) {
        return buddyList.containsKey(uri);
    }

    public boolean addBuddy(String buddyUri) {

        if (this.hasBuddy(buddyUri)) {
            Log.d(LOG_TAG, "Buddy with extension:" + buddyUri + " already added");
            return false;
        }
        BuddyConfig buddyConfig = new BuddyConfig();
        //dest_uri = "sip:%s@%s;transport=tcp" % (str(dest_extension), self.sip_server)
//        if (configParams.containsKey("sipServerTransport") && configParams.get("sipServerTransport").equalsIgnoreCase("tcp"))
        //buddyUri += ";transport=tcp";

        buddyConfig.setUri(buddyUri);
        buddyConfig.setSubscribe(false);
//        notifyEvent(new VoipEventBundle(VoipEventType.ACCOUNT_EVENT, VoipEvent.BUDDY_SUBSCRIBING, "Subscribing buddy with uri:" + buddyUri, buddyUri));
        return (this.addBuddy(buddyConfig) != null);

    }

    public boolean removeBuddy(String buddyUri) {
        return (this.delBuddy(buddyUri) != null);
    }

    public SipBuddy getBuddy(String buddyUri) {

        SipBuddy b = this.buddyList.get(buddyUri);
        if (b != null) {
            b.refreshStatus();
        }
        return b;
    }


    /***
     * add a buddy to this account , if not already added
     *
     * @param bud_cfg
     * @return the added buddy, null idf the buddy was previuosly added or an error occurred
     */
    public SipBuddy addBuddy(BuddyConfig bud_cfg) {
        if (buddyList.containsKey(bud_cfg.getUri())) {
            Log.d(LOG_TAG, "Buddy with extension:" + bud_cfg.getUri() + " already added");
            return null;
        }
            /* Create Buddy */
        SipBuddy bud = new SipBuddy(bud_cfg);
        try {
            bud.create(this, bud_cfg);
        } catch (Exception e) {
            bud = null;
        }

        if (bud != null) {
            buddyList.put(bud_cfg.getUri(), bud);
            if (bud_cfg.getSubscribe())
                try {
                    bud.subscribePresence(true);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error subscribing the buddy:" + e);
                }
        }
        return bud;
    }

    /**
     * delete the buddy with the given uri  from the account
     *
     * @param uri
     * @return the removed buddy, or null if the buddy to remove was not found.
     */
    public SipBuddy delBuddy(String uri) {
        SipBuddy mb = buddyList.remove(uri);
        if (mb != null) {
                /*
                try {
					mb.subscribePresence(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
            mb.delete();
        }
        return mb;
    }

    @Override
    public void onInstantMessage(OnInstantMessageParam prm) {
        System.out.println("======== Incoming pager ======== ");
        System.out.println("From 		: " + prm.getFromUri());
        System.out.println("To			: " + prm.getToUri());
        System.out.println("Contact		: " + prm.getContactUri());
        System.out.println("Mimetype	: " + prm.getContentType());
        System.out.println("Body		: " + prm.getMsgBody());

        String phoneNumber = "";
        Pattern remoteContactPattern = Pattern.compile("<?sip(s)?:(.*)@.*>?");
        Matcher matcher = remoteContactPattern.matcher(prm.getFromUri());
        while (matcher.find()) {
            if (matcher.group(2) != null)
                phoneNumber = matcher.group(2);
            else
                phoneNumber = "UNKNOWN";
        }

        String body = prm.getMsgBody();
        try {
            body = URLDecoder.decode(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        final Message message = new Message();
        message.setBody(body);
        message.setType(0);
        message.setPhoneNumber(phoneNumber);
        message.setTime(cal.getTimeInMillis());
        message.setUnRead(true);

        final Conversation conversation = new Conversation();
        conversation.setLastMessage(body);
        conversation.setType(0);
        conversation.setPhoneNumber(phoneNumber);
        conversation.setTime(cal.getTimeInMillis());
        conversation.setUnRead(true);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(message);
            }
        });
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(conversation);
            }
        });

        Intent intent = new Intent(service, MainActivity.class)
                .putExtra("phoneNumber", phoneNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String phoneChatting = Utility.getPref(service, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.KEY_PHONE_CHATTING);
        if (/*Foreground.get(service.getApplication()).isBackground() || */!phoneChatting.contains(phoneNumber)) {
            RealmResults<Message> messages = realm.where(Message.class).equalTo("phoneNumber", phoneNumber).equalTo("unRead", true).findAll();
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(phoneNumber);
            for (Message m : messages) {
                String content = m.getBody().startsWith("[[") && m.getBody().endsWith("]]") ? "Have a sticker" : m.getBody();
                inboxStyle.addLine(content);
            }

            body = body.startsWith("[[") && body.endsWith("]]") ? "Have a sticker" : body;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service)
                    .setSmallIcon(R.drawable.ic_launcher_small)
                    .setContentTitle("RTone")
                    .setContentText(phoneNumber + ": " + body)
                    .setLights(Color.GREEN, 2000, 1000)
                    .setAutoCancel(true)
                    .setNumber(messages.size())
                    .setSound(defaultSoundUri)
//                    .setFullScreenIntent(pendingIntent, false)
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent);

            final NotificationManager notificationManager =
                    (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
        }

        RealmResults<Conversation> realmResults = realm.where(Conversation.class).equalTo("unRead", true).findAll();
        if (realmResults.size() > 0) {
            ShortcutBadger.applyCount(service, realmResults.size() > 999 ? 999 : realmResults.size());
        }
        realm.close();

        //EventBus.getDefault().post(message);
//        Intent syncMessage = new Intent(GlobalVars.BROADCAST_ACTION_SYNCED_MESSAGE);
//        service.sendBroadcast(syncMessage);
    }

    public void sendInstantMessage(String buddy_uri, String msgBody) throws UnsupportedEncodingException {

        if (!hasBuddy(buddy_uri))
            return;
        SipBuddy myBuddy = getBuddy(buddy_uri);
        SendInstantMessageParam prm = new SendInstantMessageParam();

        msgBody = URLEncoder.encode(msgBody, "UTF-8");
        prm.setContent(msgBody);

        try {
            myBuddy.sendInstantMessage(prm);
            //removeBuddy(buddy_uri);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
