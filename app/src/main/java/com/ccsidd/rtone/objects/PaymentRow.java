package com.ccsidd.rtone.objects;

/**
 * Created by ccsidd on 12/6/16.
 */

public class PaymentRow {
    private String imageSource;
    private String title;
    private String cost;
    private String note;
    private String key;

    public PaymentRow(){

    }
    public String getKey(){ return key ;}

    public void setKey(String key) {
        this.key = key;
    }

    public String getPhoneNumbers() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
    public String getNote(){
        return note;
    }
    public void setNote(String note){
        this.note = note;
    }
}
