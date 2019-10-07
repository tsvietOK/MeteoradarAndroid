package com.tsvietok.meteoradar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.tsvietok.meteoradar.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class NetUtils {
    private static final String DOMAIN = "http://radar.veg.by";
    private static final String JSON_FILE_NAME = "update.json";

    public static String getDomain() {
        return DOMAIN;
    }

    public static String getJsonFromServer(Location location) {
        CustomLog.logDebug("getJsonFromServer()");

        URL jsonUrl = null;
        try {
            jsonUrl = new URL(DOMAIN + "/" + location.getCity() + "/" + JSON_FILE_NAME);
        } catch (MalformedURLException e) {
            CustomLog.logError("Wrong URL:" + e.getMessage());
        }
        URLConnection connection = null;
        try {
            if (jsonUrl != null) {
                connection = jsonUrl.openConnection();
            }
        } catch (IOException e) {
            CustomLog.logError("Can't open connection: " + e.getMessage());
        }

        if (connection != null) {
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
        }

        BufferedReader inputStream = null;
        try {
            if (connection != null) {
                inputStream = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
            }
        } catch (IOException e) {
            CustomLog.logError("Can't get input stream: " + e.getMessage());
        }

        String input = null;
        try {
            if (inputStream != null) {
                input = inputStream.readLine();
            }
        } catch (IOException e) {
            CustomLog.logError("Can't read from stream: " + e.getMessage());
        }
        return input;
    }

    public static Bitmap getBitmapFromServer(String url) {
        CustomLog.logDebug("getBitmapFromServer()");

        URL bitmapUrl = null;
        try {
            bitmapUrl = new URL(url);
        } catch (MalformedURLException e) {
            CustomLog.logError("Wrong URL:" + e.getMessage());
        }
        URLConnection connection = null;
        try {
            if (bitmapUrl != null) {
                connection = bitmapUrl.openConnection();
            }
        } catch (IOException e) {
            CustomLog.logError("Can't open connection: " + e.getMessage());
        }

        if (connection != null) {
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
        }

        Bitmap bitmap = null;
        try {
            if (connection != null) {
                bitmap = BitmapFactory.decodeStream(connection.getInputStream());
            }
        } catch (IOException e) {
            CustomLog.logError("Can't get or decode input stream: " + e.getMessage());
        }
        return bitmap;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = null;
        if (cm != null) {
            capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        }
        if (capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return true;
        }
        return false;
    }
}
