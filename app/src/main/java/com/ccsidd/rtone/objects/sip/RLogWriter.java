package com.ccsidd.rtone.objects.sip;

import android.util.Log;

import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;

/**
 * Created by dung on 1/25/16.
 */
public class RLogWriter extends LogWriter {
    @Override
    public void write(LogEntry entry)
    {
        Log.e("pjsip-log", entry.getMsg());
    }
}
