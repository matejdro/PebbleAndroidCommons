package com.matejdro.pebblecommons.pebble;

public enum WatchPlatform
{
    APLITE(false, false),
    BASALT(true, true),
    CHALK(true, true);

    private boolean colors;
    private boolean microphone;

    WatchPlatform(boolean colors, boolean microphone)
    {
        this.colors = colors;
        this.microphone = microphone;
    }

    public boolean hasColors()
    {
        return colors;
    }

    public boolean hasMicrophone()
    {
        return microphone;
    }
}
