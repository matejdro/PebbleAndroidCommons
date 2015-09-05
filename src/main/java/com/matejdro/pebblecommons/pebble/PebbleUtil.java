package com.matejdro.pebblecommons.pebble;

import android.content.Context;
import android.support.annotation.Nullable;

import com.getpebble.android.kit.PebbleKit;

public class PebbleUtil
{
    public static @Nullable PebbleKit.FirmwareVersionInfo getPebbleFirmwareVersion(Context context)
    {
        /**
         * For some reason this method in PebbleKit keeps throwing exceptions.
         * Lets wrap it in try/catch.
         */

        try
        {
            return PebbleKit.getWatchFWVersion(context);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
