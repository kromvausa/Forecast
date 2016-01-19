package com.forecast.app.beans;

/**
 * @author Written by Mark Alvarez.
 */
public class Today {
    private long time;
    private String summary;
    private float temperature;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }
}
