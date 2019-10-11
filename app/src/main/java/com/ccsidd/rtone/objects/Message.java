package com.ccsidd.rtone.objects;

import android.graphics.Bitmap;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.Required;

/**
 * Created by thinhvo on 10/13/16.
 */

public class Message extends RealmObject {
    @Index
    private String phoneNumber;
    private int type;
    private String body;
    private long time;
    private boolean unRead;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isUnRead() {
        return unRead;
    }

    public void setUnRead(boolean unRead) {
        this.unRead = unRead;
    }
}
