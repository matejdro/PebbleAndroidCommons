package com.matejdro.pebblecommons.pebble;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.os.Handler;

import com.matejdro.pebblecommons.PebbleCompanionApplication;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

public class AckNackReceiver extends BroadcastReceiver {
    private Context context;

    public AckNackReceiver(Context context) {
        this.context = context;
    }

    public void register()
    {
        context.registerReceiver(this, new IntentFilter("com.getpebble.action.app.RECEIVE_ACK"));
        context.registerReceiver(this, new IntentFilter("com.getpebble.action.app.RECEIVE_NACK"));
    }

    public void unregister()
    {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);

        if ("com.getpebble.action.app.RECEIVE_ACK".equals(intent.getAction()))
        {
            Intent outIntent = new Intent(context, PebbleCompanionApplication.getInstance().getTalkerServiceClass());
            outIntent.setAction(PebbleTalkerService.INTENT_PEBBLE_ACK);
            outIntent.putExtra("transactionId", transactionId);
            context.startService(outIntent);
        }
        else if ("com.getpebble.action.app.RECEIVE_NACK".equals(intent.getAction()))
        {
            Intent outIntent = new Intent(context, PebbleCompanionApplication.getInstance().getTalkerServiceClass());
            outIntent.setAction(PebbleTalkerService.INTENT_PEBBLE_NACK);
            outIntent.putExtra("transactionId", transactionId);
            context.startService(outIntent);
        }

    }
}
