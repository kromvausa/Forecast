package com.forecast.app.beans;

/**
 * @author Written by Mark Alvarez.
 */
public class Wrapper {
    private String timezone;
    private Today currently;
    private Daily daily;

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Today getCurrently() {
        return currently;
    }

    public void setCurrently(Today currently) {
        this.currently = currently;
    }

    public Daily getDaily() {
        return daily;
    }

    public void setDaily(Daily daily) {
        this.daily = daily;
    }

}
