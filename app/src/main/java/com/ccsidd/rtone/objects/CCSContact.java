package com.ccsidd.rtone.objects;

import android.graphics.Bitmap;

/**
 * Created by dung on 3/26/15.
 */
public class CCSContact extends CCSBaseContact {

    private String name;

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



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
