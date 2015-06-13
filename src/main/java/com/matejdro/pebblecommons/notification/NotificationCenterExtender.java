package com.matejdro.pebblecommons.notification;

import android.app.Notification;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class NotificationCenterExtender implements NotificationCompat.Extender
{
    private static final String EXTRA_NC_EXTENSIONS = "com.matejdro.pebblenotificationcenter.NOTIFICATION_EXTENSIONS";
    private static final String KEY_DISABLE_NC_NOTIFICATION = "disableNCNotification";

    public NotificationCenterExtender()
    {

    }

    public NotificationCenterExtender(Notification notification)
    {
        Bundle extras = NotificationCompat.getExtras(notification);
        Bundle extenderBundle = extras != null ? extras.getBundle(EXTRA_NC_EXTENSIONS) : null;
        if (extenderBundle != null)
        {
            disableNCNotification = extenderBundle.getBoolean(KEY_DISABLE_NC_NOTIFICATION);
        }
    }

    private boolean disableNCNotification = false;

    public boolean isNCNotificationDisabled()
    {
        return disableNCNotification;
    }

    public NotificationCenterExtender setDisableNCNotification(boolean disableNCNotification)
    {
        this.disableNCNotification = disableNCNotification;

        return this;
    }

    @Override
    public NotificationCompat.Builder extend(NotificationCompat.Builder builder)
    {
        Bundle extensionBundle = new Bundle();
        extensionBundle.putBoolean(KEY_DISABLE_NC_NOTIFICATION, disableNCNotification);

        builder.getExtras().putBundle(EXTRA_NC_EXTENSIONS, extensionBundle);
        return null;
    }
}
