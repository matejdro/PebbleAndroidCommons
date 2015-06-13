package com.matejdro.pebblecommons;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import com.matejdro.pebblecommons.pebble.PebbleTalkerService;

import java.util.Map;
import java.util.UUID;

public abstract class PebbleCompanionApplication extends Application
{
    private static PebbleCompanionApplication instance;

    @Override
    public void onCreate()
    {
        instance = this;

        super.onCreate();
    }

    public static PebbleCompanionApplication getInstance()
    {
        return instance;
    }

    public abstract UUID getPebbleAppUUID();
    public abstract Class<? extends PebbleTalkerService> getTalkerServiceClass();

    public @Nullable Map<String, String> getTextReplacementTable()
    {
        return null;
    }

    public static PebbleCompanionApplication fromContext(Context context)
    {
        return (PebbleCompanionApplication) context.getApplicationContext();
    }
}
