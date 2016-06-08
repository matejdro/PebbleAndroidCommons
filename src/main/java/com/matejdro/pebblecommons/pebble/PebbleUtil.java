package com.matejdro.pebblecommons.pebble;

import android.content.Context;
import android.support.annotation.Nullable;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;

import java.util.Iterator;

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

    /**
     * @param message Current AppMessage.
     * @param pebbleCapabilities Capabilities of the Pebble you want to send message to
     * @return Maximum size of the byte array than you still can add to this message and have it successfully sent.
     *
     * @see <a href="https://developer.pebble.com/docs/c/Foundation/Dictionary/#dict_calc_buffer_size">Pebble documentation</a>
     */
    public static int getBytesLeft(PebbleDictionary message, PebbleCapabilities pebbleCapabilities)
    {
        int bytesLeft = pebbleCapabilities.getMaxAppmessageSize();

        bytesLeft--; // One byte of dictionary header overhead (presumably for number of entries in AppMessage)

        for (PebbleTuple entry : message)
        {
            bytesLeft -= 7; //Every entry has 7 bytes of overhead.
            bytesLeft -= entry.length;
        }

        return bytesLeft - 7; // 7 bytes is the additional overhead that would to-be-added byte array need
    }
}
