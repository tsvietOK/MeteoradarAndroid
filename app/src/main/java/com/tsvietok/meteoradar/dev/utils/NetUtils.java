package com.tsvietok.meteoradar.dev.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static com.tsvietok.meteoradar.dev.utils.CustomLog.*;

public class NetUtils {
    private static final String JSON_URL = "http://radar.veg.by/kiev/update.json";

    public static String getJsonFromServer() throws IOException {
        logDebug("getJsonFromServer()");

        URL jsonUrl = new URL(JSON_URL);
        URLConnection connection = jsonUrl.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));

        return inputStream.readLine();
    }

    public static Bitmap getBitmapFromServer(String url) throws IOException {
        logDebug("getBitmapFromServer()");

        URL bitmapUrl = new URL(url);
        URLConnection connection = bitmapUrl.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        return BitmapFactory.decodeStream(connection.getInputStream());
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return true;
        }
        return false;
    }
}
