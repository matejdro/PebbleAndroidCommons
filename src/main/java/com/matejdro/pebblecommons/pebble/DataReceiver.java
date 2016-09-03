package com.matejdro.pebblecommons.pebble;

import static com.getpebble.android.kit.Constants.APP_UUID;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

import java.util.UUID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.getpebble.android.kit.PebbleKit;

import com.matejdro.pebblecommons.PebbleCompanionApplication;


public class DataReceiver extends BroadcastReceiver {
    public void receiveData(final Context context, final int transactionId, final String jsonPacket, Class<? extends PebbleTalkerService> talkerClass)
    {
        PebbleKit.sendAckToPebble(context, transactionId);

        Intent intent = new Intent(context, talkerClass);
        intent.setAction(PebbleTalkerService.INTENT_PEBBLE_PACKET);
        intent.putExtra("packet", jsonPacket);
        context.startService(intent);
    }

    public void onReceive(final Context context, final Intent intent) {
        final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);

        PebbleCompanionApplication application = PebbleCompanionApplication.fromContext(context);

        if (!"com.getpebble.action.app.RECEIVE".equals(intent.getAction()))
            return;

        final UUID receivedUuid = (UUID) intent.getSerializableExtra(APP_UUID);

        // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts containing their UUID
        if (!application.getPebbleAppUUID().equals(receivedUuid)) {
            return;
        }

        final String jsonData = intent.getStringExtra(MSG_DATA);
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }

        receiveData(context, transactionId, jsonData, application.getTalkerServiceClass());
    }
}
