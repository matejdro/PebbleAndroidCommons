package com.matejdro.pebblecommons.pebble;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import com.getpebble.android.kit.util.PebbleDictionary;
import com.matejdro.pebblecommons.util.TimeoutService;

import java.net.URISyntaxException;
import java.util.HashMap;

import timber.log.Timber;

public abstract class PebbleTalkerService extends TimeoutService
{
    public static final String INTENT_PEBBLE_PACKET = "PebblePacket";
    public static final String INTENT_PEBBLE_ACK = "PebbleAck";
    public static final String INTENT_PEBBLE_NACK = "PebbleNack";

    private SharedPreferences settings;

    private PebbleDeveloperConnection devConn;

    private PebbleCommunication pebbleCommunication;
    private AckNackReceiver ackNackReceiver;

    private SparseArray<CommModule> modules = new SparseArray<CommModule>();
    private HashMap<String, CommModule> registeredIntents = new HashMap<String, CommModule>();

    private HandlerThread pebbleProcessorThread;
    private Handler pebbleThreadHandler;

    private boolean enableDeveloperConnectionRefreshing = true;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        if (devConn != null)
            devConn.close();

        ackNackReceiver.unregister();
        pebbleProcessorThread.quit();
    }


    @Override
    public void onCreate()
    {
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        registerModules();

        pebbleProcessorThread = new HandlerThread("PebbleProcessorThread", Thread.NORM_PRIORITY - 1);
        pebbleProcessorThread.start();
        pebbleThreadHandler = new Handler(pebbleProcessorThread.getLooper());

        pebbleCommunication = new PebbleCommunication(this);
        ackNackReceiver = new AckNackReceiver(this, getPebbleThreadHandler(), pebbleCommunication);
        ackNackReceiver.register();

        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId)
    {
        pebbleThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (intent != null && intent.getAction() != null)
                {
                    if (intent.getAction().equals(INTENT_PEBBLE_PACKET))
                    {
                        String json = intent.getStringExtra("packet");
                        receivedPacketFromPebble(json);
                    }
                    else if (intent.getAction().equals(INTENT_PEBBLE_ACK))
                    {
                        int transactionId = intent.getIntExtra("transactionId", -1);
                        getPebbleCommunication().receivedAck(transactionId);
                    }
                    else if (intent.getAction().equals(INTENT_PEBBLE_NACK))
                    {
                        int transactionId = intent.getIntExtra("transactionId", -1);
                        getPebbleCommunication().receivedNack(transactionId);
                    }
                    else
                    {
                        CommModule receivingModule = registeredIntents.get(intent.getAction());
                        if (receivingModule != null)
                            receivingModule.gotIntent(intent);
                    }
                }
            }
        });

        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    protected abstract void registerModules();

    protected void addModule(CommModule module, int id)
    {
        modules.put(id, module);
    }

    public CommModule getModule(int id)
    {
        return modules.get(id);
    }

    public void registerIntent(String action, CommModule module)
    {
        registeredIntents.put(action, module);
    }

    public SparseArray<CommModule> getAllModules()
    {
        return modules;
    }

    public SharedPreferences getGlobalSettings()
    {
        return settings;
    }

    public PebbleCommunication getPebbleCommunication()
    {
        return pebbleCommunication;
    }

    public void runOnPebbleThread(Runnable runnable)
    {
        pebbleThreadHandler.post(runnable);
    }

    public void runOnPebbleThreadDelayed(Runnable runnable, int time)
    {
        pebbleThreadHandler.postDelayed(runnable, time);
    }

    public Handler getPebbleThreadHandler()
    {
        return pebbleThreadHandler;
    }

    private void initDeveloperConnection()
    {
        pebbleThreadHandler.removeCallbacks(developerConnectionRefresher);

        try
        {
            devConn = createDeveloperConnection();
            devConn.connectBlocking();

            if (devConn.isOpen() && enableDeveloperConnectionRefreshing)
                pebbleThreadHandler.postDelayed(developerConnectionRefresher, 9 * 60 * 1000); //Refresh developer connection every 9 minutes to prevent closing on Pebble's side.
        } catch (InterruptedException e)
        {
        } catch (URISyntaxException e)
        {
        }
    }

    protected PebbleDeveloperConnection createDeveloperConnection() throws URISyntaxException {
        return new PebbleDeveloperConnection(this);
    }

    public PebbleDeveloperConnection getDeveloperConnection()
    {
        if (devConn == null || !devConn.isOpen())
            initDeveloperConnection();

        return devConn;
    }

    public void setEnableDeveloperConnectionRefreshing(boolean enableDeveloperConnectionRefreshing) {
        this.enableDeveloperConnectionRefreshing = enableDeveloperConnectionRefreshing;
    }

    private void receivedPacketFromPebble(final String jsonPacket)
    {
        pebbleThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                PebbleDictionary data = null;
                try
                {
                    data = PebbleDictionary.fromJson(jsonPacket);
                } catch (Exception e)
                {
                    Timber.e(e, "Error while parsing PebbleDictionary! %s", jsonPacket);
                    e.printStackTrace();
                    return;
                }


                int destination = data.getUnsignedIntegerAsLong(0).intValue();
                Timber.d("Pebble packet for %d", destination);

                CommModule module = modules.get(destination);
                if (module == null)
                {
                    Timber.w("Destination module does not exist: %d  Packet: (%s).",destination, data.toJsonString());
                    return;
                }

                module.gotMessageFromPebble(data);
            }
        });
    }

    private Runnable developerConnectionRefresher = new Runnable() {
        @Override
        public void run() {
            devConn.close();
            initDeveloperConnection();
        }
    };
}
