package com.matejdro.pebblecommons.vibration;

import com.matejdro.pebblecommons.vibration.PebbleVibrationPattern;

import org.junit.Test;

import static org.junit.Assert.*;

public class PebbleVibrationPatternTest
{
    @Test
    public void testValidation()
    {
        assertFalse(PebbleVibrationPattern.validateVibrationPattern(""));
        assertFalse(PebbleVibrationPattern.validateVibrationPattern("a"));
        assertFalse(PebbleVibrationPattern.validateVibrationPattern("1, b, 3"));
        assertTrue(PebbleVibrationPattern.validateVibrationPattern("1, 2, 3"));
        assertTrue(PebbleVibrationPattern.validateVibrationPattern("100, 50, 1000"));
        assertFalse(PebbleVibrationPattern.validateVibrationPattern("-100, 50, 1000"));
    }

    @Test
    public void testParsing()
    {
        assertArrayEquals(new Byte[] { 0, 0 }, PebbleVibrationPattern.parseVibrationPattern("").toArray(new Byte[0]));
        assertArrayEquals(new Byte[] { 10, 0, 10, 0 }, PebbleVibrationPattern.parseVibrationPattern("10, 10").toArray(new Byte[0]));
        assertArrayEquals(new Byte[] { 0x10, 0x27 }, PebbleVibrationPattern.parseVibrationPattern("20000, 5000, 100, 20").toArray(new Byte[0]));
        assertArrayEquals(
                new Byte[] { 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0, 8, 0, 9, 0, 10, 0, 11, 0, 12, 0, 13, 0, 14, 0, 15, 0, 16, 0, 17, 0, 18, 0, 19, 0, 20, 0 },
                PebbleVibrationPattern.parseVibrationPattern("1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24").toArray(new Byte[0]));
    }
}