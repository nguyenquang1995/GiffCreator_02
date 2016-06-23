package com.example.hacks_000.giffcreator_02.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by framgia on 22/06/2016.
 */
public class InternetUtil {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
            .CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static byte[] convertBitmapTobyteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, ImageUtil.IMAGE_QUALITY, stream);
        return stream.toByteArray();
    }

    public static Bitmap downloadImage(String url) throws IOException {
        InputStream input = new java.net.URL(url).openStream();
        Bitmap resultBitmap = BitmapFactory.decodeStream(input);
        input.close();
        return resultBitmap;
    }
}
