package com.memes.khom.mnews.utils;


import android.graphics.Bitmap;
import android.graphics.Matrix;


public class ImageUtils {


    public static Bitmap getResizeFile(Bitmap bitmap, float mxSize) {
        Bitmap originalPhoto = null;
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

    public static Bitmap getResizedBitmap(Bitmap bm, float newHeight, float newWidth) {
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
