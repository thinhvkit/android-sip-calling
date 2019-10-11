package com.ccsidd.rtone.objects;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by thinhvo on 10/13/16.
 */

public class Conversation extends RealmObject {
    @PrimaryKey
    private String phoneNumber;
    private int type;
    private String lastMessage;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
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
