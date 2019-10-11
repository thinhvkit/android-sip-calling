package com.ccsidd.rtone.objects;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.CursorLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by dung on 7/24/15.
 */
public class ContactList {

    private ArrayList<Contact> contacts = new ArrayList<>();
    private Context context;

    public ContactList(Context context) {
        this.context = context;
    }

    public static boolean contains(RealmList<Phone> phones, String phone) {
        for (Phone p : phones) {
            if (p != null && p.getNumber().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    /*public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public void startGetContact(int type) {
        contacts.clear();
        contacts.addAll(getContactList(type));
    }

    public void startGetContact(String id) {
        contacts.clear();
        contacts.addAll(getContactUpdate(id));
    }

    public void startGetContact() {
        contacts.clear();
        contacts.addAll(getContactLast());
    }

    public ArrayList<Contact> getContactList(int type) {
        ArrayList<Contact> ccsContacts = new ArrayList<>();
        String sort = ContactsContract.Contacts.DISPLAY_NAME + " ASC";
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        try {
            String time = "";
            if (type == 0)
                time = Utility.getPref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.CONTACT_LAST_UPDATED_TIMESTAMP);

            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
                    + " > ?", new String[]{time.equalsIgnoreCase("") ? "0" : time}, sort);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Contact contact = getContact(cursor);
                cursor.moveToNext();
                if (contact == null)
                    continue;
                RealmList<Phone> phone = getAllNumber(contact.getId());
                if (phone.size() > 0) {
                    contact.setPhoneNumbers(phone);
                    ccsContacts.add(contact);
                }

                Utility.savePref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.CONTACT_LAST_UPDATED_TIMESTAMP, new Date().getTime() + "");
            }
            cursor.close();
        } catch (Exception e) {
            return ccsContacts;
        }
        return ccsContacts;
    }

    public ArrayList<Contact> getContactUpdate(String id) {
        ArrayList<Contact> ccsContacts = new ArrayList<>();
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        try {
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts._ID
                    + " = ?", new String[]{id + ""}, null);
            cursor.moveToFirst();
            Contact contact = getContact(cursor);
            if (contact != null) {
                RealmList<Phone> phone = getAllNumber(contact.getId());
                if (phone.size() > 0) {
                    contact.setPhoneNumbers(phone);
                    ccsContacts.add(contact);
                }
                cursor.close();
            }
        } catch (Exception e) {
            return ccsContacts;
        }
        return ccsContacts;
    }

    public ArrayList<Contact> getContactLast() {
        ArrayList<Contact> ccsContacts = new ArrayList<>();
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        try {
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
                    + " > ?", new String[]{"1471412547315"}, null);
            cursor.moveToLast();
            Contact contact = getContact(cursor);
            if (contact != null) {
                RealmList<Phone> phone = getAllNumber(contact.getId());
                if (phone.size() > 0) {
                    contact.setPhoneNumbers(phone);
                    ccsContacts.add(contact);

                    Utility.savePref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.CONTACT_LAST_UPDATED_TIMESTAMP, new Date().getTime() + "");
                }
                cursor.close();
            }
        } catch (Exception e) {
            return ccsContacts;
        }
        return ccsContacts;
    }

    public Contact getContact(Cursor cursor) {
        Contact contact = new Contact();
//        if (Integer.parseInt(cursor.getString(cursor
//                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) < 0) {
//            return null;
//        }

        if (cursor.isNull(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                || cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)) == null)
            contact.setImageUri("");
        else
            contact.setImageUri(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)));
        contact.setDisplayName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
        contact.setId(cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
        return contact;
    }*/

    public RealmList<Phone> getAllNumber(int contactID) {
        RealmList<Phone> phoneNumbers = new RealmList<>();
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                        + " = ?", new String[]{contactID + ""}, null);
        while (cursor.moveToNext()) {
            String accountName = cursor.getString(cursor.getColumnIndex("account_name"));
            String number = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));

            Phone phone = new Phone();
            phone.setNumber(number);
            phone.setType(cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
            phone.setLabel(accountName);
            phone.setPrimary(cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) != 0);
            phoneNumbers.add(phone);

        }
        cursor.close();
        return phoneNumbers;
    }

    public ArrayList<Contact> fetchAll() {
        String[] projectionFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
        };
        ArrayList<Contact> listContacts = new ArrayList<>();

        Cursor c = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields, // the columns to retrieve
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1", // the selection criteria (none)
                null, // the selection args (none)
                null // the sort order (default)
        );

        final Map<String, Contact> contactsMap = new HashMap<>(c.getCount());

        if (c.moveToFirst()) {

            int idIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
            int nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

            do {
                String contactId = c.getString(idIndex);
                String contactDisplayName = c.getString(nameIndex);
                Contact contact = new Contact();
                contact.setId(Integer.parseInt(contactId));
                contact.setDisplayName(contactDisplayName);
                contact.setImageUri("");
                contactsMap.put(contactId, contact);
                listContacts.add(contact);
            } while (c.moveToNext());

        }

        c.close();

        matchContactNumbers(contactsMap);
//        matchContactEmails(contactsMap);

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(listContacts);
        realm.commitTransaction();
        realm.close();

        return listContacts;
    }

    public void matchContactNumbers(Map<String, Contact> contactsMap) {
        // Get numbers
        final String[] numberProjection = new String[]{
                android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE,
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        };

        Cursor phone = context.getContentResolver().query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                numberProjection,
                null,
                null,
                null);

        if (phone.moveToFirst()) {
            final int contactNumberColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            final int contactTypeColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            final int contactIdColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);

            while (!phone.isAfterLast()) {
                final String number = phone.getString(contactNumberColumnIndex);
                final String contactId = phone.getString(contactIdColumnIndex);
                Contact contact = contactsMap.get(contactId);
                if (contact == null || contains(contact.getPhoneNumbers(), number)) {
                    phone.moveToNext();
                    continue;
                }

                int type = phone.getInt(contactTypeColumnIndex);
                String customLabel = "Custom";
                CharSequence phoneLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel);
                Phone p = new Phone();
                p.setNumber(number);
                p.setLabel(phoneLabel.toString());
                p.setPrimary(false);
                p.setType(type);

                contact.getPhoneNumbers().add(p);
                phone.moveToNext();
            }
        }

        phone.close();

    }

    public void matchContactEmails(Map<String, Contact> contactsMap) {
        // Get email
        final String[] emailProjection = new String[]{
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
        };

        Cursor email = new CursorLoader(context,
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                emailProjection,
                null,
                null,
                null).loadInBackground();

        if (email.moveToFirst()) {
            final int contactEmailColumnIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            final int contactTypeColumnIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
            final int contactIdColumnsIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);

            while (!email.isAfterLast()) {
                final String address = email.getString(contactEmailColumnIndex);
                final String contactId = email.getString(contactIdColumnsIndex);
                final int type = email.getInt(contactTypeColumnIndex);
                String customLabel = "Custom";
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    continue;
                }
                CharSequence emailType = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.getResources(), type, customLabel);
                //contact.addEmail(address, emailType.toString());
                email.moveToNext();
            }
        }

        email.close();
    }

}
