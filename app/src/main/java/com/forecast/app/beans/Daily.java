package com.forecast.app.beans;

import com.forecast.app.db.DailyForecast;

import java.util.ArrayList;

/**
 * @author Written by Mark Alvarez.
 */
public class Daily {
    private String summary;
    private ArrayList<DailyForecast> data;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ArrayList<DailyForecast> getData() {
        return data;
    }

    public void setData(ArrayList<DailyForecast> data) {
        this.data = data;
    }

}
