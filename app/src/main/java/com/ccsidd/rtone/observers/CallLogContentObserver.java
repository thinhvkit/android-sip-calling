/*
package com.ccsidd.rtone.observers;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;

import com.ccsidd.rtone.Interfaces.ObserverInterface;
import com.ccsidd.rtone.objects.CallLog;
import com.ccsidd.rtone.objects.CallLogList;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

*/
/**
 * Created by dung on 7/29/15.
 *//*

public class CallLogContentObserver extends ContentObserver {

    private Context context;
    private ObserverInterface observerInterface;
    private Uri callLogUri = android.provider.CallLog.Calls.CONTENT_URI;

    public CallLogContentObserver(Context context, ObserverInterface observerInterface) {
        super(null);
        this.context = context;
        this.observerInterface = observerInterface;
    }

    */
/*@Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        CallLogList callLogList = new CallLogList(context);
        callLogList.startGetCallLog();
//        ContactList contactList = new ContactList(context);
//        contactList.startGetContact();

        final ArrayList<CallLog> callLogs = callLogList.getCallLogs();

//        for (CallLog callLog : recents)
//        {
//            if (callLog.getName().length() == 0)
//            {
//                for (Contact contact : ContactFragment.contacts)
//                {
//                    if (callLog.getName().length() > 0)
//                        break;
//                    for (Phone phone : contact.getPhoneNumbers())
//                    {
//                        if (callLog.getName().length() > 0 )
//                            break;
//
//                        if (phone.getNumber().equals(callLog.getName()))
//                            callLog.getName() = contact.getDisplayName();
//
//                    }
//                }
//            }
//        }


        Realm realm = Realm.getDefaultInstance();
        final CallLog callLogResult = realm.where(CallLog.class).equalTo("id", -1).findFirst();
        if (callLogResult != null) {
            final CallLog callLog = callLogs.get(0);
            final CallLog newCallLog = new CallLog();
            newCallLog.setId(callLog.getId());
            newCallLog.setNumber(callLog.getNumber());
            newCallLog.setImageUri(callLog.getImageUri());
            newCallLog.setType(callLog.getType());
            newCallLog.setDate(callLog.getDate());
            newCallLog.setNumberLabel(callLog.getNumberLabel());
            newCallLog.setNumberType(callLog.getNumberType());
            newCallLog.setCountry(callLog.getCountry());
            newCallLog.setHasContact(callLogResult.isHasContact());
            newCallLog.setName(callLogResult.getName());
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(newCallLog);
                    realm.where(CallLog.class).equalTo("id", -1).findAll().clear();
                }
            });
        }
        else {
            final RealmQuery<CallLog> query = realm.where(CallLog.class);
            if (callLogs.size() == 0) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<CallLog> results = query.findAll();
                        results.clear();
                    }
                });
            } else {
                for (int i = 0; i < callLogs.size(); i++) {
                    query.notEqualTo("id", callLogs.get(i).getId());
                }
                query.notEqualTo("id", callLogs.get(callLogs.size() - 1).getId());
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<CallLog> results = query.findAll();
                        results.clear();
                    }
                });
            }
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(CallLog.class).equalTo("id", -1).findAll().clear();
            }
        });
        realm.close();
        callLogList.getCallLogs().clear();
        observerInterface.onChange(callLogUri);
    }*//*


}
*/
