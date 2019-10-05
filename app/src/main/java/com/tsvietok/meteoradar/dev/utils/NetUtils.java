package com.tsvietok.meteoradar.dev.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.tsvietok.meteoradar.dev.utils.CustomLog.logDebug;
import static com.tsvietok.meteoradar.dev.utils.CustomLog.logError;

public class NetUtils {
    private static final String JSON_URL = "http://radar.veg.by/kiev/update.json";

    public static String getJsonFromServer() {
        logDebug("getJsonFromServer()");

        URL jsonUrl = null;
        try {
            jsonUrl = new URL(JSON_URL);
        } catch (MalformedURLException e) {
            logError("Wrong URL:" + e.getMessage());
        }
        URLConnection connection = null;
        try {
            if (jsonUrl != null) {
                connection = jsonUrl.openConnection();
            }
        } catch (IOException e) {
            logError("Can't open connection: " + e.getMessage());
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
            logError("Can't get input stream: " + e.getMessage());
        }

        String input = null;
        try {
            if (inputStream != null) {
                input = inputStream.readLine();
            }
        } catch (IOException e) {
            logError("Can't read from stream: " + e.getMessage());
        }
        return input;
    }

    public static Bitmap getBitmapFromServer(String url) {
        logDebug("getBitmapFromServer()");

        URL bitmapUrl = null;
        try {
            bitmapUrl = new URL(url);
        } catch (MalformedURLException e) {
            logError("Wrong URL:" + e.getMessage());
        }
        URLConnection connection = null;
        try {
            if (bitmapUrl != null) {
                connection = bitmapUrl.openConnection();
            }
        } catch (IOException e) {
            logError("Can't open connection: " + e.getMessage());
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
            logError("Can't get or decode input stream: " + e.getMessage());
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
