package com.williamqin.visionmotion;

import android.os.Parcel;
import android.os.Parcelable;

public class DataEntry implements Parcelable
{
    private double time;
    private double rawX;
    private double rawY;
    private double rawDiameter;
    private double realToPixelsRatio;
    private double resolutionWidth;
    private double resolutionHeight;

    public DataEntry()
    {
        // Default constructor required for calls to DataSnapshot.getValue(DataEntry.class)
    }

    public DataEntry(long time, double rawX, double rawY, double rawDiameter, double realToPixelsRatio, double resolutionWidth, double resolutionHeight)
    {
        this.time = (double)time;
        this.rawX = rawX;
        this.rawY = rawY;
        this.rawDiameter = rawDiameter;
        this.realToPixelsRatio = realToPixelsRatio;
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;
    }


    protected  DataEntry(Parcel in) {
        time = in.readDouble();
        rawX = in.readDouble();
        rawY = in.readDouble();
        rawDiameter = in.readDouble();
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
        return rawX;
    }
    public double getReverseRawX()
    {
        return resolutionWidth - rawX;
    }

    public double getX()
    {
        return getDistanceInUnits(rawX);
    }
    public double getReverseX() {
        return getDistanceInUnits(resolutionWidth - rawX);
    }

    public double getRawY()
    {
        return rawY;
    }
    public double getReverseRawY()
    {
        return resolutionHeight - rawY;
    }

    public double getY()
    {
        return getDistanceInUnits(rawY);
    }
    public double getReverseY() {
        return getDistanceInUnits(resolutionHeight - rawY);
    }

    public double getRawDiameter()
    {
        return rawDiameter;
    }
    public double getDiameter() {
        return getDistanceInUnits(rawDiameter);
    }

    public double getRealToPixelsRatio() {
        return realToPixelsRatio;
    }
    public double getResolutionWidth() {
        return resolutionWidth;
    }
    public double getResolutionHeight() {
        return resolutionHeight;
    }

    private double getDistanceInUnits(double length) {
        return realToPixelsRatio * (length);
    }
    private double getUnitsInDistance(double distance) {
        return distance / realToPixelsRatio;
    }



    @Override
    public String toString() {
        return "DataEntry: T=" + getSecondTime() + " X=" + rawX + "/" + resolutionWidth + " Y=" + rawY + "/" + resolutionHeight + " Diameter=" + rawDiameter;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(time);
        dest.writeDouble(rawX);
        dest.writeDouble(rawY);
        dest.writeDouble(rawDiameter);
        dest.writeDouble(realToPixelsRatio);
        dest.writeDouble(resolutionWidth);
        dest.writeDouble(resolutionHeight);
    }

    public void setSecondTime(double secondTime) {
        this.time = secondTime * 1000;
    }
    public void setMillisecondTime(double time) {
        this.time = time;
    }

    public void setRawX(double rawX) {
        this.rawX = rawX;
    }
    public void setReverseRawX(double reverseRawX) {
        this.rawX = resolutionWidth - reverseRawX;
    }

    public void setX(double x) {
        this.rawX = getUnitsInDistance(x);
    }
    public void setReverseX(double reverseX) {
        this.rawX = getUnitsInDistance(resolutionWidth - reverseX);
    }

    public void setRawY(double rawY) {
        this.rawY = rawY;
    }
    public void setReverseRawY(double reverseRawY) {
        this.rawY = resolutionHeight - reverseRawY;
    }

    public void setY(double y) {
        this.rawY = getUnitsInDistance(y);
    }
    public void setReverseY(double reverseY) {
        this.rawY = getUnitsInDistance(resolutionHeight - reverseY);
    }

    public void setRawDiameter(double rawDiameter) {
        this.rawDiameter = rawDiameter;
    }
    public void setDiameter(double diameter) {
        this.rawDiameter = getUnitsInDistance(diameter);
    }

    public void setRealToPixelsRatio(double realToPixelsRatio) {
        this.realToPixelsRatio = realToPixelsRatio;
    }
    public void setResolutionWidth(double resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
    }
    public void setResolutionHeight(double resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
    }

}