package com.ccsidd.rtone.objects;

import android.graphics.Bitmap;

/**
 * Created by dung on 3/26/15.
 */
public class CCSCallLog extends  CCSBaseContact{
    private int type;
    private String name;
    private String number;
    private int numberType;
    private String numberLabel;
    private long date;
    private String country;
    private String imageUri;
    // use for api < 14
    private Bitmap imageBitMap;

    public Bitmap getImageBitMap() {
        return imageBitMap;
    }

    public void setImageBitMap(Bitmap imageBitMap) {
        this.imageBitMap = imageBitMap;
    }
    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getNumberLabel() {
        return numberLabel;
    }

    public void setNumberLabel(String numberLabel) {
        this.numberLabel = numberLabel;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getNumberType() {
        return numberType;
    }

    public void setNumberType(int numberType) {
        this.numberType = numberType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
