package com.thacks2.motionsensor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

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

    private ImageButton mButton;

    private boolean mSettingsOpen = false;
    private int mCurrentState = 0;
    private int mTouchX = 0, mTouchY = 0;
    private boolean mSetTargetToTouch = false;

//    private double mObjLength;
//    private float mStartDiameter;
    private double mRealToPixelsRatio = -1;

    // Viewport
    private int mCameraWidth, mCameraHeight;
    private int mScreenWidth, mScreenHeight;

    // Color settings
    private double mTargetH = 0, mTargetS = 0, mTargetV = 0;
    private double mRangeH = 7, mRangeS = 100, mRangeV = 90;
    private double mDefaultRangeH = 7, mDefaultRangeS = 100, mDefaultRangeV = 90;

    // Time recording
    private long mStartTime, mLastTime, mDeltaTime, mElapsedTime;
    // Position recording
    private double mCenterX, mCenterY;
    // Size recording
    private float mDiameter;

    // List to hold recorded data

    private ArrayList<DataEntry> mRecordedData = new ArrayList<>();



    // Minimum contour area in percent for contours filtering
//    private static double mMinContourArea = 0.99;

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

        // Set window settings
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // Get display resolution
        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;

        // Retrieve information
//        Bundle bundle = getIntent().getExtras();
//        mObjLength = bundle.getDouble("length", -1);

        // Setup OpenCV camera view
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javasurfaceview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mCurrentState == 0) {
                        mTouchX = (int)event.getX();
                        mTouchY = (int)event.getY();
                        mSetTargetToTouch = true;
                    }
                }
                return false;
            }
        });

        // Set listener on start/stop button
        mButton = (ImageButton) findViewById(R.id.record);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentState == 1)
                    stopRecording();
                else if (mCurrentState == 0)
                    startRecording();
            }
        });
        // Set listener on back button to go back when clicked NOT NEEDED BECAUSE ANDROID HAS BACK
//        ImageButton back = (ImageButton) findViewById(R.id.back);
//        back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                backToEnterData();
//            }
//        });

        final ConstraintLayout buttonsView = (ConstraintLayout) findViewById(R.id.buttonsview);
        final ConstraintLayout settingsView = (ConstraintLayout) findViewById(R.id.settingsLayout);

        // Set listener on settings button to show/hide settings view when clicked

        View.OnClickListener settingsListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settingsView.getVisibility() == ConstraintLayout.VISIBLE) {
                    settingsView.setVisibility(ConstraintLayout.GONE);
                    buttonsView.setVisibility(ConstraintLayout.VISIBLE);
                    mSettingsOpen = false;
                }
                else {
                    settingsView.setVisibility(ConstraintLayout.VISIBLE);
                    buttonsView.setVisibility(ConstraintLayout.GONE);
                    mSettingsOpen = true;
                }
            }
        };
        ImageButton settings = (ImageButton) findViewById(R.id.settings);
        settings.setOnClickListener(settingsListener);
        ImageButton settingsCancel = (ImageButton) findViewById(R.id.settingsBack);
        settingsCancel.setOnClickListener(settingsListener);

        final EditText input = (EditText) findViewById(R.id.input);

        Button calibrate = (Button) findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double objLength = Double.parseDouble(input.getText().toString());
                    mRealToPixelsRatio = objLength/mDiameter;
                    Toast.makeText(getApplicationContext(),"Camera successfully calibrated!", Toast.LENGTH_LONG).show();
                }
                catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(),"Please input a number.", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(),"Unknown error occurred. Please make sure that there is a detected object.", Toast.LENGTH_LONG).show();
                }
            }
        });

        final SeekBar sliderH = (SeekBar) findViewById(R.id.sliderHRange);
        sliderH.setProgress((int)mRangeH);
        sliderH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRangeH = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SeekBar sliderS = (SeekBar) findViewById(R.id.sliderSRange);
        sliderS.setProgress((int)mRangeS);
        sliderS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRangeS = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SeekBar sliderV = (SeekBar) findViewById(R.id.sliderVRange);
        sliderV.setProgress((int)mRangeV);
        sliderV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRangeV = progress;
                System.out.println("HSV Range: " + mRangeH + ", " + mRangeS + ", " + mRangeV);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button restoreDefaults = (Button) findViewById(R.id.restoreDefaults);
        restoreDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRangeH = mDefaultRangeH;
                mRangeS = mDefaultRangeS;
                mRangeV = mDefaultRangeV;
                sliderH.setProgress((int)mRangeH);
                sliderS.setProgress((int)mRangeS);
                sliderV.setProgress((int)mRangeV);
            }
        });

    }

//    private void backToEnterData() {
//        Intent intent = new Intent(this, EnterData.class);
//        startActivity(intent);
//    }

    private void startRecording() {
        if (mRealToPixelsRatio == -1) {
            Toast.makeText(getApplicationContext(),"Please calibrate the camera in settings (gear icon).", Toast.LENGTH_LONG).show();
            return;
        }

        if (mRecordedData.size() != 0) {
            mRecordedData.clear();
        }

        mButton.setImageResource(R.drawable.main_square);
        mStartTime = System.currentTimeMillis();
        mDeltaTime = System.currentTimeMillis() - mLastTime;
        mElapsedTime = System.currentTimeMillis() - mStartTime;
        mLastTime = System.currentTimeMillis();

        mCurrentState = 1;
    }

    private void stopRecording() {
        mCurrentState = 0;
        mButton.setImageResource(R.drawable.main_circle);
        for (DataEntry data : mRecordedData) {
            System.out.println(data);
        }
        Intent intent = new Intent(this, Graphs.class);
        Bundle b = new Bundle();
        b.putParcelableArrayList("data", (mRecordedData));
        intent.putExtras(b);
        startActivity(intent);
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
        mCameraWidth = width;
        mCameraHeight = height;
    }
    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        System.gc();
        System.runFinalization();

        setColorSpaceMats(inputFrame.rgba());

        if (mCurrentState == 1) {
            mDeltaTime = System.currentTimeMillis() - mLastTime;
            mElapsedTime = System.currentTimeMillis() - mStartTime;
            mLastTime = System.currentTimeMillis();
        }

        Mat reMat = pipeline();

        if (mCurrentState == 0) {
            if (mHsvMat != null) { // use && !mSettingsOpen to prevent changing target while in settings
                int coorX = -999;
                int coorY = -999;

                if (mSetTargetToTouch) {
                    coorX = mTouchX;
                    coorY = mTouchY;

                    int diffX = mScreenWidth - mCameraWidth;
                    int diffY = mScreenHeight - mCameraHeight;

                    coorX -= diffX / 2;
                    coorY -= diffY / 2;

                    mSetTargetToTouch = false;
                }
//                else {
//                    coorX = (int)mCenterX;
//                    coorY = (int)mCenterY;
//                }

                if (coorX >= 0 && coorX < mCameraWidth && coorY >= 0 && coorY < mCameraHeight) {
                    double[] pixel = mHsvMat.get(coorY, coorX);

                    mTargetH = pixel[0];
                    mTargetS = pixel[1];
                    mTargetV = pixel[2];
                }
            }
        }

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
        mRgbaMat = mBgrMat; // For some reason on a Huawei P10 the output needs to be in BGR
        //mRgbaMat = inMat;

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
        MatOfPoint maxContour = null;
        Iterator<MatOfPoint> each = mPreContours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea) {
                maxArea = area;
                maxContour = wrapper;
            }
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
//        each = mPreContours.iterator();
//        while (each.hasNext()) {
//            MatOfPoint contour = each.next();
//            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
//                Core.multiply(contour, new Scalar(1, 1), contour);
//                mContours.add(contour);
//            }
//        }
        if (maxContour != null)
            mContours.add(maxContour);
        else
            return mRgbaMat;

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
//            Moments moments = Imgproc.moments(contour);
//
//            int cX = (int) (moments.get_m10() / moments.get_m00());
//            int cY = (int) (moments.get_m01() / moments.get_m00());

            // Set Output Center Value and Diameter
//            mCenterX = (double) cX;
//            mCenterY = (double) cY;
            mCenterX = center.x;
            mCenterY = center.y;
            mDiameter = 2 * radius[0];

            if (mCurrentState == 1 && mElapsedTime < System.currentTimeMillis() - 1000 && mRealToPixelsRatio != -1)
                mRecordedData.add(new DataEntry(mElapsedTime, mCenterX, mCenterY, mDiameter, mRealToPixelsRatio, mCameraWidth, mCameraHeight));

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
