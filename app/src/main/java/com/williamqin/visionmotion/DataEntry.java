package com.williamqin.visionmotion;

import android.os.Parcel;
import android.os.Parcelable;

public class DataEntry implements Parcelable
{
    private double time;
    private double x;
    private double y;
    private float diameter;
    private double realToPixelsRatio;
    private double resolutionWidth;
    private double resolutionHeight;

    public DataEntry()
    {
        // Default constructor required for calls to DataSnapshot.getValue(DataEntry.class)
    }

    public DataEntry(long time, double x, double y, float diameter, double realToPixelsRatio, double resolutionWidth, double resolutionHeight)
    {
        this.time = (double)time;
        this.x = x;
        this.y = y;
        this.diameter = diameter;
        this.realToPixelsRatio = realToPixelsRatio;
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;
    }


    protected  DataEntry(Parcel in) {
        time = in.readDouble();
        x = in.readDouble();
        y = in.readDouble();
        diameter = in.readFloat();
        realToPixelsRatio = in.readDouble();
        resolutionWidth = in.readDouble();
        resolutionHeight = in.readDouble();
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
    public double getReverseRawX() {
        return resolutionWidth - x;
    }
    public double getX()
    {
        return getDistanceInUnits(x);
    }
    public double getReverseX() {
        return getDistanceInUnits(resolutionWidth - x);
    }
    public double getRawY()
    {
        return y;
    }
    public double getReverseRawY() {
        return resolutionHeight - y;
    }
    public double getY()
    {
        return getDistanceInUnits(y);
    }
    public double getReverseY() {
        return getDistanceInUnits(resolutionHeight - y);
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
        return "DataEntry: T=" + getSecondTime() + " X=" + getRawX() + "/" + resolutionWidth + " Y=" + getRawY() + "/" + resolutionHeight + " Diameter=" + getDiameter();
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
        dest.writeDouble(resolutionWidth);
        dest.writeDouble(resolutionHeight);
    }
}