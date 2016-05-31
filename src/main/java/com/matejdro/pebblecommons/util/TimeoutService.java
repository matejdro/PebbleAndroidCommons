package com.matejdro.pebblecommons.util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.CallSuper;

/**
 * Service that stops itself after not receiving any commands for specified amount of time
 */
public abstract class TimeoutService extends Service {
    private int timeoutTime = 0;
    private Handler timeoutHandler;

    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();

        timeoutHandler = new Handler();
    }

    @Override
    @CallSuper
    public int onStartCommand(Intent intent, int flags, int startId) {
        rescheduleTimeout();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Enable timeout. This must be called to enable the timeout.
     * Must be called after {@code super.onCreate()}.
     *
     * @param delay timeout delay in milliseconds.
     */
    public void enableTimeout(int delay)
    {
        timeoutTime = delay;
        rescheduleTimeout();
    }

    /**
     * Stop the timeout. It can be restarted later using #enableTimeout
     * Must be called after {@code super.onCreate()}.
     */
    public void stopDelay()
    {
        timeoutTime = 0;
        timeoutHandler.removeCallbacks(serviceStopRunnable);
    }

    private void rescheduleTimeout()
    {
        if (timeoutTime <= 0)
            return;

        timeoutHandler.removeCallbacks(serviceStopRunnable);
        timeoutHandler.postDelayed(serviceStopRunnable, timeoutTime);
    }

    /**
     * Override to to have final control whether service can stop itself after timeout has ran out.
     */
    protected boolean canServiceTimeout()
    {
        return true;
    }

    private Runnable serviceStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopSelf();
        }
    };
}
