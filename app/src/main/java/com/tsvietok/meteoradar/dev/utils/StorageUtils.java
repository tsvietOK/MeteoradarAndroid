package com.tsvietok.meteoradar.dev.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.tsvietok.meteoradar.dev.utils.CustomLog.logDebug;
import static com.tsvietok.meteoradar.dev.utils.CustomLog.logError;

public class StorageUtils {
    private static final String PNG_EXTENSION = ".png";
    private static final String JSON_EXTENSION = ".json";

    public static Bitmap getBitmapFromStorage(Context context, String fileName) {
        File file = new File(context.getFilesDir() + "/" + fileName + PNG_EXTENSION);
        if (file.exists()) {
            FileInputStream stream;
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                logError("File not found: " + e.getMessage());
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            try {
                stream.close();
            } catch (IOException e) {
                logError("Can't close stream: " + e.getMessage());
            }
            return bitmap;
        }
        return null;
    }

    public static void saveBitmapToStorage(Context context, Bitmap bitmap, int fileName) {
        File file = new File(context.getFilesDir(), fileName + PNG_EXTENSION);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            logError("File not found: " + e.getMessage());
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        try {
            if (stream != null) {
                logDebug("saveBitmapToStorage(): " + file.getAbsolutePath() + " saved.");
                stream.close();
            }
        } catch (IOException e) {
            logError("Can't close stream: " + e.getMessage());
        }
    }

    public static String getJsonFromStorage(Context context, String fileName) {
        File file = new File(context.getFilesDir() + "/" + fileName + JSON_EXTENSION);
        if (file.exists()) {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) text.append(line);
                br.close();
            } catch (IOException e) {
                logError("Can't get JSON config from storage: " + e.getMessage());
            }
            return text.toString();
        }
        return null;
    }

    public static void saveJsonToStorage(Context context, String string, String fileName) {
        File file = new File(context.getFilesDir(), fileName + JSON_EXTENSION);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            logError("File not found: " + e.getMessage());
        }
        if (stream != null) {
            try {
                stream.write(string.getBytes());
            } catch (IOException e) {
                logError("Can't write to file: " + e.getMessage());
            }

            try {
                stream.close();
            } catch (IOException e) {
                logError("Can't close stream: " + e.getMessage());
            }
        }
    }

    public static void removeUnusedBitmap(Context context, int[] times) {
        File[] files = new File(context.getFilesDir().toString()).listFiles();
        if (files != null) {
            ArrayList<String> filesToRemove = new ArrayList<>();

            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(PNG_EXTENSION)) filesToRemove.add(fileName);
            }

            for (int time : times) {
                filesToRemove.remove(time + PNG_EXTENSION);
            }

            for (String fileName : filesToRemove) {
                File file = new File(context.getFilesDir().toString() + "/" + fileName);
                if (file.exists()) {
                    if (file.delete()) {
                        logDebug("removeUnusedBitmap(): Deleted file: " + file.getPath());
                    } else {
                        logDebug("removeUnusedBitmap(): File not deleted: " + file.getPath());
                    }
                }
            }
        }
    }
}
