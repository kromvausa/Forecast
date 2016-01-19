package com.forecast.app.interfaces;

import com.forecast.app.beans.Wrapper;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * @author  Written by Mark Alvarez.
 */
public interface ForecastService {

    /**
     * Method to get the current forecast info
     * @param latitude The last known latitude of user's location
     * @param longitude The last known longitude of user's location
     */
    @GET("/forecast/93beaba8c87097f0a50aa9f92d4a052f/{lat},{lon}")
    Call<Wrapper> getForecast(@Path("lat")String latitude, @Path("lon")String longitude);

}
