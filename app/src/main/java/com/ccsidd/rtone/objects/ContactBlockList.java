package com.ccsidd.rtone.objects;

import io.realm.RealmObject;

/**
 * Created by dung on 7/27/15.
 */
public class ContactBlockList extends RealmObject {
    private String phoneNumbers;
    private String displayName;
    private String imageUri;

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}