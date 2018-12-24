package com.example.pc_asus.nguoimu.FaceRecognition.liveVideo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;

public class MyFaceDetector extends Detector<Face> {
    public static Bitmap bitmap;
    public static int rotate;

    private Detector<Face> mDelegate;

    MyFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
    }

    public SparseArray<Face> detect(Frame frame) {
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

//        bitmapRotate=frame.getBitmap().extractAlpha();

        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
         byte [] jpegArray = byteArrayOutputStream.toByteArray();
         bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);


        rotate=frame.getMetadata().getRotation(); // 0-2  3|1
    //    Log.e("abc", "zooooo "+frame.getMetadata().getRotation()+" rộng:"+bitmap.getWidth()+" cao:"+bitmap.getHeight());

        return mDelegate.detect(frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}