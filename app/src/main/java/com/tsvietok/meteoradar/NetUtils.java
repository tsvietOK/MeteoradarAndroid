package com.tsvietok.meteoradar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static com.tsvietok.meteoradar.CustomLog.*;

class NetUtils {
    private static final String JSON_URL = "http://radar.veg.by/kiev/update.json";

    static String getJsonFromServer() throws IOException {
        logDebug("getJsonFromServer()");

        URL jsonUrl = new URL(JSON_URL);
        URLConnection connection = jsonUrl.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));

        return inputStream.readLine();
    }

    static Bitmap getBitmapFromServer(String url) throws IOException {
        logDebug("getBitmapFromServer()");

        URL bitmapUrl = new URL(url);
        URLConnection connection = bitmapUrl.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        return BitmapFactory.decodeStream(connection.getInputStream());
    }
}
