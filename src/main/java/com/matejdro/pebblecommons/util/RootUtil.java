package com.matejdro.pebblecommons.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RootUtil {

    /** @author Kevin Kowalewski */
    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    /** @author Kevin Kowalewski */
    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    /** @author Kevin Kowalewski */
    private static boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su" };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    /** @author Kevin Kowalewski */
    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    public static boolean isRootAccessible()
    {
        try {
            Process proces = Runtime.getRuntime().exec("su");

            DataOutputStream out = new DataOutputStream(proces.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(proces.getInputStream()));
            out.writeBytes("id\n");
            out.flush();

            String line = in.readLine();
            if (line == null) return false;

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}