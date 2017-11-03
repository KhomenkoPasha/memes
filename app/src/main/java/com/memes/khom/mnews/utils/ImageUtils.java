package com.memes.khom.mnews.utils;


import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;


public class ImageUtils {


    public static Bitmap getResizeFile(Bitmap bitmap, float mxSize) {
        Bitmap originalPhoto = bitmap;
        final float height = bitmap.getHeight();
        final float width = bitmap.getWidth();

        if (height >= mxSize || width >= mxSize) {
            float newWidth;
            float newHeight;
            if (height > width) {
                newHeight = mxSize;
                newWidth = width / (height / mxSize);
            } else {
                newWidth = mxSize;
                newHeight = height / (width / mxSize);
            }
            originalPhoto = getResizedBitmap(bitmap, newHeight, newWidth);

        }
        return originalPhoto;
    }

    //Получаем абсолютный путь файла из Uri
    public static String getRealPathFromURI(Uri uri, Activity activity) {
        String[] projection = {MediaStore.Images.Media.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        int columnIndex = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    private static Bitmap getResizedBitmap(Bitmap bm, float newHeight, float newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
    }
}
