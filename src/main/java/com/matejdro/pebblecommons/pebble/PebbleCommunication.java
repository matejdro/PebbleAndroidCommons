package com.matejdro.pebblecommons.pebble;

import android.content.Context;
import android.content.SharedPreferences;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.Deque;
import java.util.LinkedList;

import com.matejdro.pebblecommons.PebbleCompanionApplication;
import timber.log.Timber;

public class PebbleCommunication
{
    public static final int PEBBLE_PLATFORM_APLITE = 0;
    public static final int PEBBLE_PLATFORM_BASSALT = 1;

    private PebbleTalkerService talkerService;

    private Deque<CommModule> queuedModules;
    private int lastSentPacket;
    private boolean commBusy;

    private PebbleDictionary lastPacket;
    private boolean retriedNack;

    private int connectedPebblePlatform;

    public PebbleCommunication(PebbleTalkerService talkerService)
    {
        this.talkerService = talkerService;
        queuedModules = new LinkedList<CommModule>();
        commBusy = false;
        lastSentPacket = -1;
        retriedNack = false;

        connectedPebblePlatform = talkerService.getGlobalSettings().getInt("LastConnectedPebblePlatform", PEBBLE_PLATFORM_APLITE);
    }

    public void sendToPebble(PebbleDictionary packet)
    {
        lastSentPacket = (lastSentPacket + 1) % 255;
        Timber.d("SENT " + lastSentPacket);

        this.lastPacket = packet;

        PebbleKit.sendDataToPebbleWithTransactionId(talkerService, PebbleCompanionApplication.fromContext(talkerService).getPebbleAppUUID(), packet, lastSentPacket);

        commBusy = true;
        retriedNack = false;
    }

    public void sendNext()
    {
        Timber.d("SendNext " + commBusy);

        if (commBusy)
            return;

        while (!queuedModules.isEmpty())
        {
            CommModule nextModule = queuedModules.peek();

            Timber.d("SendNextModule " + nextModule.getClass().getSimpleName());

            if (nextModule.sendNextMessage())
                return;

            queuedModules.removeFirst();
        }

        Timber.d("Comm idle!");
    }

    public void receivedAck(int transactionId)
    {
        Timber.d("ACK " + transactionId);

        if (transactionId != lastSentPacket || lastPacket == null)
        {
            Timber.w("Got invalid ACK");
            return;
        }

        commBusy = false;
        lastPacket = null;
        sendNext();
    }

    public void receivedNack(int transactionId)
    {
        Timber.d("NACK " + transactionId);
        if (transactionId != lastSentPacket || lastPacket == null)
        {
            Timber.w("Got invalid NACK");
            return;
        }

        commBusy = false;

        // Retry sending packet once. If we got NACK 2 times in a row, it probably means Pebble app was closed.
        if (!retriedNack)
        {
            Timber.d("Retrying last message...");
            sendToPebble(lastPacket);
            retriedNack = true;
            return;
        }

        lastPacket = null;
    }

    public void resetBusy()
    {
        commBusy = false;
    }

    public void queueModule(CommModule module)
    {
        if (queuedModules.contains(module))
            queuedModules.remove(module);

        queuedModules.addLast(module);
    }

    public void queueModulePriority(CommModule module)
    {
        if (queuedModules.contains(module))
            queuedModules.remove(module);

        queuedModules.addFirst(module);
    }

    public int getConnectedPebblePlatform()
    {
        return connectedPebblePlatform;
    }

    public void setConnectedPebblePlatform(int connectedPebblePlatform)
    {
        this.connectedPebblePlatform = connectedPebblePlatform;

        SharedPreferences.Editor editor = talkerService.getGlobalSettings().edit();
        editor.putInt("LastConnectedPebblePlatform", connectedPebblePlatform);
        editor.apply();
    }
}
