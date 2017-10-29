package com.thacks2.motionsensor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.dfp.DfpField;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.geometry.euclidean.threed.Line;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Graphs extends AppCompatActivity implements Serializable {

    private ArrayList<DataEntry> mEntries;

    private LineChart mChartX;
    private LineChart mChartY;

    private List<Entry> mPositionX = new ArrayList<>(), mVelocityX, mAccelerationX;
    private List<Entry> mPositionY = new ArrayList<>(), mVelocityY, mAccelerationY;

    private LineDataSet mRegressionX;
    private LineDataSet mRegressionY;

    private boolean mShowPos = true, mShowVel = true, mShowAcc = false;

    private ImageButton mHome, mDownload, mPos, mVel, mAcc, mHor, mVer, mRegression;
    private boolean mChangingRegression = false;
    private int mHorDisplayState, mVerDisplayState; // 0 for display, 1 for backwards, 2 for hidden
    private Entry mInitialHor, mFinalHor, mInitialVer, mFinalVer;
    private int mRegressionDegree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mChartY = (LineChart) findViewById(R.id.chartY);
        mChartX = (LineChart) findViewById(R.id.chartX);

        Bundle bundle = getIntent().getExtras();
        mEntries = bundle.getParcelableArrayList("data");

        plotGraphs();
        drawHorizontalGraph();
        drawVerticalGraph();

        mHome = (ImageButton) findViewById(R.id.home);
        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToHome();
            }
        });

        mDownload = (ImageButton) findViewById(R.id.download);
        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadMotion();
            }
        });

        mPos = (ImageButton) findViewById(R.id.showPosition);
        mPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHidePosition();
                if (!mShowPos) {
                    mPos.setImageResource(R.drawable.graph_position_disabled);
                }
                else {
                    mPos.setImageResource(R.drawable.graph_position_coloured);
                }
            }
        });

        mVel = (ImageButton) findViewById(R.id.showVelocity);
        mVel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideVelocity();
                if (!mShowVel) {
                    mVel.setImageResource(R.drawable.graph_velocity_disabled);
                }
                else {
                    mVel.setImageResource(R.drawable.graph_velocity_coloured);
                }
            }
        });

        mAcc = (ImageButton) findViewById(R.id.showAcceleration);
        mAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideAcceleration();
                if (!mShowAcc) {
                    mAcc.setImageResource(R.drawable.graph_acceleration_disabled);
                }
                else {
                    mAcc.setImageResource(R.drawable.graph_acceleration_coloured);
                }
            }
        });

        mHor = (ImageButton) findViewById(R.id.showHorizontal);
        mHor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeViewHorizontal();
            }
        });

        mVer = (ImageButton) findViewById(R.id.showVertical);
        mVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeViewVertical();
            }
        });

        mRegression = (ImageButton) findViewById(R.id.regression);
        mRegression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChangingRegression = !mChangingRegression;
                if (mChangingRegression) {
                    mRegression.setImageResource(R.drawable.graph_regression_coloured);
                }
                else {
                    mRegression.setImageResource(R.drawable.graph_regression_disabled);

                    if (mInitialHor != null || mFinalHor != null || mRegressionX != null) {
                        mInitialHor = null;
                        mFinalHor = null;
                        mRegressionX = null;
                        drawHorizontalGraph();
                    }

                    if (mInitialVer != null || mFinalVer != null || mRegressionY != null) {
                        mInitialVer = null;
                        mFinalVer = null;
                        mRegressionY = null;
                        drawVerticalGraph();
                    }
                }
            }
        });

        Spinner numPick = (Spinner) findViewById(R.id.regressionDegree);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.degree_array, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        numPick.setAdapter(adapter);

        numPick.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mRegressionDegree = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        numPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                mRegressionDegree = newVal - 2;
//            }
//        });
//        numPick.setMinValue(0);
//        numPick.setMaxValue(4);
//        numPick.setWrapSelectorWheel(false);
//        numPick.setFormatter(new NumberPicker.Formatter() {
//            @Override
//            public String format(int value) {
//                return Integer.toString(value - 2);
//            }
//        });

    }

    private void backToHome() {
        Intent intent = new Intent(this, Landing.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        mEntries.clear();
        super.onBackPressed();
    }

    private void downloadMotion() {
        retrieveSaveData();
        promptForName();
    }

    private void showHidePosition() {
        mShowPos = !mShowPos;
        drawHorizontalGraph();
        drawVerticalGraph();
    }
    private void showHideVelocity() {
        mShowVel = !mShowVel;
        drawHorizontalGraph();
        drawVerticalGraph();
    }
    private void showHideAcceleration() {
        mShowAcc = !mShowAcc;
        drawHorizontalGraph();
        drawVerticalGraph();
    }

    private void changeViewHorizontal() {
        if (mHorDisplayState == 2) { // Going from disabled state to normal state
            mChartX.setVisibility(LineChart.VISIBLE);
            mHor.setImageResource(R.drawable.graph_horizontal);
            mHor.setRotation(0);
            mHorDisplayState = 0;
        }
        else if (mHorDisplayState == 1) { // Going from flipped state to disabled state
            mChartX.setVisibility(LineChart.GONE);
            mHor.setImageResource(R.drawable.graph_xmark_disabled);
            mHor.setRotation(0);
            mHorDisplayState = 2;
        }
        else { // Going from normal state to flipped state
            mHor.setRotation(180);
            mHorDisplayState = 1;
        }
        mInitialHor = null;
        mFinalHor = null;
        mRegressionX = null;

        plotGraphs();
        drawHorizontalGraph();
    }

    private void changeViewVertical() {
        if (mVerDisplayState == 2) { // Going from disabled state to normal state
            mChartY.setVisibility(LineChart.VISIBLE);
            mVer.setImageResource(R.drawable.graph_vertical);
            mVer.setRotation(0);
            mVerDisplayState = 0;
        }
        else if (mVerDisplayState == 1) { // Going from flipped state to disabled state
            mChartY.setVisibility(LineChart.GONE);
            mVer.setImageResource(R.drawable.graph_xmark_disabled);
            mVer.setRotation(0);
            mVerDisplayState = 2;
        }
        else { // Going from normal state to flipped state
            mVer.setRotation(180);
            mVerDisplayState = 1;
        }
        mInitialVer = null;
        mFinalVer = null;
        mRegressionY = null;

        plotGraphs();
        drawVerticalGraph();
    }

    private void redrawRegressionHorizontal() {

        // Check if there are entries selected

        if (mInitialHor == null || mFinalHor == null) {
            return;
        }

        // Check if both selected entries are from same line

        int lineType = 0;
        int initialIndex;
        int finalIndex;

        initialIndex = mPositionX.indexOf(mInitialHor);
        finalIndex = mPositionX.indexOf(mFinalHor);

            // If both not from position line
        if (initialIndex == -1 && finalIndex == -1) {
            initialIndex = mVelocityX.indexOf(mInitialHor);
            finalIndex = mVelocityX.indexOf(mFinalHor);
            lineType = 1;
        }

            // If both not from velocity line
        if (initialIndex == -1 && finalIndex == -1) {
            initialIndex = mAccelerationX.indexOf(mInitialHor);
            finalIndex = mAccelerationX.indexOf(mFinalHor);
            lineType = 2;
        }

            // If one from one line and one from another
        if (initialIndex == -1 || finalIndex == -1) {
            Toast.makeText(getApplicationContext(),"Please select two points from the same line.", Toast.LENGTH_LONG).show();
            return;
        }

        // Gather all data in between two lines

        List<Entry> targetDataSet;
        if (lineType == 0)
            targetDataSet = mPositionX;
        else if (lineType == 1)
            targetDataSet = mVelocityX;
        else
            targetDataSet = mAccelerationX;

        List<WeightedObservedPoint> points = new ArrayList<>();
        for (int i = initialIndex; i <= finalIndex; i++) {
            points.add(new WeightedObservedPoint(1, targetDataSet.get(i).getX(), targetDataSet.get(i).getY()));
        }

        // Create polynomial curve of best fit
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(mRegressionDegree);
        PolynomialFunction function = new PolynomialFunction(fitter.fit(points));

        List<Entry> regressionData = new ArrayList<>();

        for (double i = 0; i < mEntries.get(mEntries.size()-1).getSecondTime(); i+=0.02){
            regressionData.add(new Entry((float) i, (float) function.value(i)));
        }

        // Get equation
        String equation = "";
        double[] coefficients = function.getCoefficients();
        for (int j = coefficients.length - 1; j >= 0; j--) {
            String coefficient = String.format("%.2f", coefficients[j]);

            if (j == 1)
                equation += coefficient + "x + ";
            else if (j != 0)
                equation += coefficient + "x^" + j + " + ";
            else
                equation += coefficient;
        }

        equation = equation.replace("+ -", "- ");

        // Set data to LineDataSet
        mRegressionX = new LineDataSet(regressionData, equation);
        mRegressionX.setDrawCircles(false);
        mRegressionX.setDrawValues(false);
        mRegressionX.setHighlightEnabled(false);
        mRegressionX.setLineWidth(2.0f);

    }

    private void redrawRegressionVertical() {

        // Check if there are entries selected

        if (mInitialVer == null || mFinalVer == null) {
            return;
        }

        // Check if both selected entries are from same line

        int lineType = 0;
        int initialIndex;
        int finalIndex;

        initialIndex = mPositionY.indexOf(mInitialVer);
        finalIndex = mPositionY.indexOf(mFinalVer);

        // If both not from position line
        if (initialIndex == -1 && finalIndex == -1) {
            initialIndex = mVelocityY.indexOf(mInitialVer);
            finalIndex = mVelocityY.indexOf(mFinalVer);
            lineType = 1;
        }

        // If both not from velocity line
        if (initialIndex == -1 && finalIndex == -1) {
            initialIndex = mAccelerationY.indexOf(mInitialVer);
            finalIndex = mAccelerationY.indexOf(mFinalVer);
            lineType = 2;
        }

        // If one from one line and one from another
        if (initialIndex == -1 || finalIndex == -1) {
            Toast.makeText(getApplicationContext(),"Please select two points from the same line.", Toast.LENGTH_LONG).show();
            return;
        }

        // Gather all data in between two lines

        List<Entry> targetDataSet;
        if (lineType == 0)
            targetDataSet = mPositionY;
        else if (lineType == 1)
            targetDataSet = mVelocityY;
        else
            targetDataSet = mAccelerationY;

        List<WeightedObservedPoint> points = new ArrayList<>();
        for (int i = initialIndex; i <= finalIndex; i++) {
            points.add(new WeightedObservedPoint(1, targetDataSet.get(i).getX(), targetDataSet.get(i).getY()));
        }

        // Create polynomial curve of best fit
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(mRegressionDegree);
        PolynomialFunction function = new PolynomialFunction(fitter.fit(points));

        List<Entry> regressionData = new ArrayList<>();

        for (double i = 0; i < mEntries.get(mEntries.size()-1).getSecondTime(); i+=0.02){
            regressionData.add(new Entry((float) i, (float) function.value(i)));
        }

        // Get equation
        String equation = "";
        double[] coefficients = function.getCoefficients();
        for (int j = coefficients.length - 1; j >= 0; j--) {
            String coefficient = String.format("%.2f", coefficients[j]);

            if (j == 1)
                equation += coefficient + "x + ";
            else if (j != 0)
                equation += coefficient + "x^" + j + " + ";
            else
                equation += coefficient;
        }

        equation = equation.replace("+ -", "- ");

        // Set data to LineDataSet
        mRegressionY = new LineDataSet(regressionData, equation);
        mRegressionY.setDrawCircles(false);
        mRegressionY.setDrawValues(false);
        mRegressionY.setHighlightEnabled(false);
        mRegressionY.setLineWidth(2.0f);

    }

    private OnChartValueSelectedListener mChartXSelectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry entry, Highlight highlight) {
            Toast.makeText(getApplicationContext(),"Selected: (" + Float.toString(entry.getX()) + "," + Float.toString(entry.getY()) + ")", Toast.LENGTH_LONG).show();
            if (mChangingRegression) {
                if (mInitialHor == null) {
                    mInitialHor = entry;
                }
                else if (mFinalHor == null) {
                    if (entry.getX() < mInitialHor.getX()) {
                        mFinalHor = mInitialHor;
                        mInitialHor = entry;
                    }
                    else {
                        mFinalHor = entry;
                    }

                    redrawRegressionHorizontal();
                    drawHorizontalGraph();
                }
                else {
                    mInitialHor = entry;
                    mFinalHor = null;
                }
            }
        }

        @Override
        public void onNothingSelected() {

        }
    };

    private OnChartValueSelectedListener mChartYSelectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry entry, Highlight highlight) {
            Toast.makeText(getApplicationContext(),"Selected: (" + Float.toString(entry.getX()) + "," + Float.toString(entry.getY()) + ")", Toast.LENGTH_LONG).show();
            if (mChangingRegression) {
                if (mInitialVer == null) {
                    mInitialVer = entry;
                }
                else if (mFinalVer == null) {
                    if (entry.getX() < mInitialVer.getX()) {
                        mFinalVer = mInitialVer;
                        mInitialVer = entry;
                    }
                    else {
                        mFinalVer = entry;
                    }

                    redrawRegressionVertical();
                    drawVerticalGraph();
                }
                else {
                    mInitialVer = entry;
                    mFinalVer = null;
                }
            }
        }

        @Override
        public void onNothingSelected() {

        }
    };

    private void drawHorizontalGraph() {

        // Draw horizontal motion graph
        LineDataSet dataSetDx = new LineDataSet(mPositionX, "Position"); // add entries to dataset
        LineDataSet dataSetVx = new LineDataSet(mVelocityX, "Velocity"); // add entries to dataset
        LineDataSet dataSetAx = new LineDataSet(mAccelerationX, "Acceleration"); // add entries to dataset

        dataSetDx.setColor(Color.RED);
        dataSetDx.setCircleColor(Color.RED);
        dataSetDx.setCircleColorHole(Color.RED);
        dataSetDx.setHighlightLineWidth(2.0f);
        dataSetVx.setColor(Color.BLUE);
        dataSetVx.setCircleColor(Color.BLUE);
        dataSetVx.setCircleColorHole(Color.BLUE);
        dataSetVx.setHighlightLineWidth(2.0f);
        dataSetAx.setColor(Color.GREEN);
        dataSetAx.setCircleColor(Color.GREEN);
        dataSetAx.setCircleColorHole(Color.GREEN);
        dataSetAx.setHighlightLineWidth(2.0f);

        List<ILineDataSet> dataSetsX = new ArrayList<>();
        if (mShowPos)
            dataSetsX.add(dataSetDx);
        if (mShowVel)
            dataSetsX.add(dataSetVx);
        if (mShowAcc)
            dataSetsX.add(dataSetAx);
        if (mRegressionX != null)
            dataSetsX.add(mRegressionX);


        LineData lineDataX = new LineData(dataSetsX);

        mChartX.setData(lineDataX);

        mChartX.animateX(1000); // animate horizontal and vertical 3000 milliseconds
        mChartX.setOnChartValueSelectedListener(mChartXSelectedListener);
        mChartX.setDrawBorders(true);
        mChartX.setDrawMarkers(false);
        mChartX.setHardwareAccelerationEnabled(true);
        Description descX = new Description();
        descX.setText("Horizontal Motion");
        descX.setTextSize(16f);
        mChartX.setDescription(descX);
        mChartX.invalidate(); // refresh

    }

    private void drawVerticalGraph() {

        // Draw vertical motion graph
        LineDataSet dataSetDy = new LineDataSet(mPositionY, "Position"); // add entries to dataset
        LineDataSet dataSetVy = new LineDataSet(mVelocityY, "Velocity"); // add entries to dataset
        LineDataSet dataSetAy = new LineDataSet(mAccelerationY, "Acceleration"); // add entries to dataset

        dataSetDy.setColor(Color.RED);
        dataSetDy.setCircleColor(Color.RED);
        dataSetDy.setCircleColorHole(Color.RED);
        dataSetDy.setHighlightLineWidth(2.0f);
        dataSetVy.setColor(Color.BLUE);
        dataSetVy.setCircleColor(Color.BLUE);
        dataSetVy.setCircleColorHole(Color.BLUE);
        dataSetVy.setHighlightLineWidth(2.0f);
        dataSetAy.setColor(Color.GREEN);
        dataSetAy.setCircleColor(Color.GREEN);
        dataSetAy.setCircleColorHole(Color.GREEN);
        dataSetAy.setHighlightLineWidth(2.0f);

        List<ILineDataSet> dataSetsY = new ArrayList<>();
        if (mShowPos)
            dataSetsY.add(dataSetDy);
        if (mShowVel)
            dataSetsY.add(dataSetVy);
        if (mShowAcc)
            dataSetsY.add(dataSetAy);
        if (mRegressionY != null)
            dataSetsY.add(mRegressionY);

        LineData lineDataY = new LineData(dataSetsY);


        mChartY.setData(lineDataY);
        mChartY.animateX(1000); // animate horizontal and vertical 3000 milliseconds
        mChartY.setOnChartValueSelectedListener(mChartYSelectedListener);
        mChartY.setDrawBorders(true);
        mChartY.setDrawMarkers(false);
        mChartY.setHardwareAccelerationEnabled(true);
        Description descY = new Description();
        descY.setText("Vertical Motion");
        descY.setTextSize(16f);
        mChartY.setDescription(descY);
        mChartY.invalidate(); // refresh

    }

    private void plotGraphs() {

    // -- Horizontal Motion Graph --

        // Get entry data
        double tx [] = new double[mEntries.size()];
        double dx [] = new double[mEntries.size()];

        for (int i=0;i<mEntries.size();i++){
            tx[i] = mEntries.get(i).getSecondTime();
            if (mHorDisplayState == 0)
                dx[i] = mEntries.get(i).getX();
            else if (mHorDisplayState == 1)
                dx[i] = mEntries.get(i).getReverseX();
            else
                dx[i] = 0;
        }

        // Initialize position list
        mPositionX.clear();

        // Add entry data to position list

//        final double rateX = 0.02;
//        SplineInterpolator interpolatorX = new SplineInterpolator();
//        PolynomialSplineFunction functionX = interpolatorX.interpolate(tx, dx);
//
//        for (double ix = tx[0]; ix < mEntries.get(mEntries.size()-1).getSecondTime(); ix+=rateX){
//            positionX.add(new Entry((float) ix, (float) functionX.value(ix)));
//        }

        for (int i = 0; i < mEntries.size(); i++) {
            mPositionX.add(new Entry((float)tx[i], (float)dx[i]));
        }

        // Find first and second derivative for velocity and acceleration respectively
        mVelocityX = findDerivative(mPositionX, 2);
        mAccelerationX = findDerivative(mVelocityX, 3);


    // Vertical Motion Graph

        // Get entry data
        double ty [] = new double[mEntries.size()];
        double dy [] = new double[mEntries.size()];

        for (int i=0;i<mEntries.size();i++){
            ty[i] = mEntries.get(i).getSecondTime();
            if (mVerDisplayState == 0)
                dy[i] = mEntries.get(i).getY();
            else if (mVerDisplayState == 1)
                dy[i] = mEntries.get(i).getReverseY();
            else
                dy[i] = 0;
        }

        // Initialize position list
        mPositionY.clear();

        // Add entry data to position list

//        final double rateY = 0.02;
//        SplineInterpolator interpolatorY = new SplineInterpolator();
//        PolynomialSplineFunction functionY = interpolatorY.interpolate(ty, dy);
//
//        for (double iy = ty[0]; iy < mEntries.get(mEntries.size()-1).getSecondTime(); iy+=rateY){
//            positionY.add(new Entry((float) iy, (float) functionY.value(iy)));
//        }

        for (int i = 0; i < mEntries.size(); i++) {
            mPositionY.add(new Entry((float)ty[i], (float)dy[i]));
        }

        // Find first and second derivative for velocity and acceleration respectively
        mVelocityY = findDerivative(mPositionY, 2);
        mAccelerationY = findDerivative(mVelocityY, 3);


    }

    private List<Entry> findDerivative(List<Entry> function, int averageDerivativeRange){
        List<Entry> derivative = new ArrayList<Entry>();
        for(int i = 0; i < function.size();i++){
            float sumDerivatives = 0f;
            int totalDerivatives = 0;
            for (int j = 1; j <= averageDerivativeRange; j++) {
                if (i - j >= 0) {
                    sumDerivatives += (function.get(i).getY() - function.get(i-j).getY()) / (function.get(i).getX() - function.get(i-j).getX());
                    totalDerivatives ++;
                }
                if (i + j < function.size()) {
                    sumDerivatives += (function.get(i+j).getY() - function.get(i).getY()) / (function.get(i+j).getX() - function.get(i).getX());
                    totalDerivatives ++;
                }
            }

            float derivativeAtPoint = sumDerivatives / totalDerivatives;

            derivative.add(new Entry((function.get(i).getX()) , derivativeAtPoint));
        }
        return derivative;
    }

    // Saving data stuff


    private class CSVEntry {
        double time;
        double horizontalPosition;
        double horizontalVelocity;
        double horizontalAcceleration;
        double verticalPosition;
        double verticalVelocity;
        double verticalAcceleration;

        public void setTime(double time) {
            this.time = time;
        }
        public void setHorizontalPosition(double horizontalPosition) {
            this.horizontalPosition = horizontalPosition;
        }
        public void setHorizontalVelocity(double horizontalVelocity) {
            this.horizontalVelocity = horizontalVelocity;
        }
        public void setHorizontalAcceleration(double horizontalAcceleration) {
            this.horizontalAcceleration = horizontalAcceleration;
        }
        public void setVerticalPosition(double verticalPosition) {
            this.verticalPosition = verticalPosition;
        }
        public void setVerticalVelocity(double verticalVelocity) {
            this.verticalVelocity = verticalVelocity;
        }
        public void setVerticalAcceleration(double verticalAcceleration) {
            this.verticalAcceleration = verticalAcceleration;
        }


        public double getTime() {
            return time;
        }
        public double getHorizontalPosition() {
            return horizontalPosition;
        }
        public double getHorizontalVelocity() {
            return horizontalVelocity;
        }
        public double getHorizontalAcceleration() {
            return horizontalAcceleration;
        }
        public double getVerticalPosition() {
            return verticalPosition;
        }
        public double getVerticalVelocity() {
            return verticalVelocity;
        }
        public double getVerticalAcceleration() {
            return verticalAcceleration;
        }
    }

    private List<CSVEntry> mSaveData = new ArrayList<>();

    //CSV file header
    private static final String FILE_HEADER = "Time(s), HorizontalPosition(m), HorizontalVelocity(m/s), HorizontalAcceleration(m/s^2), VerticalPosition(m), VerticalVelocity(m/s), VerticalAcceleration(m/s^2)";

    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    public void retrieveSaveData() {

        // Horizontal motion
        for (Entry entry : mPositionX) {
            CSVEntry csvEntry = new CSVEntry();
            csvEntry.setTime(entry.getX());
            csvEntry.setHorizontalPosition(entry.getY());
            mSaveData.add(csvEntry);
        }
        int velXCounter = 0;
        for (Entry entry : mVelocityX) {
            mSaveData.get(velXCounter).setHorizontalVelocity(entry.getY());
            velXCounter += 1;
        }
        int accXCounter = 0;
        for (Entry entry : mAccelerationX) {
            mSaveData.get(accXCounter).setHorizontalAcceleration(entry.getY());
            accXCounter += 1;
        }

        // Vertical motion
        int posYCounter = 0;
        for (Entry entry : mPositionY) {
            mSaveData.get(posYCounter).setVerticalPosition(entry.getY());
            posYCounter++;
        }
        int velYCounter = 0;
        for (Entry entry : mVelocityY) {
            mSaveData.get(velYCounter).setVerticalVelocity(entry.getY());
            velYCounter++;
        }
        int accYCounter = 0;
        for (Entry entry : mAccelerationY) {
            mSaveData.get(accYCounter).setVerticalAcceleration(entry.getY());
            accYCounter++;
        }

    }

    public void promptForName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter name for file:");

// Set up the input
        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if (!name.endsWith(".csv")) {
                    name += ".csv";
                }
                writeCsvFile(name);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void writeCsvFile(String name) {

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path + "/" + name);
        FileWriter fr = null;
        BufferedWriter br = null;
        try{
            fr = new FileWriter(file);
            br = new BufferedWriter(fr);

            br.write(FILE_HEADER);

            for (CSVEntry data : mSaveData) {
                br.write(NEW_LINE_SEPARATOR);

                String line = "";
                line += String.format("%.3f", data.getTime());;
                line += COMMA_DELIMITER;
                line += data.getHorizontalPosition();
                line += COMMA_DELIMITER;
                line += data.getHorizontalVelocity();
                line += COMMA_DELIMITER;
                line += data.getHorizontalAcceleration();
                line += COMMA_DELIMITER;
                line += data.getVerticalPosition();
                line += COMMA_DELIMITER;
                line += data.getVerticalVelocity();
                line += COMMA_DELIMITER;
                line += data.getVerticalAcceleration();

                br.write(line);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                br.close();
                fr.close();

                Toast.makeText(getApplicationContext(), "Successfully saved " + name + " to " + path.toString(), Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
