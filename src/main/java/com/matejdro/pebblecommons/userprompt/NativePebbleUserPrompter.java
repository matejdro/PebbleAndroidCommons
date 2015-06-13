package com.matejdro.pebblecommons.userprompt;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.matejdro.pebblecommons.R;

public class NativePebbleUserPrompter implements UserPrompter
{
    private static final String INTENT_ACTION_REMOVE = "com.matejdro.pebblecommon.notificationaction.NATIVE_PEBBLE_PROMPT_REMOVE";
    private static final int NOTIFICATION_ID = 47489;


    private Context context;

    public NativePebbleUserPrompter(Context context)
    {
        this.context = context;
    }

    @Override
    public void promptUser(String title, @Nullable String subtitle, String body, PromptAnswer... answers)
    {

        // Instruction text is always the same so I need to add random character (usually appears as box) and padding to ensure
        // that Pebble duplicate filtering won't affect this.
        char randomCharacter = (char) (System.currentTimeMillis() % Short.MAX_VALUE);
        long padding = System.currentTimeMillis();
        String instructions = context.getString(R.string.native_pebble_user_prompter_body, randomCharacter, body, padding);

        PendingIntent removeIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_REMOVE), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
        for (PromptAnswer answer : answers)
        {
            PendingIntent answerIntent = PendingIntent.getBroadcast(context, 0, answer.getAction(), PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, answer.getText(), answerIntent).build();
            wearableExtender.addAction(action);
        }

        if (subtitle != null && !subtitle.isEmpty())
            title += " - " + subtitle;

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(instructions)
                .setSmallIcon(android.R.drawable.sym_def_app_icon) //Dummy icon to satisfy android notification requiremet
                .extend(wearableExtender)
                .setContentIntent(removeIntent) //Add option to remove notification on tap from Android statusbar
                .build();

        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR; //Give it no clear flag to remove Dismiss action from the top.
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification);
    }

    public static class NotificationRemoverReceiver extends BroadcastReceiver
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
