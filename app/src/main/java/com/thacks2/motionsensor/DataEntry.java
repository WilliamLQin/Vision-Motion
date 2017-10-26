package com.thacks2.motionsensor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nekhilnagia16 on 10/21/17.
 */

public class DataEntry implements Parcelable
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


    protected  DataEntry(Parcel in) {
        time = in.readDouble();
        x = in.readDouble();
        y = in.readDouble();
        diameter = in.readFloat();
        realToPixelsRatio = in.readDouble();
    }

    public static final Creator<DataEntry> CREATOR = new Creator<DataEntry>() {
        @Override
        public DataEntry createFromParcel(Parcel in) {
            return new DataEntry(in);
        }

        @Override
        public DataEntry[] newArray(int size) {
            return new DataEntry[size];
        }
    };

    public double getSecondTime()
    {
        return time/1000.0;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(time);
        dest.writeDouble(x);
        dest.writeDouble(y);
        dest.writeFloat(diameter);
        dest.writeDouble(realToPixelsRatio);
    }
}