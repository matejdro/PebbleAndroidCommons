package com.matejdro.pebblecommons.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by Matej on 7.1.2015.
 */
public class AccountRetreiver
{
    public static File copyAccounts(Context context)
    {
        String accountDbPath;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            UserHandle uh = android.os.Process.myUserHandle();
            UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);

            long userId = um.getSerialNumberForUser(uh);

            accountDbPath = "/data/system/users/" + userId + "/accounts.db";
        }
        else
        {
            accountDbPath = "/data/system/accounts.db";
        }

        try {
            File resultFile = new File(context.getCacheDir(), "accounts.db");
            String cmdLine = "cat " + accountDbPath + " > " + resultFile.getAbsolutePath();

            String[] args = new String[] {"su", "-c", cmdLine };
            Process process = Runtime.getRuntime().exec(args);
            process.waitFor();

            cmdLine = "chmod 777 " + resultFile.getAbsolutePath();
            args = new String[] {"su", "-c", cmdLine };
            process = Runtime.getRuntime().exec(args);
            process.waitFor();

            return resultFile;

        } catch (IOException e) {
        } catch (InterruptedException e) {
        }

        return null;
    }

    public static String getPebbleAccountToken(File accountsDbFile)
    {
        SQLiteDatabase db = null;
        try
        {
            db = SQLiteDatabase.openDatabase(accountsDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);

            if (db == null || !db.isOpen())
                return null;

            Cursor cursor = db.rawQuery("SELECT authtoken FROM authtokens WHERE type = \"com.getpebble\" LIMIT 1", null);
            if (!cursor.moveToNext())
            {
                cursor.close();
                return null;
            }

            String token = cursor.getString(0);
            cursor.close();

            return token;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (db != null)
                db.close();
        }

        return  null;
    }

}
