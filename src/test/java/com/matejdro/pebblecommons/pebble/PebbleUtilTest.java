package com.matejdro.pebblecommons.pebble;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class PebbleUtilTest
{
    @Test
    public void testGetBytesLeft() throws Exception
    {
        PebbleCapabilities testCapabilities = new PebbleCapabilities(false, false, false, false, false, 100);

        PebbleDictionary testDictionary = new PebbleDictionary();
        assertEquals(92, PebbleUtil.getBytesLeft(testDictionary, testCapabilities));

        testDictionary.addInt8(1, (byte) 20);
        assertEquals(84, PebbleUtil.getBytesLeft(testDictionary, testCapabilities));

        testDictionary.addInt32(2, 20);
        assertEquals(73, PebbleUtil.getBytesLeft(testDictionary, testCapabilities));

        testDictionary.addBytes(3, new byte[]{1, 2, 3, 4});
        assertEquals(62, PebbleUtil.getBytesLeft(testDictionary, testCapabilities));

        testDictionary.addString(4, "abcde");
        assertEquals(50, PebbleUtil.getBytesLeft(testDictionary, testCapabilities));

        testDictionary.addUint32(1, 20);
        assertEquals(47, PebbleUtil.getBytesLeft(testDictionary, testCapabilities));
    }
}