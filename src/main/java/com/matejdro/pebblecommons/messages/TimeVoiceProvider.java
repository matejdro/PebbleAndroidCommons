package com.matejdro.pebblecommons.messages;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import com.matejdro.pebblecommons.R;
import com.matejdro.pebblecommons.notification.NotificationCenterExtender;

public class TimeVoiceProvider extends BroadcastReceiver implements MessageTextProvider
{
    private static final String INTENT_ACTION_REMOVE = "com.matejdro.pebblecommon.notificationaction.TIME_VOICE_NOTIFICATION_REMOVE";
    private static final String INTENT_ACTION_TEXT = "com.matejdro.pebblecommon.notificationaction.TIME_VOICE_NOTIFICATION_TEXT";

    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static final int NOTIFICATION_ID = 1867;

    private MessageTextProviderListener textListener;
    private Context context;

    public TimeVoiceProvider(Context context)
    {
        this.context = context;
    }

    @Override
    public void startRetrievingText(MessageTextProviderListener textListener)
    {
        this.textListener = textListener;

        PendingIntent replyIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_TEXT), 0);
        PendingIntent removeIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_REMOVE), PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY).setLabel(context.getString(R.string.voice_prompt_title)).setAllowFreeFormInput(true).build();
        NotificationCompat.Action voiceAction = new NotificationCompat.Action.Builder(0, context.getString(R.string.voice_prompt_title), replyIntent).addRemoteInput(remoteInput).build();

        // Instruction text is always the same so I need to add random character (usually appears as box) and padding to ensure
        // that Pebble duplicate filtering won't affect this.
        char randomCharacter = (char) (System.currentTimeMillis() % Short.MAX_VALUE);
        long padding = System.currentTimeMillis();
        String appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
        String instructions = context.getString(R.string.voice_prompt_instructions, randomCharacter, padding, appName);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.voice_prompt_title))
                .setContentText(instructions)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(instructions))
                .setSmallIcon(android.R.drawable.sym_def_app_icon) //Dummy icon to satisfy android notification requiremet
                .extend(new NotificationCompat.WearableExtender().addAction(voiceAction))
                .extend(new NotificationCenterExtender().setDisableNCNotification(true)) //Prevent NC from displaying this notification
                .setContentIntent(removeIntent) //Add option to remove notification on tap from Android statusbar
                .build();

        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR; //Give it no clear flag to remove Dismiss action from the top.
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification);

        context.registerReceiver(this, new IntentFilter(INTENT_ACTION_TEXT));
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (INTENT_ACTION_TEXT.equals(intent.getAction()))
        {
            String text = RemoteInput.getResultsFromIntent(intent).getCharSequence(EXTRA_VOICE_REPLY).toString();
            textListener.gotText(text);

            context.unregisterReceiver(this);
        }
    }

    public static class VoiceNotificationRemoverReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (INTENT_ACTION_REMOVE.equals(intent.getAction()))
            {
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
            }
        }
    }
}
