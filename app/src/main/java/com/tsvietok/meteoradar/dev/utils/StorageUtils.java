package com.tsvietok.meteoradar.dev.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tsvietok.meteoradar.dev.Location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class StorageUtils {
    private static final String PNG_EXTENSION = ".png";
    private static final String JSON_CONFIG_FILE_NAME = "config.json";

    public static Bitmap getBitmapFromStorage(Context context,
                                              String fileName,
                                              Location location) {
        File workingDirectory = new File(context.getFilesDir() + location.getCity());
        if (!workingDirectory.exists()) {
            workingDirectory.mkdir();
        }
        File file = new File(workingDirectory, fileName + PNG_EXTENSION);
        if (file.exists()) {
            FileInputStream stream;
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                CustomLog.logError("File not found: " + e.getMessage());
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            try {
                stream.close();
            } catch (IOException e) {
                CustomLog.logError("Can't close stream: " + e.getMessage());
            }
            return bitmap;
        }
        return null;
    }

    public static void saveBitmapToStorage(Context context,
                                           Bitmap bitmap,
                                           int fileName,
                                           Location location) {
        File workingDirectory = new File(context.getFilesDir() + location.getCity());
        if (!workingDirectory.exists()) {
            workingDirectory.mkdir();
        }
        File file = new File(workingDirectory, fileName + PNG_EXTENSION);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            CustomLog.logError("File not found: " + e.getMessage());
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        try {
            if (stream != null) {
                CustomLog.logDebug("saveBitmapToStorage(): " + file.getAbsolutePath() + " saved.");
                stream.close();
            }
        } catch (IOException e) {
            CustomLog.logError("Can't close stream: " + e.getMessage());
        }
    }

    public static String getJsonFromStorage(Context context, Location location) {
        File workingDirectory = new File(context.getFilesDir() + location.getCity());
        if (!workingDirectory.exists()) {
            workingDirectory.mkdir();
        }
        File file = new File(workingDirectory, JSON_CONFIG_FILE_NAME);
        if (file.exists()) {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) text.append(line);
                br.close();
            } catch (IOException e) {
                CustomLog.logError("Can't get JSON config from storage: " + e.getMessage());
            }
            return text.toString();
        }
        return null;
    }

    public static void saveJsonToStorage(Context context, String string, Location location) {
        File workingDirectory = new File(context.getFilesDir() + location.getCity());
        if (!workingDirectory.exists()) {
            workingDirectory.mkdir();
        }
        File file = new File(workingDirectory, JSON_CONFIG_FILE_NAME);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            CustomLog.logError("File not found: " + e.getMessage());
        }
        if (stream != null) {
            try {
                stream.write(string.getBytes());
            } catch (IOException e) {
                CustomLog.logError("Can't write to file: " + e.getMessage());
            }

            try {
                stream.close();
            } catch (IOException e) {
                CustomLog.logError("Can't close stream: " + e.getMessage());
            }
        }
    }

    public static void removeUnusedBitmap(Context context, int[] times, Location location) {
        String path = context.getFilesDir().toString();

        File[] files = new File(path + location.getCity())
                .listFiles();
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
                File file = new File(path + location.getCity() + fileName);
                if (file.exists()) {
                    if (file.delete()) {
                        CustomLog.logDebug("removeUnusedBitmap(): Deleted file: "
                                + file.getPath());
                    } else {
                        CustomLog.logDebug("removeUnusedBitmap(): File not deleted: "
                                + file.getPath());
                    }
                }
            }
        }
    }

    public static void clearStorage(Context context) {
        File dir = new File(context.getFilesDir().toString());
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    new File(dir, child).delete();
                }
                CustomLog.logDebug("clearStorage(): Storage cleared successfully!");
            }
        }
    }
}
