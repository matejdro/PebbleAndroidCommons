package com.matejdro.pebblecommons.pebble;

import org.junit.Test;

import static org.junit.Assert.*;

public class PebbleCapabilitiesTest
{
    @Test
    public void equalsTest()
    {
        PebbleCapabilities firstCapabilities = new PebbleCapabilities(true, false, true, false, true, 9753);
        PebbleCapabilities secondCapabilities = new PebbleCapabilities(true, false, true, false, true, 9753);
        assertEquals(firstCapabilities, secondCapabilities);

        secondCapabilities = new PebbleCapabilities(false, false, true, false, true, 9753);
        assertNotEquals(firstCapabilities, secondCapabilities);

        secondCapabilities = new PebbleCapabilities(true, true, true, false, true, 9753);
        assertNotEquals(firstCapabilities, secondCapabilities);

        secondCapabilities = new PebbleCapabilities(true, false, false, false, true, 9753);
        assertNotEquals(firstCapabilities, secondCapabilities);

        secondCapabilities = new PebbleCapabilities(true, false, true, true, true, 9753);
        assertNotEquals(firstCapabilities, secondCapabilities);

        secondCapabilities = new PebbleCapabilities(true, false, true, false, false, 9753);
        assertNotEquals(firstCapabilities, secondCapabilities);

        secondCapabilities = new PebbleCapabilities(true, false, true, false, true, 9754);
        assertNotEquals(firstCapabilities, secondCapabilities);

        secondCapabilities = new PebbleCapabilities(false, true, false, true, false, 0);
        assertNotEquals(firstCapabilities, secondCapabilities);

    }

    @Test
    public void testSerialization()
    {
        PebbleCapabilities originalCapabilities = new PebbleCapabilities(true, false, true, false, true, 9753);
        PebbleCapabilities reserializedCapabilities = PebbleCapabilities.fromSerializedForm(originalCapabilities.serialize());
        assertEquals(originalCapabilities, reserializedCapabilities);

        originalCapabilities = new PebbleCapabilities(false, true, false, true, false, 7777);
        reserializedCapabilities = PebbleCapabilities.fromSerializedForm(originalCapabilities.serialize());
        assertEquals(originalCapabilities, reserializedCapabilities);

        originalCapabilities = new PebbleCapabilities(false, false, false, false, false, 0xFF);
        reserializedCapabilities = PebbleCapabilities.fromSerializedForm(originalCapabilities.serialize());
        assertEquals(originalCapabilities, reserializedCapabilities);

        originalCapabilities = new PebbleCapabilities(true, true, true, true, true, 0xFF);
        reserializedCapabilities = PebbleCapabilities.fromSerializedForm(originalCapabilities.serialize());
        assertEquals(originalCapabilities, reserializedCapabilities);

        originalCapabilities = new PebbleCapabilities(false, false, false, false, false, 0);
        reserializedCapabilities = PebbleCapabilities.fromSerializedForm(originalCapabilities.serialize());
        assertEquals(originalCapabilities, reserializedCapabilities);

        originalCapabilities = new PebbleCapabilities(true, true, true, true, true, 0);
        reserializedCapabilities = PebbleCapabilities.fromSerializedForm(originalCapabilities.serialize());
        assertEquals(originalCapabilities, reserializedCapabilities);
    }
}