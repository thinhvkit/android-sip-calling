package com.ccsidd.rtone.objects;

/**
 * Created by dung on 4/3/15.
 */
public class CCSPhoneNumber {
    private String number;
    private int type;
    private String label;
    private boolean primary;

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return getNumber();
    }
}
