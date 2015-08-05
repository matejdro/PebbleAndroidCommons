package com.matejdro.pebblecommons.pebble;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Matej on 7.1.2015.
 */
public class PebbleSiteAPI
{
    public static final String LOCKER_JSON_URL = "https://dev-portal.getpebble.com/api/users/locker?platform=android";
    public static final String APP_INFO_JSON_URL = "https://dev-portal.getpebble.com/api/applications/";

    public static String getLockerJson(String authToken)
    {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(LOCKER_JSON_URL).openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
            InputStream stream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder builder = new StringBuilder();
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                    break;

                builder.append(line);
            }

            reader.close();

            return builder.toString();
        } catch (IOException e) {
        }

        return null;
    }

    public static String getPbwUrlFromAppstoreID(String id)
    {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(APP_INFO_JSON_URL + id).openConnection();
            InputStream stream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder builder = new StringBuilder();
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                    break;

                builder.append(line);
            }

            reader.close();

            String fullJsonText = builder.toString();
            JSONObject json = new JSONObject(fullJsonText);
            JSONArray applications = json.getJSONArray("applications");
            if (applications.length() < 1)
                return null;

            JSONObject application = applications.getJSONObject(0);
            return application.getString("pbw_file");


        } catch (IOException e) {
        } catch (JSONException e) {
        }

        return null;

    }

    public static boolean downloadPebbleApp(String url, File destination)
    {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(destination);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
    }
}
