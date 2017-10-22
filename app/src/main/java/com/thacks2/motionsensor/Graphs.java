package com.thacks2.motionsensor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import org.apache.commons.math3.exception.DimensionMismatchException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nekhilnagia16 on 10/21/17.
 */

public class Graphs extends AppCompatActivity implements Serializable {

    private double mObjLength;
    private List<DataEntry> mEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Bundle bundle = getIntent().getExtras();
        mEntries = bundle.getParcelableArrayList("data");
        mObjLength = bundle.getDouble("obj_length", -1.0);

        Button recordNew = (Button) findViewById(R.id.recordNew);
        recordNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordNewMotion();
            }
        });

        Button retry = (Button) findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryMotion();
            }
        });

        Button horDload = (Button) findViewById(R.id.downloadHorizontal);
        horDload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadHorizontalMotion();
            }
        });

        Button verDload = (Button) findViewById(R.id.downloadVertical);
        verDload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadVerticalMotion();
            }
        });

        plotGraphs();

    }

    private void recordNewMotion() {
        Intent intent = new Intent(this, EnterData.class);
        startActivity(intent);
    }

    private void retryMotion() {
        if (mObjLength > 0) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("length", mObjLength);
            startActivity(intent);
        }
        else {
            recordNewMotion();
        }
    }

    private void downloadHorizontalMotion() {

    }

    private void downloadVerticalMotion() {

    }

    private void plotGraphs() {

        // Horizontal Motion Graph
        double tx [] = new double[mEntries.size()];
        double dx [] = new double[mEntries.size()];

        for (int i=0;i<mEntries.size();i++){
            dx[i] = mEntries.get(i).getX();
            tx[i] = mEntries.get(i).getSecondTime();
        }

        List<Entry> positionX = new ArrayList<>();
        List<Entry> velocityX;
        List<Entry> accelerationX;

        UnivariateInterpolator interpolatorX = new SplineInterpolator();
        UnivariateFunction functionX = interpolatorX.interpolate(tx, dx);

        for (double ix = tx[0]; ix < mEntries.get(mEntries.size()-1).getSecondTime(); ix+=0.2){
            positionX.add(new Entry((float) ix, (float) functionX.value(ix)));
        }

        velocityX = findDerivative(positionX);
        accelerationX = findDerivative(velocityX);

        LineDataSet dataSetDx = new LineDataSet(positionX, "Position"); // add entries to dataset
        LineDataSet dataSetVx = new LineDataSet(velocityX, "Velocity"); // add entries to dataset
        LineDataSet dataSetAx = new LineDataSet(accelerationX, "Acceleration"); // add entries to dataset\

        dataSetDx.setColor(Color.GREEN);
        dataSetVx.setColor(Color.RED);
        dataSetAx.setColor(Color.MAGENTA);


        List<ILineDataSet> dataSetsX = new ArrayList<ILineDataSet>();
        dataSetsX.add(dataSetDx);
        dataSetsX.add(dataSetVx);
        //dataSetsX.add(dataSetAx);

        LineData lineDataX = new LineData(dataSetsX);

        // in this example, a LineChart is initialized from xml
        LineChart chartX = (LineChart) findViewById(R.id.chartX);
        chartX.setData(lineDataX);
        chartX.animateX(3000); // animate horizontal and vertical 3000 milliseconds
        chartX.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(getApplicationContext(),"Selected: (" + Float.toString(e.getX()) + "," + Float.toString(e.getY()) + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        chartX.setDrawBorders(true);
        chartX.setDrawMarkers(false);
        chartX.setHardwareAccelerationEnabled(true);
        chartX.setDrawMarkers(false);
        Description descX = new Description();
        descX.setText("Horizontal Motion");
        descX.setTextSize(16f);
        chartX.setDescription(descX);
        chartX.invalidate(); // refresh


        // Vertical Motion Graph
        double ty [] = new double[mEntries.size()];
        double dy [] = new double[mEntries.size()];

        for (int i=0;i<mEntries.size();i++){
            dy[i] = mEntries.get(i).getY();
            ty[i] = mEntries.get(i).getSecondTime();
        }

        List<Entry> positionY = new ArrayList<>();
        List<Entry> velocityY;
        List<Entry> accelerationY;

        UnivariateInterpolator interpolatorY = new SplineInterpolator();
        UnivariateFunction functionY = interpolatorY.interpolate(ty, dy);

        for (double iy = ty[0]; iy < mEntries.get(mEntries.size()-1).getSecondTime(); iy+=0.2){
            positionY.add(new Entry((float) iy, (float) functionY.value(iy)));
        }

        velocityY = findDerivative(positionY);
        accelerationY = findDerivative(velocityY);

        LineDataSet dataSetDy = new LineDataSet(positionY, "Position"); // add entries to dataset
        LineDataSet dataSetVy = new LineDataSet(velocityY, "Velocity"); // add entries to dataset
        LineDataSet dataSetAy = new LineDataSet(accelerationY, "Acceleration"); // add entries to dataset\

        dataSetDy.setColor(Color.GREEN);
        dataSetVy.setColor(Color.RED);
        dataSetAy.setColor(Color.MAGENTA);


        List<ILineDataSet> dataSetsY = new ArrayList<ILineDataSet>();
        dataSetsY.add(dataSetDy);
        dataSetsY.add(dataSetVy);
        //dataSetsY.add(dataSetAy);

        LineData lineDataY = new LineData(dataSetsY);

        // in this example, a LineChart is initialized from xml
        LineChart chartY = (LineChart) findViewById(R.id.chartY);
        chartY.setData(lineDataY);
        chartY.animateX(3000); // animate horizontal and vertical 3000 milliseconds
        chartY.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(getApplicationContext(),"Selected: (" + Float.toString(e.getX()) + "," + Float.toString(e.getY()) + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        chartY.setDrawBorders(true);
        chartY.setDrawMarkers(false);
        chartY.setHardwareAccelerationEnabled(true);
        chartY.setDrawMarkers(false);
        Description descY = new Description();
        descY.setText("Vertical Motion");
        descY.setTextSize(16f);
        chartY.setDescription(descY);
        chartY.invalidate(); // refresh
    }

    private List<Entry> findDerivative(List<Entry> function){
        List<Entry> derivative = new ArrayList<Entry>();
        for(int i = 1; i < function.size()-1;i++){
            float derivativeBefore = (function.get(i+1).getY() - function.get(i-1).getY()) / (function.get(i+1).getX() - function.get(i-1).getX());
            //float derivativeAfter = (function.get(i+1).getY() - function.get(i).getY()) / (function.get(i+1).getX() - function.get(i).getX());
            float derivativeAtPoint = (derivativeBefore + derivativeBefore) / 2;
            //float derivative = (float)(0.5*Math.cos(i));
            derivative.add(new Entry(((function.get(i).getX())) , derivativeAtPoint));
        }
        return derivative;
    }

}
