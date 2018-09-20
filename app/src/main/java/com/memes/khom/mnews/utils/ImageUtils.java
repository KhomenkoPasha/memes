package com.memes.khom.mnews.utils;


import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class ImageUtils {


    public static void compressAndRotatePhoto(String photoURL) {
        int orientation = 0;
        File imgFile = new File(photoURL);
        if (imgFile.exists()) {
            try {
                ExifInterface ei = new ExifInterface(imgFile.getAbsolutePath());
                orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
            } catch (IOException e) {
                e.printStackTrace();

            }

            final float MAX_SIZE = 1024;
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
            final float height = options.outHeight;
            final float width = options.outWidth;
            Bitmap originalPhoto = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            if (height >= MAX_SIZE || width >= MAX_SIZE) {
                float newWidth;
                float newHeight;
                if (height > width) {
                    newHeight = MAX_SIZE;
                    newWidth = width / (height / MAX_SIZE);
                } else {
                    newWidth = MAX_SIZE;
                    newHeight = height / (width / MAX_SIZE);
                }
                originalPhoto = getResizedBitmap(originalPhoto, newHeight, newWidth);
            }

            try {
                FileOutputStream out = new FileOutputStream(imgFile);

                if (orientation > 1) {
                    originalPhoto = rotateBitmap(originalPhoto, orientation);
                } else originalPhoto = convertToMutable(originalPhoto);

                originalPhoto.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public static void compressAndRotatePhotoTemp(String photoURL, String tempFilePa) {
        int orientation = 0;
        File imgFile = new File(photoURL);
        if (imgFile.exists()) {
            try {
                ExifInterface ei = new ExifInterface(imgFile.getAbsolutePath());
                orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
            } catch (IOException e) {
                e.printStackTrace();

            }

            final float MAX_SIZE = 1024;
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
            final float height = options.outHeight;
            final float width = options.outWidth;
            Bitmap originalPhoto = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            if (height >= MAX_SIZE || width >= MAX_SIZE) {
                float newWidth;
                float newHeight;
                if (height > width) {
                    newHeight = MAX_SIZE;
                    newWidth = width / (height / MAX_SIZE);
                } else {
                    newWidth = MAX_SIZE;
                    newHeight = height / (width / MAX_SIZE);
                }
                originalPhoto = getResizedBitmap(originalPhoto, newHeight, newWidth);
            }

            try {
                FileOutputStream out = new FileOutputStream(tempFilePa);

                if (orientation > 1) {
                    originalPhoto = rotateBitmap(originalPhoto, orientation);
                } else originalPhoto = convertToMutable(originalPhoto);

                originalPhoto.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }



    private static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes() * height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }


    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        try {
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }
            try {
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getResizeFile(Bitmap bitmap, float mxSize) {
        Bitmap originalPhoto;
        final float height = bitmap.getHeight();
        final float width = bitmap.getWidth();

        if (width >= mxSize) {
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

        } else {
            originalPhoto = getResizedBitmap(bitmap, height / (width / mxSize), mxSize);
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
            if (newHeight > 0 && newWidth > 0) {
                int width = bm.getWidth();
                int height = bm.getHeight();
                float scaleWidth = newWidth / width;
                float scaleHeight = newHeight / height;
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                return Bitmap.createBitmap(bm, 0, 0, width, height,
                        matrix, false);
            } else return bm;
    }
}
