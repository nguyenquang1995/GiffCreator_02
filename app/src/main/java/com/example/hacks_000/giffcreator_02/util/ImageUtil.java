package com.example.hacks_000.giffcreator_02.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by hacks_000 on 6/6/2016.
 */
public class ImageUtil {
    public static final int LOW_IMAGE = 700;
    private static final String DOWNLOAD_DOCUMENT = "com.android.providers.downloads.documents";
    private static final String MEDIA_DOCUMENT = "com.android.providers.media.documents";
    private static final String GOOGLE_PHOTO = "com.google.android.apps.photos.content";
    private static final String PRIMARY_IMAGE = "primary";
    private static final String DOWNLOAD_CONTENT = "content://downloads/public_downloads";
    private static final String ID_SELECTION = "_id=?";
    private static final String IMAGE_TYPE = "image";
    private static final String AUDIO_TYPE = "audio";
    private static final String VIDEO_TYPE = "video";
    private static final String FILE_TYPE = "file";
    private static final String CONTENT_TYPE = "content";
    private static final String SPLIT_ID = ":";
    private static final String EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    public static final int IMAGE_QUALITY = 85;
    private static final String IMAGE_EXTENSION = ".jpg";

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

    public static Bitmap decodeBitmapFromPathToFitScreen(Context context, String pathName) {
        WindowManager windowManager =
            (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int reqWidth = windowManager.getDefaultDisplay().getWidth();
        int reqHeight = windowManager.getDefaultDisplay().getHeight();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSizeFitWidth(options, reqWidth);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
        float scaleW = (float) reqWidth / bitmap.getWidth();
        float scaleH = (float) reqHeight / bitmap.getHeight();
        float scale = (scaleW > scaleH) ? scaleW : scaleH;
        float i = bitmap.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
            matrix, false);
        bitmap.recycle();
        return result;
    }

    public static Bitmap decodeBitmapFromPath(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static Bitmap getImageFromUri(Context context, Uri uri)
        throws FileNotFoundException, IOException {
        WindowManager windowManager =
            (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int size = windowManager.getDefaultDisplay().getWidth();
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;
        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ?
            onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;
        double ratio = (originalSize > size) ? (originalSize / size) : 1.0;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    public static String getPath(Context context, Intent data) {
        Uri uri = data.getData();
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri) && isExternalStorageDocument
            (uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(SPLIT_ID);
            final String type = split[0];
            if (PRIMARY_IMAGE.equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else return null;
        }
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri) && isDownloadsDocument(uri)) {
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris
                .withAppendedId(Uri.parse(DOWNLOAD_CONTENT),
                    Long.valueOf(id));
            return getDataColumn(context, contentUri, null, null);
        }
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri) && isMediaDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(SPLIT_ID);
            final String type = split[0];
            Uri contentUri = null;
            if (IMAGE_TYPE.equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (VIDEO_TYPE.equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (AUDIO_TYPE.equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            final String[] selectionArgs = new String[]{split[1]};
            return getDataColumn(context, contentUri, ID_SELECTION, selectionArgs);
        }
        if (CONTENT_TYPE.equalsIgnoreCase(uri.getScheme()) && isGooglePhotosUri(uri)) {
            return uri.getLastPathSegment();
        }
        if (CONTENT_TYPE.equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        if (FILE_TYPE.equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = {MediaStore.Images.Media.DATA};
        try {
            cursor =
                context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return EXTERNAL_STORAGE.equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return DOWNLOAD_DOCUMENT.equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return MEDIA_DOCUMENT.equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return GOOGLE_PHOTO.equals(uri.getAuthority());
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, bytes);
        String path = MediaStore.Images.Media
            .insertImage(inContext.getContentResolver(), inImage, "title", null);
        return Uri.parse(path);
    }

    public static File saveImage(Context context, Bitmap imageBitmap, String imageName) {
        File file = null;
        try {
            OutputStream outputStream = null;
            file = new File(Environment.getExternalStorageDirectory() + "/" + imageName +
                IMAGE_EXTENSION);
            outputStream = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream);
            outputStream.flush();
            outputStream.close();
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file
                .getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static ArrayList<String> getFilePaths(Activity activity) {
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor c = null;
        SortedSet<String> dirList = new TreeSet<String>();
        ArrayList<String> resultIAV = new ArrayList<String>();
        String[] directories = null;
        if (u != null) {
            c = activity.managedQuery(u, projection, null, null, null);
        }
        if ((c != null) && (c.moveToFirst())) {
            do {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try {
                    dirList.add(tempDir);
                } catch (Exception e) {
                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);
        }
        for (int i = 0; i < dirList.size(); i++) {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if (imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {
                    if (imagePath.isDirectory()) {
                        imageList = imagePath.listFiles();
                    }
                    if (imagePath.getName().contains(".gif") ||
                        imagePath.getName().contains(".GIF")) {
                        if (imagePath.length() == 0) {
                            DeleteFileAsynTask deleteFileAsynTask = new DeleteFileAsynTask();
                            deleteFileAsynTask.execute(imagePath.getAbsolutePath());
                        } else {
                            String path = imagePath.getAbsolutePath();
                            resultIAV.add(path);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resultIAV;
    }
}
