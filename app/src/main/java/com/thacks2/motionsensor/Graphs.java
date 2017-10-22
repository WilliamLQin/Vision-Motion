package com.thacks2.motionsensor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
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
import org.apache.commons.math3.geometry.euclidean.threed.Line;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nekhilnagia16 on 10/21/17.
 */

public class Graphs extends AppCompatActivity implements Serializable {

    private double mObjLength;
    private List<DataEntry> mEntries;

    private LineChart mChartX;
    private LineChart mChartY;

    private LineDataSet mDataSetDx, mDataSetVx, mDataSetAx;
    private LineDataSet mDataSetDy, mDataSetVy, mDataSetAy;

    private boolean mShowPos = true, mShowVel = true, mShowAcc = false;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mChartY = (LineChart) findViewById(R.id.chartY);
        mChartX = (LineChart) findViewById(R.id.chartX);

        Bundle bundle = getIntent().getExtras();
        mEntries = bundle.getParcelableArrayList("data");
        mObjLength = bundle.getDouble("obj_length", -1.0);

        plotGraphs();
        drawGraphs();

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

        Button download = (Button) findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadMotion();
            }
        });

        Button pos = (Button) findViewById(R.id.showPosition);
        pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHidePosition();
            }
        });

        Button vel = (Button) findViewById(R.id.showVelocity);
        vel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideVelocity();
            }
        });

        Button acc = (Button) findViewById(R.id.showAcceleration);
        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideAcceleration();
            }
        });

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

    private void downloadMotion() {
        promptForName();
    }

    private void showHidePosition() {
        mShowPos = !mShowPos;
        drawGraphs();
    }
    private void showHideVelocity() {
        mShowVel = !mShowVel;
        drawGraphs();
    }
    private void showHideAcceleration() {
        mShowAcc = !mShowAcc;
        drawGraphs();
    }

    public void promptForName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

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

    //CSV file header
    private static final String FILE_HEADER = "Time(s), HorizontalPosition(m), HorizontalVelocity(m/s), HorizontalAcceleration(m/s^2), VerticalPosition(m), VerticalVelocity(m/s), VerticalAcceleration(m/s^2)";

    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

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

    private void drawGraphs() {

        // Draw horizontal motion graph
        List<ILineDataSet> dataSetsX = new ArrayList<>();
        if (mShowPos)
            dataSetsX.add(mDataSetDx);
        if (mShowVel)
            dataSetsX.add(mDataSetVx);
        if (mShowAcc)
            dataSetsX.add(mDataSetAx);

        LineData lineDataX = new LineData(dataSetsX);

        mChartX.setData(lineDataX);

        mChartX.animateX(1000); // animate horizontal and vertical 3000 milliseconds
        mChartX.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(getApplicationContext(),"Selected: (" + Float.toString(e.getX()) + "," + Float.toString(e.getY()) + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        mChartX.setDrawBorders(true);
        mChartX.setDrawMarkers(false);
        mChartX.setHardwareAccelerationEnabled(true);
        mChartX.setDrawMarkers(false);
        Description descX = new Description();
        descX.setText("Horizontal Motion");
        descX.setTextSize(16f);
        mChartX.setDescription(descX);
        mChartX.invalidate(); // refresh


        // Draw vertical motion graph
        List<ILineDataSet> dataSetsY = new ArrayList<>();
        if (mShowPos)
            dataSetsY.add(mDataSetDy);
        if (mShowVel)
            dataSetsY.add(mDataSetVy);
        if (mShowAcc)
            dataSetsY.add(mDataSetAy);

        LineData lineDataY = new LineData(dataSetsY);


        mChartY.setData(lineDataY);
        mChartY.animateX(1000); // animate horizontal and vertical 3000 milliseconds
        mChartY.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(getApplicationContext(),"Selected: (" + Float.toString(e.getX()) + "," + Float.toString(e.getY()) + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        mChartY.setDrawBorders(true);
        mChartY.setDrawMarkers(false);
        mChartY.setHardwareAccelerationEnabled(true);
        mChartY.setDrawMarkers(false);
        Description descY = new Description();
        descY.setText("Vertical Motion");
        descY.setTextSize(16f);
        mChartY.setDescription(descY);
        mChartY.invalidate(); // refresh

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

        for (Entry entry : positionX) {
            CSVEntry csvEntry = new CSVEntry();
            csvEntry.setTime(entry.getX());
            csvEntry.setHorizontalPosition(entry.getY());
            mSaveData.add(csvEntry);
        }
        int velXCounter = 1;
        for (Entry entry : velocityX) {
            mSaveData.get(velXCounter).setHorizontalVelocity(entry.getY());
            velXCounter += 1;
        }
        int accXCounter = 2;
        for (Entry entry : accelerationX) {
            mSaveData.get(accXCounter).setHorizontalAcceleration(entry.getY());
            accXCounter += 1;
        }



        mDataSetDx = new LineDataSet(positionX, "Position"); // add entries to dataset
        mDataSetVx = new LineDataSet(velocityX, "Velocity"); // add entries to dataset
        mDataSetAx = new LineDataSet(accelerationX, "Acceleration"); // add entries to dataset\

        mDataSetDx.setColor(Color.GREEN);
        mDataSetVx.setColor(Color.RED);
        mDataSetAx.setColor(Color.MAGENTA);



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

        int posYCounter = 0;
        for (Entry entry : positionY) {
            mSaveData.get(posYCounter).setVerticalPosition(entry.getY());
            posYCounter++;
        }
        int velYCounter = 1;
        for (Entry entry : velocityY) {
            mSaveData.get(velYCounter).setVerticalVelocity(entry.getY());
            velYCounter++;
        }
        int accYCounter = 2;
        for (Entry entry : accelerationY) {
            mSaveData.get(accYCounter).setVerticalAcceleration(entry.getY());
            accYCounter++;
        }



        mDataSetDy = new LineDataSet(positionY, "Position"); // add entries to dataset
        mDataSetVy = new LineDataSet(velocityY, "Velocity"); // add entries to dataset
        mDataSetAy = new LineDataSet(accelerationY, "Acceleration"); // add entries to dataset\

        mDataSetDy.setColor(Color.GREEN);
        mDataSetVy.setColor(Color.RED);
        mDataSetAy.setColor(Color.MAGENTA);

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
