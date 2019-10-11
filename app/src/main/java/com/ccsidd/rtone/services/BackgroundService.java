package com.ccsidd.rtone.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;

/**
 * Service with a background worker thread.
 *
 * @author gotev (Aleksandar Gotev)
 */
class BackgroundService extends Service {

    private HandlerThread mWorkerThread;
    private Handler mHandler;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        //mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
        mWakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");

        mWakeLock.acquire();

        mWorkerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_FOREGROUND);
        mWorkerThread.setPriority(Thread.MAX_PRIORITY);
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWorkerThread.quitSafely();
        }else
            mWorkerThread.quit();

        if (mWakeLock.isHeld())
            mWakeLock.release();
    }

    protected void enqueueJob(Runnable job) {
        mHandler.post(job);
    }
}
