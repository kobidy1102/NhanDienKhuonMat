package com.example.pc_asus.nguoimu;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * --> Created by phong.nguyen@beesightsoft.com on 3/12/18.
 */

public class AppUtil {
    private static final int BUFFER_SIZE = 1024 * 4;

    public static final long SECOND_MILLIS = 1000;
    public static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    public static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    public static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    public static final long WEEK_MILLIS = 7 * DAY_MILLIS;
    public static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    private static final long YEAR_MILLIS = 365 * DAY_MILLIS;


    public static Bitmap rotateImage(String imagePath, int maxWidth, int maxHeight) {
        if (!TextUtils.isEmpty(imagePath)) {
            Matrix matrix = new Matrix();
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, bitmapOptions);
            getResizedBitmapOptions(bitmapOptions, maxWidth, maxHeight);
            bitmapOptions.inJustDecodeBounds = false;
            Bitmap srcBitmap = BitmapFactory.decodeFile(imagePath, bitmapOptions);
            try {
                ExifInterface exifInterface = new ExifInterface(imagePath);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return srcBitmap == null ? null
                    : Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }
        return null;
    }

    private static void getResizedBitmapOptions(BitmapFactory.Options bitmapOptions, int maxWidth, int maxHeight) {
        int inSampleSize = 0;
        int width = bitmapOptions.outWidth;
        int height = bitmapOptions.outHeight;

        float ratio = (float) width / (float) height;
        if (maxWidth != 0 && maxHeight != 0) {
            if (ratio > 1) {
                if (width > maxWidth) { //prevent zoom image
                    inSampleSize = width / maxWidth;
                    width = maxWidth;
                    height = (int) (width / ratio);
                }
            } else {
                if (height > maxHeight) { //prevent zoom image
                    inSampleSize = height / maxHeight;
                    height = maxHeight;
                    width = (int) (height * ratio);
                }
            }
        } else {
            if (maxWidth == 0) {
                if (height > maxHeight) { //prevent zoom image
                    inSampleSize = height / maxHeight;
                    height = maxHeight;
                    width = (int) (height * ratio);
                }
            } else {
                if (width > maxWidth) { //prevent zoom image
                    inSampleSize = width / maxWidth;
                    width = maxWidth;
                    height = (int) (width / ratio);
                }
            }
        }
        bitmapOptions.outWidth = width;
        bitmapOptions.outHeight = height;
        bitmapOptions.inSampleSize = inSampleSize;
    }

    /**
     * Handle loading image
     */

    public static Bitmap getResizedBitmap(Bitmap srcBitmap, int maxWidth, int maxHeight) {
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();

        float ratio = (float) width / (float) height;
        if (maxWidth != 0 && maxHeight != 0) {
            if (ratio > 1) {
                if (width > maxWidth) { //prevent zoom image
                    width = maxWidth;
                    height = (int) (width / ratio);
                }
            } else {
                if (height > maxHeight) { //prevent zoom image
                    height = maxHeight;
                    width = (int) (height * ratio);
                }
            }
        } else {
            if (maxWidth == 0) {
                if (height > maxHeight) { //prevent zoom image
                    height = maxHeight;
                    width = (int) (height * ratio);
                }
            } else {
                if (width > maxWidth) { //prevent zoom image
                    width = maxWidth;
                    height = (int) (width / ratio);
                }
            }
        }
        return Bitmap.createScaledBitmap(srcBitmap, width, height, true);
    }


    public static DatabaseReference getmDatabase() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        return mDatabase;
    }

    public static FirebaseUser getmCurrentUser() {
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        return mCurrentUser;
    }

    public static String getUid() {
        String uid = getmCurrentUser().getUid();
        return uid;
    }

    //uid chữ thường
    public static String getUidLowerCase() {
        String uid = getmCurrentUser().getUid().toLowerCase();
        return uid;
    }

}
