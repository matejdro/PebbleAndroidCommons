package com.matejdro.pebblecommons.pebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

public class AckNackReceiver extends BroadcastReceiver {
    private Context context;
    private PebbleCommunication communication;

    public AckNackReceiver(Context context, PebbleCommunication communication) {
        this.context = context;
        this.communication = communication;
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
            communication.receivedAck(transactionId);
        }
        else if ("com.getpebble.action.app.RECEIVE_NACK".equals(intent.getAction()))
        {
            communication.receivedNack(transactionId);
        }

    }
}
