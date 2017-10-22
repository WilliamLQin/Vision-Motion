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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

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

    private Button mButton;

    // Color settings
    private double mTargetH = 60, mTargetS = 155, mTargetV = 155;
    private double mRangeH = 20, mRangeS = 100, mRangeV = 100;

    private int mCurrentState = 0;

    private double mObjLength;
    private float mStartDiameter;
    private int mSeekBarProgress;

    // Time recording
    private long mStartTime, mLastTime, mDeltaTime, mElapsedTime;
    // Position recording
    private double mCenterX, mCenterY;
    // Size recording
    private float mDiameter;

    // List to hold recorded data
    public class DataEntry
    {
        private double time;
        private double x;
        private double y;
        private float diameter;
        private double realToPixelsRatio;

        public DataEntry(long time, double x, double y, float diameter, double realToPixelsRatio)
        {
            this.time = (double)time;
            this.x = x;
            this.y = y;
            this.diameter = diameter;
            this.realToPixelsRatio = realToPixelsRatio;
        }

        public double getSecondTime()
        {
            return time/1000;
        }
        public double getMillisecondTime()
        {
            return time;
        }
        public double getRawX()
        {
            return x;
        }
        public double getX()
        {
            return getDistanceInUnits(x);
        }
        public double getRawY()
        {
            return y;
        }
        public double getY()
        {
            return getDistanceInUnits(y);
        }
        public float getDiameter()
        {
            return diameter;
        }

        private double getDistanceInUnits(double length) {
            return realToPixelsRatio * (length);
        }

        @Override
        public String toString() {
            return "DataEntry: T=" + getSecondTime() + " X=" + getX() + " Y=" + getY() + " Diameter=" + getDiameter();
        }
    }
    private List<DataEntry> mRecordedData = new ArrayList<>();

    // Viewport
    private int w, h;

    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.99;

    // Cache
    private List<MatOfPoint> mContours = new ArrayList<>();
    private List<MatOfPoint> mPreContours = new ArrayList<>();

    private Mat mRgbaMat = new Mat();
    private Mat mHsvMat = new Mat();
    private Mat mBgrMat = new Mat();

    private Mat mMask = new Mat();
    private Mat mDilatedMask = new Mat();
    private Mat mHierarchy = new Mat();

    // OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

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

        try{
            Bundle bundle = getIntent().getExtras();
            mObjLength = Double.parseDouble(bundle.getString("length"));

        } catch(Exception e) {
            System.out.println(e);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javasurfaceview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        SeekBar slider = (SeekBar) findViewById(R.id.slider);
        slider.setOnSeekBarChangeListener(customSeekBarListener);

        mButton = (Button) findViewById(R.id.record);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentState == 1)
                    stopRecording();
                else if (mCurrentState == 0)
                    startRecording();
            }
        });

    }

    private void startRecording() {
        mCurrentState = 1;
        mButton.setText("Stop");
        mStartTime = System.currentTimeMillis();
        mTargetH = mSeekBarProgress;
        mStartDiameter = mDiameter;
    }

    private void stopRecording() {
        mCurrentState = 2;
        mButton.setText("");
        for (DataEntry data : mRecordedData) {
            System.out.println(data);
        }
    }

    private SeekBar.OnSeekBarChangeListener customSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            System.out.println("Seek bar at: " + progress);
            mSeekBarProgress = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

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

        setColorSpaceMats(inputFrame.rgba());

        if (mCurrentState == 0)
            mTargetH = mSeekBarProgress;

        if (mCurrentState == 1) {
            mDeltaTime = System.currentTimeMillis() - mLastTime;
            mElapsedTime = System.currentTimeMillis() - mStartTime;
            mLastTime = System.currentTimeMillis();

        }

        Mat reMat = pipeline();

        return reMat;
    }

    private Scalar getLowerBound() {
        return new Scalar(mTargetH - mRangeH, mTargetS - mRangeS, mTargetV - mRangeV);
    }
    private Scalar getUpperBound() {
        return new Scalar(mTargetH + mRangeH, mTargetS + mRangeS, mTargetV + mRangeV);
    }

    private void setColorSpaceMats(Mat inMat) { // Also sets mBgrMat

        // Convert to BGR
        Imgproc.cvtColor(inMat, mBgrMat, Imgproc.COLOR_RGBA2BGR);

        // Get RGBA
        //mRgbaMat = mBgrMat; // For some reason on a Huawei P10 the output needs to be in BGR
        mRgbaMat = inMat;

        // Convert to HSV
        Imgproc.cvtColor(mBgrMat, mHsvMat, Imgproc.COLOR_BGR2HSV);

    }

    public Mat pipeline() {

        // BEGIN OpenCV Pipeline
//  --------------------------------------------------------------

        // Find contours
        Core.inRange(mHsvMat, getLowerBound(), getUpperBound(), mMask);
        Imgproc.erode(mMask, mDilatedMask, new Mat(), new Point(-1.0, -1.0), 1);

        mPreContours.clear();
        Imgproc.findContours(mDilatedMask, mPreContours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

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

        // Draw Contours
        Imgproc.drawContours(mRgbaMat, mContours, -1, new Scalar(255, 255, 255), -1);

        // Do actions with contours
        for (MatOfPoint contour : mContours) {

            // Get Minimum Enclosing Circle
            Point center = new Point(0, 0);
            float[] radius = new float[1];

            MatOfPoint2f floatContour = new MatOfPoint2f(contour.toArray());

            Imgproc.minEnclosingCircle(floatContour, center, radius);


            // Get Center
            Moments moments = Imgproc.moments(contour);

            int cX = (int) (moments.get_m10() / moments.get_m00());
            int cY = (int) (moments.get_m01() / moments.get_m00());

            // Set Output Center Value and Diameter
//            mCenterX = (double) cX;
//            mCenterY = (double) cY;
            mCenterX = center.x;
            mCenterY = center.y;
            mDiameter = 2 * radius[0];

            if (mCurrentState == 1)
                mRecordedData.add(new DataEntry(mElapsedTime, mCenterX, mCenterY, mDiameter, mObjLength/mStartDiameter));

            // Draw Minimum Enclosing Circle
            Imgproc.circle(mRgbaMat, center, (int) radius[0], new Scalar(100, 100, 255), 7);


            // Draw Center
            Imgproc.circle(mRgbaMat, new Point(mCenterX, mCenterY), 7, new Scalar(160, 255, 255), -1);
            Imgproc.putText(mRgbaMat, "center", new Point(mCenterX - 20, mCenterY - 20), 0, 0.5, new Scalar(160, 255, 255), 2);

        }

//  --------------------------------------------------------------
        // END OpenCV Pipeline

        return mRgbaMat;
    }



}
