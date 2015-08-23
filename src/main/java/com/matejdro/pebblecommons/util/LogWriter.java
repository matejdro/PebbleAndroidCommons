package com.matejdro.pebblecommons.util;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import timber.log.Timber;

/**
 * Created by Matej on 27.10.2014.
 */
public class LogWriter
{
    private static final String ENABLE_LOG_WRITING = "enableLogWriter";
    private static SharedPreferences.OnSharedPreferenceChangeListener listener;
    private static PrintWriter writer = null;
    private static String appName;

    private static Timber.Tree timberTree;

    public static void init(final SharedPreferences preferences, String appName)
    {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
            {
                if (ENABLE_LOG_WRITING.equals(key))
                {
                    if (preferences.getBoolean(ENABLE_LOG_WRITING, false))
                        open();
                    else
                        close();
                }
            }
        };

        LogWriter.appName = appName;

        preferences.registerOnSharedPreferenceChangeListener(listener);
        if (preferences.getBoolean(ENABLE_LOG_WRITING, false))
            open();
    }

    private static void open()
    {
        File targetFolder = Environment.getExternalStoragePublicDirectory(appName);
        if (!targetFolder.exists())
            targetFolder.mkdir();

        File file = new File(targetFolder, "log.txt");

        try
        {
            writer = new PrintWriter(file);

            timberTree = new TimberLogWriterTree();
            Timber.plant(timberTree);

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void close()
    {
        if (writer == null)
            return;

        writer.close();

        writer = null;
        if (timberTree != null)
        {
            Timber.uproot(timberTree);
            timberTree = null;
        }
    }

    public static void write(String text)
    {
        if (writer != null)
        {
            writer.flush();
        }
    }

    public static boolean isEnabled()
    {
        return writer != null;
    }

    private static class TimberLogWriterTree extends Timber.AppTaggedDebugTree
    {
        @Override
        protected boolean isLoggable(int priority)
        {
            //LogWriter should write everything as long as logging is enabled
            return writer != null;
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t)
        {
            Calendar calendar = Calendar.getInstance();
            writer.write(
                    calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND) +
                    getLogPriorityAbbreviation(priority) + " " + message + "\n"
                        );

            if (t != null)
            {
                t.printStackTrace(writer);
            }

            writer.flush();
        }
    }

    private static char getLogPriorityAbbreviation(int priority)
    {
        switch (priority)
        {
            case Log.ASSERT:
                return 'A';
            case Log.DEBUG:
                return 'D';
            case Log.ERROR:
                return 'E';
            case Log.INFO:
                return 'I';
            case Log.WARN:
                return 'W';
            default:
                return 'V';
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
