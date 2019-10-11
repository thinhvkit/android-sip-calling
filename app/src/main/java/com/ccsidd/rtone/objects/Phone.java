package com.ccsidd.rtone.objects;

import io.realm.RealmObject;

/**
 * Created by dung on 7/27/15.
 */
public class Phone extends RealmObject{

    private String number;
    private int type;
    private String label;
    private boolean primary;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
