package com.ccsidd.rtone.listeners;

/**
 * Created by dung on 6/26/15.
 */
public interface PhoneListener {

    void onItemClickListener(int position, Object data);

    void onCall(String phoneNumber);

    void onMessage(String phoneNumber);
}
