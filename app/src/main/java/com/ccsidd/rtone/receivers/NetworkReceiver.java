package com.ccsidd.rtone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dung on 2/23/16.
 */
public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*if (Utility.checkNetwork(context)) {
            Utility.configureRealm(context, Utility.getPref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));
            Realm realm = Realm.getDefaultInstance();
            RealmResults<User> users = realm.allObjects(User.class);
            if (users.size() > 0 && users.first().getPassword().length() > 0) {
                User tmp = users.first();
                User user = new User();
                user.setUsername(tmp.getUsername());
                user.setPassword(tmp.getPassword());
                Gson gson = Utility.createGson();
                Intent intentService = new Intent(GlobalVars.BROADCAST_ACTION_SIP_SERVICE);
                intent.putExtra("function", GlobalVars.SERVICE_METHOD_REGISTER);
                intent.putExtra("data", gson.toJson(user));
                context.sendBroadcast(intentService);
            }
            realm.close();
        }*/
        /*String status = Utility.getConnectivityStatusString(context);
        Log.i("Network", status);

        Intent intentService = new Intent(GlobalVars.BROADCAST_ACTION_SIP_SERVICE);
        intentService.setAction(GlobalVars.BROADCAST_ACTION_SIP_SERVICE);
        intentService.putExtra("function", GlobalVars.SERVICE_METHOD_CHANGE_NETWORK);
        context.sendBroadcast(intentService);

        Toast.makeText(context, status, Toast.LENGTH_LONG).show();*/
    }
}
