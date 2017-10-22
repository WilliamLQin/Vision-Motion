package com.thacks2.motionsensor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nekhilnagia16 on 10/21/17.
 */

public class Graphs extends AppCompatActivity implements Serializable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);
        System.out.println("Graph");
//        Intent intent = getIntent();
//        List <DataEntry> entries = intent.getParcelableExtra("data");

        Bundle bundle = getIntent().getExtras();
        List<DataEntry> entries = bundle.getParcelableArrayList("data");




//        mData = (List<MainActivity.DataEntry>)intent.getSerializableExtra("data");
//
        for (DataEntry data : entries) {
            System.out.println(data);
        }

//        GraphView graph = (GraphView) findViewById(R.id.graph);
//        LineGraphSeries series = new LineGraphSeries();
//
//        Random mRand = new Random();
//
//
//        DataPoint[] values = new DataPoint[30];
//        for (int i=0; i<30; i++) {
//            double x = i;
//            double f = mRand.nextDouble()*0.15+0.3;
//            double y = Math.sin(i);
//            DataPoint v = new DataPoint(x, y);
//            values[i] = v;
//        }
//
//        LineGraphSeries<DataPoint> mSeries1 ;
//        mSeries1 = new LineGraphSeries<>(values);
//
//        graph.addSeries(mSeries1);
//
//
//        // activate horizontal zooming and scrolling
//        graph.getViewport().setScalable(true);
//
//// activate horizontal scrolling
//        graph.getViewport().setScrollable(true);
//
//// activate horizontal and vertical zooming and scrolling
//        graph.getViewport().setScalableY(true);
//
//// activate vertical scrolling
//        graph.getViewport().setScrollableY(true);
//
//        graph.addSeries(series);



        // in this example, a LineChart is initialized from xml
        LineChart chart = (LineChart) findViewById(R.id.chart);

        List<Entry> position = new ArrayList<Entry>();
        List<Entry> velocity = new ArrayList<Entry>();
        List<Entry> acceleration = new ArrayList<Entry>();



        for(DataEntry data:entries){
            position.add(new Entry((float) data.getSecondTime() ,(float) data.getX()));
        }

        System.out.println("Works!!");
        velocity = findDerivative(position);
        acceleration = findDerivative(velocity);

//        for(float i = 0; i <100; i=i+0.5f){
//            float derivative = (float)(0.5*Math.cos(i));
//            velocity.add(new Entry((i) , derivative));
//        }
//        for(int i = 1; i < position.size()-1;i++){
//            float derivativeBefore = (position.get(i).getY() - position.get(i-1).getY()) / (position.get(i).getX() - position.get(i-1).getX());
//            float derivativeAfter = (position.get(i+1).getY() - position.get(i).getY()) / (position.get(i+1).getX() - position.get(i).getX());
//            float derivative = (derivativeAfter + derivativeBefore) / 2;
//            //float derivative = (float)(0.5*Math.cos(i));
//            velocity.add(new Entry(((position.get(i).getX())) , derivative));
//        }
//
//        for(int i = 1; i < velocity.size()-1;i++){
//            float derivativeBefore = (velocity.get(i).getY() - velocity.get(i-1).getY()) / (velocity.get(i).getX() - velocity.get(i-1).getX());
//            float derivativeAfter = (velocity.get(i+1).getY() - velocity.get(i).getY()) / (velocity.get(i+1).getX() - velocity.get(i).getX());
//            float derivative = (derivativeAfter + derivativeBefore) / 2;
//            //float derivative = (float)(0.5*Math.cos(i));
//            acceleration.add(new Entry(((velocity.get(i).getX())) , -1*derivative));
//        }

//        float averageErr,total = 0f;
//
//        for (int i =1; i <acceleration.size()-1;i++){
//            total+=(((acceleration.get(i).getY()- position.get(i).getY()) / position.get(i).getX())*100);
//        }
//        averageErr = (total/(acceleration.size()-2));
//        Log.v(">>MainActivity", Float.toString(averageErr));

        LineDataSet dataSet = new LineDataSet(position, "X"); // add entries to dataset
        LineDataSet dataSet2 = new LineDataSet(velocity, "V"); // add entries to dataset
        LineDataSet dataSet3 = new LineDataSet(acceleration, "A"); // add entries to dataset\


        dataSet.setColor(Color.GREEN);
        dataSet2.setColor(Color.RED);
        dataSet3.setColor(Color.MAGENTA);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(dataSet);
        dataSets.add(dataSet2);
        dataSets.add(dataSet3);

        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);
        //chart.animateX(3000); // animate horizontal and vertical 3000 milliseconds
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //Toast.makeText(getApplicationContext(),"The selected coordinates are (x,y): (" + Float.toString(e.getX()) + "," + Float.toString(e.getY()) + ")",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        chart.setDrawBorders(true);
        chart.setDrawMarkers(false);
        chart.setHardwareAccelerationEnabled(true);
        chart.setDrawMarkers(false);
        chart.invalidate(); // refresh

    }

    private List<Entry> findDerivative(List<Entry> function){
        List<Entry> derivative = new ArrayList<Entry>();
        for(int i = 1; i < function.size()-1;i++){
            float derivativeBefore = (function.get(i).getY() - function.get(i-1).getY()) / (function.get(i).getX() - function.get(i-1).getX());
            float derivativeAfter = (function.get(i+1).getY() - function.get(i).getY()) / (function.get(i+1).getX() - function.get(i).getX());
            float derivativeAtPoint = (derivativeAfter + derivativeBefore) / 2;
            //float derivative = (float)(0.5*Math.cos(i));
            derivative.add(new Entry(((function.get(i).getX())) , derivativeAtPoint));
        }


        return derivative;
    }

}
