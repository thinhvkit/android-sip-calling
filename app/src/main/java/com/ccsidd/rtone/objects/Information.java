package com.ccsidd.rtone.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by dung on 7/3/15.
 */
public class Information implements Parcelable {

    private ArrayList<CCSContact> contacts;
    private ArrayList<CCSCallLog> callLogs;

    public Information() {

    }

    public ArrayList<CCSContact> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<CCSContact> contacts) {
        this.contacts = contacts;
    }

    public ArrayList<CCSCallLog> getCallLogs() {
        return callLogs;
    }

    public void setCallLogs(ArrayList<CCSCallLog> callLogs) {
        this.callLogs = callLogs;
    }

    // Parcelling part
    public Information(Parcel in){

        Parcelable[] parcelables = in.readParcelableArray(CCSContact.class.getClassLoader());
        if (parcelables != null)
            this.contacts = new ArrayList<CCSContact>(Arrays.asList(Arrays.copyOf(parcelables, parcelables.length, CCSContact[].class)));

        Parcelable[] parcelables1 = in.readParcelableArray(CCSContact.class.getClassLoader());
        if (parcelables1 != null)
            this.callLogs = new ArrayList<CCSCallLog>(Arrays.asList(Arrays.copyOf(parcelables1, parcelables1.length, CCSCallLog[].class)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
//        parcel.writeSerializable(this);
    }
}
