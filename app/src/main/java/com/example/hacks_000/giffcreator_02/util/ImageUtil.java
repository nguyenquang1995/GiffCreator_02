package com.example.hacks_000.giffcreator_02.util;

import android.graphics.BitmapFactory;

/**
 * Created by hacks_000 on 6/6/2016.
 */
public class ImageUtil {
    public static final int LOW_IMAGE = 150;
    public static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            inSampleSize = 2;
            while ((height / inSampleSize) > reqHeight
                && (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static int calculateInSampleSizeFitWidth(BitmapFactory.Options options, int reqWidth) {
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            inSampleSize = 2;
            while (width / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
