package com.ccsidd.rtone.fcm;

/**
 * Created by thinhvo on 11/16/16.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.MainActivity;
import com.ccsidd.rtone.objects.CallLog;
import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.services.SipServiceCommand;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        Map<String,String> data = remoteMessage.getData();
        String message = "";
        String userName = data.get("user");
        String type = data.get("type");

        if (type.contains("INCOMING") || type.contains("MESSAGE")) {
            //restart service
            SipServiceCommand.start(this);
//            SipServiceCommand.restartSipStack(this);
//            message = "Incoming: " + userName;
//            sendNotification("test push");
        } else if (type.contains("MISSED_CALL")) {
            //Utility.configureRealm(getApplicationContext(), Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));
            String regex = "src=(.*);id=(.*);t=(.*)";
            Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(userName == null ? "" : userName);
            while (matcher.find()) {
                Log.e(TAG, String.format("%s - %s - %s", matcher.group(1), matcher.group(2), matcher.group(3)));
                /*if (Utility.blockNumber(matcher.group(1)))
                    continue;*/
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        long id = 0;
                        RealmResults<CallLog> result = realm.where(CallLog.class).findAll();
                        if (result.size() > 0)
                            id = result.where().max("id").longValue();
                        if (result.where().equalTo("callId", matcher.group(2)).findFirst() == null) {
                            CallLog callLog = new CallLog();
                            callLog.setId((int) id + 1);
                            callLog.setCallId(matcher.group(2));
                            callLog.setName(Utility.formatNumberphone(matcher.group(1)));
                            callLog.setNumber(Utility.formatNumberphone(matcher.group(1)));
                            callLog.setDate(Long.valueOf(matcher.group(3)).longValue() * 1000L);
                            callLog.setNumberLabel("VoIP");
                            callLog.setNumberType(0);
                            callLog.setType(android.provider.CallLog.Calls.MISSED_TYPE);

                            realm.copyToRealm(callLog);

                            Intent intentBadge = new Intent(GlobalVars.BROADCAST_ACTION_BADGE);
                            intentBadge.putExtra("missCall", 1);
                            sendBroadcast(intentBadge);
                        }
                    }
                });

                message =  String.format("Missed call: %s", matcher.group(1));
                sendNotification(message);
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_small)
                .setContentTitle("RTone")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
//                .setFullScreenIntent(pendingIntent, false)
                .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}