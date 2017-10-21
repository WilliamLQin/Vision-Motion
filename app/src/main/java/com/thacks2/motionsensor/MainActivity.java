package com.thacks2.motionsensor;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
            System.out.println("OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
            System.out.println("OpenCV loaded");
        }
    }

    private TextView mTextView;
    private double mCenterX, mCenterY;

    private int w, h;
    private CameraBridgeViewBase mOpenCvCameraView;

    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.99;

    private List<MatOfPoint> mContours = new ArrayList<>();
    private List<MatOfPoint> mPreContours = new ArrayList<>();

    // Cache
    private Mat mRgbaMat = new Mat();
    private Mat mHsvMat = new Mat();

    private List<Mat> mHsvChannels = new ArrayList<>();
    private Mat mHMat = new Mat(), mSMat = new Mat(), mVMat = new Mat();

    private Mat mMask = new Mat();
    private Mat mDilatedMask = new Mat();
    private Mat mHierarchy = new Mat();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void initializeOpenCVDependencies() throws IOException {
        mOpenCvCameraView.enableView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javasurfaceview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mTextView = (TextView) findViewById(R.id.textview);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        w = width;
        h = height;
    }
    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        System.gc();
        System.runFinalization();

        return pipeline(inputFrame.rgba());
    }

    public Mat pipeline(Mat aInputFrame) {

        // BEGIN OpenCV Pipeline
//  --------------------------------------------------------------

        // Convert to RGBA
        Imgproc.cvtColor(aInputFrame, mRgbaMat, Imgproc.COLOR_BGR2RGBA);

        // Convert to HSV
        Imgproc.cvtColor(aInputFrame, mHsvMat, Imgproc.COLOR_BGR2HSV);

        // Split HSV to individual channels
        mHsvChannels.clear();
        Core.split(mHsvMat, mHsvChannels);

        // Get gray scale H channel
        Imgproc.cvtColor(mHsvChannels.get(0), mHMat, Imgproc.COLOR_GRAY2RGBA);
        // Get gray scale S channel
        Imgproc.cvtColor(mHsvChannels.get(0), mSMat, Imgproc.COLOR_GRAY2RGBA);
        // Get gray scale V channel
        Imgproc.cvtColor(mHsvChannels.get(0), mVMat, Imgproc.COLOR_GRAY2RGBA);

        // Find contours
        Scalar lowerBound = new Scalar(60 - 20, 60, 60);
        Scalar upperBound = new Scalar(60 + 20, 245, 245);

        Core.inRange(mHsvMat, lowerBound, upperBound, mMask);
        Imgproc.erode(mMask, mDilatedMask, new Mat(), new Point(-1.0, -1.0), 1);

        mPreContours.clear();
        Imgproc.findContours(mDilatedMask, mPreContours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        System.out.println("Contours Count: " + mPreContours.size());

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = mPreContours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = mPreContours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
                Core.multiply(contour, new Scalar(1, 1), contour);
                mContours.add(contour);
            }
        }
        System.out.println("Filtered Contours Count: " + mContours.size());

        // Draw Contours
        Imgproc.drawContours(mRgbaMat, mContours, -1, new Scalar(255, 255, 255), -1);

        // Do actions with contours
        for (MatOfPoint contour : mContours) {

            // Draw Center
            Moments moments = Imgproc.moments(contour);

            int cX = (int)(moments.get_m10() / moments.get_m00());
            int cY = (int)(moments.get_m01() / moments.get_m00());

            Imgproc.circle(mRgbaMat, new Point(cX, cY), 7, new Scalar(160, 255, 255), -1);
            Imgproc.putText(mRgbaMat, "center", new Point(cX - 20, cY - 20), 0, 0.5, new Scalar (160, 255, 255), 2);


            // Draw Minimum Enclosing Circle
            Point center = new Point(0, 0);
            float[] radius = new float[1];

            MatOfPoint2f floatContour = new MatOfPoint2f(contour.toArray());

            Imgproc.minEnclosingCircle(floatContour, center, radius);

            Imgproc.circle(mRgbaMat, center, (int)radius[0], new Scalar(100, 100, 255), 7);


            // Set Output Center Value
//            mCenterX = (double) cX;
//            mCenterY = (double) cY;
            mCenterX = center.x;
            mCenterY = center.y;



        }




        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mTextView.setText("Center: (" + mCenterX + ", " + mCenterY + ")");

            }
        });


//  --------------------------------------------------------------
        // END OpenCV Pipeline

        return mRgbaMat;
    }

//    private void releaseCache() {
//        if (mBgrMat != null)
//            mBgrMat.release();
//
//        if (mHsvMat != null)
//            mHsvMat.release();
//
//        for (Mat m : mHsvChannels) {
//            if (m != null)
//                m.release();
//        }
//
//        if (mHMat != null)
//            mHMat.release();
//        if (mSMat != null)
//            mSMat.release();
//        if (mVMat != null)
//            mVMat.release();
//    }

}
