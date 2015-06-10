package com.matejdro.pebblecommons;

import android.app.Application;
import android.content.Context;

import com.matejdro.pebblecommons.pebble.PebbleTalkerService;

import java.util.UUID;

public abstract class PebbleCompanionApplication extends Application
{
    public abstract UUID getPebbleAppUUID();
    public abstract Class<? extends PebbleTalkerService> getTalkerServiceClass();

    public static PebbleCompanionApplication fromContext(Context context)
    {
        return (PebbleCompanionApplication) context.getApplicationContext();
    }
}
