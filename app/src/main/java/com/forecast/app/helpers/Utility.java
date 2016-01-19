package com.forecast.app.helpers;

import android.widget.Toast;

import com.forecast.app.definitions.Definitions;
import com.forecast.app.definitions.AppController;
import com.forecast.app.db.DailyForecast;
import com.forecast.app.db.DailyForecastDao;

import java.util.Calendar;
import java.util.List;

/**
 * @author Written by Mark Alvarez.
 */
public class Utility {

    /**
     * General utility function to print popup messages
     * @param message Message to be printed on the screen
     */
    public static void showMessage(String message) {
        Toast.makeText(AppController.getApp(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Get the available forecast list
     * @return returns the last list of forecast stored in the data base
     */
    public static List<DailyForecast> getForecastList() {
        return AppController.getApp().getDAOSession().getDailyForecastDao().loadAll();
    }

    /**
     * Store forecast list in the SQLite data base
     * @param dailyForecast Temperature of the next 8 days, including today
     */
    public static void storeLocations(List<DailyForecast> dailyForecast) {
        DailyForecastDao dao = AppController.getApp().getDAOSession().getDailyForecastDao();
        dao.deleteAll();
        dao.insertInTx(dailyForecast);
    }

    /**
     * Get the corresponding day in String format of a given Calendar day
     * @param dayNum Given day
     */
    public static String getDay(int dayNum) {
        String day = "";
        switch(dayNum) {
            case Calendar.SUNDAY:
                day = "Sunday";
                break;
            case Calendar.MONDAY:
                day = "Monday";
                break;
            case Calendar.TUESDAY:
                day = "Tuesday";
                break;
            case Calendar.WEDNESDAY:
                day = "Wednesday";
                break;
            case Calendar.THURSDAY:
                day = "Thursday";
                break;
            case Calendar.FRIDAY:
                day = "Friday";
                break;
            case Calendar.SATURDAY:
                day = "Saturday";
                break;
        }
        return day;
    }

    /**
     * Parse the user's region where the user is located
     * @return The current location if it was parsed successfully
     */
    public static String getLocation() {
        String location = AppController.getApp().getSharedPreferences().
                getString(Definitions.TODAY_TIME_ZONE, "/");
        String[] arr;
        if (location.contains("/")) {
            arr = location.split("/");
            if (arr.length > 1) {
                location = arr[1].replace("_", " ");
            }
        }

        return location;

    }

    /**
     * Converts the temperature from Fahrenheit to Celsius
     * @param fahrenheit Temperature in Fahrenheit degrees
     */
    public static float temperatureConverterF2C(float fahrenheit) {
        return (1.0f * 5 / 9) * (fahrenheit - 32);
    }

}
