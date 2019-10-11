package com.ccsidd.rtone.objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dung on 7/27/15.
 */
public class Contact extends RealmObject{
    @PrimaryKey
    private int id;

//    private int idContact;
//    private boolean isDelete;
    private String displayName;
    private String imageUri;
    private RealmList<Phone> phoneNumbers;

    public Contact()
    {
        phoneNumbers = new RealmList<>();
    }

    public RealmList<Phone> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(RealmList<Phone> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /*public int getIdContact() {
        return idContact;
    }

    public void setIdContact(int idContact) {
        this.idContact = idContact;
    }*/

//    public boolean isDelete() {
//        return isDelete;
//    }
//
//    public void setIsDelete(boolean isDelete) {
//        this.isDelete = isDelete;
//    }
}