package com.matejdro.pebblecommons.pebble;

import org.java_websocket.WebSocket;

/**
 * Class that defines the capabilites of the connected watch
 */
public class PebbleCapabilities
{
    public static int MINIMUM_APPMESSAGE_INBOX_SIZE = 124;
    public static final int BASIC_CAPABILITIES_SERIALIZED = new PebbleCapabilities().serialize();

    public PebbleCapabilities(boolean microphone, boolean colorScreen, boolean roundScreen, boolean smartStraps, boolean health, int maxAppmessageSize)
    {
        this.microphone = microphone;
        this.colorScreen = colorScreen;
        this.roundScreen = roundScreen;
        this.smartStraps = smartStraps;
        this.health = health;
        this.maxAppmessageSize = maxAppmessageSize;
    }

    /**
     * Creates most basic common denominator Capabilities
     */
    public PebbleCapabilities()
    {
        this.microphone = false;
        this.colorScreen = false;
        this.roundScreen = false;
        this.smartStraps = false;
        this.health = false;
        this.maxAppmessageSize = MINIMUM_APPMESSAGE_INBOX_SIZE;
    }

    private boolean microphone;
    private boolean colorScreen;
    private boolean roundScreen;
    private boolean smartStraps;
    private boolean health;
    private int maxAppmessageSize;

    public boolean hasMicrophone()
    {
        return microphone;
    }

    public boolean hasColorScreen()
    {
        return colorScreen;
    }

    public boolean hasRoundScreen()
    {
        return roundScreen;
    }

    public boolean hasSmartStraps()
    {
        return smartStraps;
    }

    public boolean hasPebbleHealth()
    {
        return health;
    }

    public int getMaxAppmessageSize()
    {
        return maxAppmessageSize;
    }

    /**
     * @return Capabilities compacted into one unsigned integer, for easy transfer or storage
     * @see PebbleCapabilities#fromSerializedForm(int)
     */
    public int serialize()
    {
        int serializedCapabilities = 0;

        serializedCapabilities |= hasMicrophone() ? 0x01 : 0x00;
        serializedCapabilities |= hasColorScreen() ? 0x02 : 0x00;
        serializedCapabilities |= hasRoundScreen() ? 0x04 : 0x00;
        serializedCapabilities |= hasSmartStraps() ? 0x08 : 0x00;
        serializedCapabilities |= hasPebbleHealth() ? 0x10 : 0x00;
        serializedCapabilities |= getMaxAppmessageSize() << 16;

        return serializedCapabilities;
    }

    /**
     * Serialized form of Pebble Capabilities is 32-bit unsigned integer containing:
     * <ul>
     *     <li>Bit 0 - Does watch have microphone</li>
     *     <li>Bit 1 - Does watch have color screen</li>
     *     <li>Bit 2 - Does watch have round screen</li>
     *     <li>Bit 3 - Does watch support smartstraps</li>
     *     <li>Bit 4 - Does watch support Pebble Health</li>
     *     <li>Bits 15-31 - Unsigned 16-bit integer representing number of maximum bytes that can fit into one appmessage</li>
     * </ul>
     */
    public static PebbleCapabilities fromSerializedForm(int serializedCapabilities)
    {
        PebbleCapabilities capabilities = new PebbleCapabilities();

        capabilities.microphone = (serializedCapabilities & 0x01) != 0;
        capabilities.colorScreen = (serializedCapabilities & 0x02) != 0;
        capabilities.roundScreen = (serializedCapabilities & 0x04) != 0;
        capabilities.smartStraps = (serializedCapabilities & 0x08) != 0;
        capabilities.health = (serializedCapabilities & 0x10) != 0;
        capabilities.maxAppmessageSize = serializedCapabilities >> 16;

        return capabilities;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PebbleCapabilities that = (PebbleCapabilities) o;

        if (microphone != that.microphone) return false;
        if (colorScreen != that.colorScreen) return false;
        if (roundScreen != that.roundScreen) return false;
        if (smartStraps != that.smartStraps) return false;
        if (health != that.health) return false;
        return maxAppmessageSize == that.maxAppmessageSize;

    }

    @Override
    public int hashCode()
    {
        int result = (microphone ? 1 : 0);
        result = 31 * result + (colorScreen ? 1 : 0);
        result = 31 * result + (roundScreen ? 1 : 0);
        result = 31 * result + (smartStraps ? 1 : 0);
        result = 31 * result + (health ? 1 : 0);
        result = 31 * result + maxAppmessageSize;
        return result;
    }

    @Override
    public String toString()
    {
        return "PebbleCapabilities{" +
                "microphone=" + microphone +
                ", colorScreen=" + colorScreen +
                ", roundScreen=" + roundScreen +
                ", smartStraps=" + smartStraps +
                ", health=" + health +
                ", maxAppmessageSize=" + maxAppmessageSize +
                '}';
    }
}
