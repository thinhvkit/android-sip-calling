/*
package com.ccsidd.rtone.observers;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.ContactsContract;

import com.ccsidd.rtone.Interfaces.ObserverInterface;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactList;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;
import com.google.gson.Gson;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

*/
/**
 * Created by dung on 7/29/15.
 *//*

public class ContactContentObserver extends ContentObserver {

    private Context context;
    private ObserverInterface observerInterface;
    private Uri contactUri = ContactsContract.Contacts.CONTENT_URI;

    long lastTimeofCall = 0L;
    long lastTimeofUpdate = 0L;
    long threshold_time = 10000;

    public ContactContentObserver(Context context, ObserverInterface observerInterface) {
        super(null);
        this.context = context;
        this.observerInterface = observerInterface;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        String editContact = Utility.getPref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_EDIT_CONTACT);
        if(editContact.isEmpty()){
            String addContact = Utility.getPref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_ADD_CONTACT);
            if(!addContact.isEmpty()){
                ContactList contactList = new ContactList(context);
                contactList.startGetContact();
                final ArrayList<Contact> messages = contactList.getContacts();
                if(messages.size() <= 0)
                    return;
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(messages);
                    }
                });
                realm.close();
                contactList.getContacts().clear();
                observerInterface.onChange(contactUri);

                Utility.removePref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_ADD_CONTACT);

            }
        }else {
            Gson gson = Utility.createGson();
            final Contact contact = gson.fromJson(editContact, Contact.class);
            if (contact != null) {
                ContactList contactList = new ContactList(context);
                contactList.startGetContact(String.valueOf(contact.getId()));
                final ArrayList<Contact> messages = contactList.getContacts();
                if(messages.size() <= 0)
                    return;
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(messages);
                    }
                });
                realm.close();
                contactList.getContacts().clear();
                observerInterface.onChange(contactUri);

                Utility.removePref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_EDIT_CONTACT);
            }
        }
    }
}
*/
