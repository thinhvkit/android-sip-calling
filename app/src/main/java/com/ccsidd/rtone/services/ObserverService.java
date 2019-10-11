/*
package com.ccsidd.rtone.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.ccsidd.rtone.Interfaces.ObserverInterface;
import com.ccsidd.rtone.observers.CallLogContentObserver;
import com.ccsidd.rtone.observers.ContactContentObserver;
import com.ccsidd.rtone.utilities.GlobalVars;

public class ObserverService extends Service implements ObserverInterface{

    private ContactContentObserver contactContentObserver;
//    private CallLogContentObserver callLogContentObserver;

    public ObserverService() {
    }

//    private final IBinder mBinder = new ObserverBinder();
//
//    public class ObserverBinder extends Binder {
//        ObserverService getService() {
//            return ObserverService.this;
//        }
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        contactContentObserver = new ContactContentObserver(this, this);
        //callLogContentObserver = new CallLogContentObserver(this, this);

        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactContentObserver);
        //getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogContentObserver);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//        return mBinder;
        return null;
    }

    @Override
    public void onChange(Uri uri) {

        String action = GlobalVars.BROADCAST_ACTION_DEFAULT;
        if (uri == ContactsContract.Contacts.CONTENT_URI)
        {
            action = GlobalVars.BROADCAST_ACTION_SYNCED_CONTACT;
        }
//        else if (uri == CallLog.Calls.CONTENT_URI)
//        {
//            action = GlobalVars.BROADCAST_ACTION_SYNCED_CALLLOG;
//        }
        Intent i = new Intent(action);
        sendBroadcast(i);
    }
}
*/
