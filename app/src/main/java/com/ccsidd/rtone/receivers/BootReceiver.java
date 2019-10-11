package com.ccsidd.rtone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ccsidd.rtone.services.SipServiceCommand;

/**
 * Created by dung on 2/22/16.
 */
public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        SipServiceCommand.start(context);
    }
}
