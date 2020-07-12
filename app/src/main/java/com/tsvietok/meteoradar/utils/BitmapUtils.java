package com.tsvietok.meteoradar.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.Matrix3f;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicColorMatrix;

import com.tsvietok.meteoradar.ScriptC_invert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class BitmapUtils {
    public static Bitmap CreateBitmap(int width, int height, int color) {
        int sourceSize = width * height;
        int[] sourcePixels = new int[sourceSize];

        Arrays.fill(sourcePixels, 0, sourceSize, color);

        return Bitmap.createBitmap(sourcePixels, width, height, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap ReplaceColor(Bitmap source, Map<Integer, Integer> colorsHashMap) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int sourceSize = sourceWidth * sourceHeight;
        int[] sourcePixels = new int[sourceSize];
        source.getPixels(sourcePixels, 0, sourceWidth, 0, 0, sourceWidth, sourceHeight);

        for (int i = 0; i < sourceSize; i++) {
            if (colorsHashMap.containsKey(sourcePixels[i])) {
                sourcePixels[i] = colorsHashMap.get(sourcePixels[i]);
            }
        }

        return Bitmap.createBitmap(sourcePixels, sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap OverlayBitmap(Bitmap lowerLayer, Bitmap upperLayer) {
        Bitmap bmOverlay = Bitmap.createBitmap(upperLayer.getWidth(),
                upperLayer.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(lowerLayer, new Matrix(), null);
        canvas.drawBitmap(upperLayer, new Matrix(), null);
        return bmOverlay;
    }

    public static Bitmap InvertBitmap(Bitmap source, Context context) {
        Bitmap outputBitmap = source.copy(source.getConfig(), true);

        RenderScript renderScript = RenderScript.create(context);

        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, outputBitmap);
        Allocation invertedAllocation =
                Allocation.createTyped(renderScript, inputAllocation.getType());

        ScriptC_invert invertScript = new ScriptC_invert(renderScript);

        invertScript.forEach_invert(inputAllocation, invertedAllocation);
        invertedAllocation.copyTo(outputBitmap);

        inputAllocation.destroy();
        invertedAllocation.destroy();
        invertScript.destroy();
        renderScript.destroy();


        int value = 100;
        final float max = (float) Math.PI;
        final float min = (float) -Math.PI;
        float f = (float) ((max - min) * (value / 100.0) + min);
        return rotateHue(outputBitmap, context, f);
    }

    public static Bitmap rotateHue(Bitmap source, Context context, float value) {
        Bitmap outputBitmap = source.copy(source.getConfig(), true);

        RenderScript renderScript = RenderScript.create(context);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, outputBitmap);

        Allocation hueAllocation = Allocation.createTyped(renderScript, inputAllocation.getType());
        ScriptIntrinsicColorMatrix scriptIntrinsicColorMatrix =
                ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));

        float cos = (float) Math.cos(value);
        float sin = (float) Math.sin(value);
        Matrix3f mat = new Matrix3f();
        mat.set(0, 0, (float) (.299 + .701 * cos + .168 * sin));
        mat.set(1, 0, (float) (.587 - .587 * cos + .330 * sin));
        mat.set(2, 0, (float) (.114 - .114 * cos - .497 * sin));
        mat.set(0, 1, (float) (.299 - .299 * cos - .328 * sin));
        mat.set(1, 1, (float) (.587 + .413 * cos + .035 * sin));
        mat.set(2, 1, (float) (.114 - .114 * cos + .292 * sin));
        mat.set(0, 2, (float) (.299 - .3 * cos + 1.25 * sin));
        mat.set(1, 2, (float) (.587 - .588 * cos - 1.05 * sin));
        mat.set(2, 2, (float) (.114 + .886 * cos - .203 * sin));

        scriptIntrinsicColorMatrix.setColorMatrix(mat);
        scriptIntrinsicColorMatrix.forEach(inputAllocation, hueAllocation);
        hueAllocation.copyTo(outputBitmap);

        inputAllocation.destroy();
        hueAllocation.destroy();
        scriptIntrinsicColorMatrix.destroy();
        renderScript.destroy();

        return outputBitmap;
    }

    static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            CustomLog.logError("Can't load bitmap from asset: " + e.getMessage());
        }

        return bitmap;
    }
}
