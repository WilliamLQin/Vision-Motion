package com.williamqin.visionmotion;

/**
 * Created by William on 11/7/2017.
 */

public class MetaData {

    private String date;
    private String name;
    private int size;
    private long timestamp;

    public MetaData()
    {

    }

    public MetaData(String date, String name, int size, long timestamp)
    {
        this.date = date;
        this.name = name;
        this.size = size;
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public int getSize()
    {
        return size;
    }
    public void setSize(int size)
    {
        this.size = size;
    }

    public long getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

}
