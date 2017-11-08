package com.thacks2.motionsensor;

/**
 * Created by William on 11/7/2017.
 */

public class User {

    private int dataCount;
    private String name;
    private String email;

    public User()
    {

    }

    public User(String name, String email)
    {
        this.dataCount = 0;
        this.name = name;
        this.email = email;
    }

    public int getDataCount()
    {
        return dataCount;
    }

    public void incrementDataCount()
    {
        dataCount ++;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }

}
