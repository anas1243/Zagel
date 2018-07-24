package com.example.anas.zagel.Models;

import java.io.Serializable;

/**
 * Created by Toka on 2018-03-22.
 */

public class Birthday implements Serializable {
    private int day;
    private int month;
    private int year;

    public Birthday(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public Birthday() {

    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
