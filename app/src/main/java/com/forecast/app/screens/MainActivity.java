package com.forecast.app.screens;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.forecast.app.R;
import com.forecast.app.definitions.Definitions;
import com.forecast.app.db.DailyForecast;
import com.forecast.app.fragments.DialogFragment;
import com.forecast.app.interfaces.ForecastService;
import com.forecast.app.helpers.Utility;
import com.forecast.app.adapters.Forecast;
import com.forecast.app.beans.Wrapper;
import com.forecast.app.definitions.AppController;
import com.forecast.app.location.LocationServicesHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * @author Mark Alvarez.
 */
public class MainActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG = MainActivity.class.getName();
    @Bind(R.id.progressbar)
    protected ProgressBar mWaitBar;
    @Bind(R.id.forecastList)
    protected ListView mForecastList;
    private boolean mIsUpdating;
    private boolean mOrientationChange;
    private int mCurrentOrientation;
    private LocationRequest mLocRequest;
    private GoogleApiClient mGoogleApi;
    private Location mLocation1;
    private Forecast mAdapter;
    private ArrayList<DailyForecast> mForecastItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Google Play Services is availability
        if (!LocationServicesHelper.isGooglePlayServicesAvailable(this)) {
            Utility.showMessage(AppController.getApp().getString(R.string.play_services_not_available));
            finish();
            return;
        }

        // Set Layout content
        setContentView(R.layout.activity_main);

        // Inject dependencies
        ButterKnife.bind(this);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create location request
        createLocationRequest();

        // Build Google API
        mGoogleApi = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getMyLastKnownLocation();

        // Show if there was an existing download
        openLoadBar(mIsUpdating);

        // Setup ListView
        mForecastItems.addAll(Utility.getForecastList());
        mAdapter = new Forecast(this, mForecastItems);
        mForecastList.setAdapter(mAdapter);
        mForecastList.setEmptyView(findViewById(R.id.emptyList));
        setCurrentLocation();

        mOrientationChange = savedInstanceState != null && mCurrentOrientation !=
                getResources().getConfiguration().orientation;
        if (savedInstanceState == null || mOrientationChange) {
            mCurrentOrientation = getResources().getConfiguration().orientation;
        }
    }

    protected void setCurrentLocation() {
        if (mAdapter.getList().size() > 0) {
            setTitle(Utility.getLocation());
        } else {
            setTitle(getString(R.string.app_name));
        }
    }

    private void getMyLastKnownLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                mLocation1 = location;
            } else {
                mLocation1 = null;
            }

        }

    }

    private boolean verifyPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    private void getPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    Definitions.REQUEST_LOCATION_SERVICES);
        }

    }

    public void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean(Definitions.LOADING, mIsUpdating);
        bundle.putInt(Definitions.DEVICE_ORIENTATION, mCurrentOrientation);
        super.onSaveInstanceState(bundle);
    }

    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        mIsUpdating = bundle.getBoolean(Definitions.LOADING, false);
        mCurrentOrientation = bundle.getInt(Definitions.DEVICE_ORIENTATION, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            // Verify if location services is enabled
            if (!isLocationServicesEnabled()) return true;

            // Verify if there no an update in ongoing
            if(!mIsUpdating) {
                mOrientationChange = false;
                requestData();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openLoadBar(boolean open) {
        if (open) {
            mWaitBar.setVisibility(View.VISIBLE);
        } else {
            mWaitBar.setVisibility(View.GONE);
        }
    }

    private void requestData() {
        if (mLocation1 != null && !mIsUpdating && !mOrientationChange) {
            mIsUpdating = true;
            openLoadBar(mIsUpdating);
            updateRequest();
        }
    }

    /**
     * Request to the web service
     */
    private void updateRequest() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Definitions.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ForecastService forecastService = retrofit.create(ForecastService.class);
        Call<Wrapper> call = forecastService.getForecast("" + mLocation1.getLatitude(),
                "" + mLocation1.getLongitude());

        call.enqueue(new Callback<Wrapper>() {
            @Override
            public void onResponse(Response<Wrapper> response, Retrofit retrofit) {
                Wrapper wrapper = response.body();

                // Store today values
                SharedPreferences.Editor editor = AppController.getApp().
                        getSharedPreferences().edit();
                editor.putString(Definitions.TODAY_TIME_ZONE, wrapper.getTimezone());
                editor.putLong(Definitions.TODAY_TIME, wrapper.getCurrently().getTime());
                editor.putFloat(Definitions.TODAY_TEMPERATURE, wrapper.getCurrently().
                        getTemperature());
                editor.putString(Definitions.TODAY_SUMMARY, wrapper.getCurrently().getSummary());
                editor.commit();

                // Forecast expressed in week days
                ArrayList list = wrapper.getDaily().getData();

                // Update the ListView
                mAdapter.setList(list);

                // Set last known user location
                setCurrentLocation();

                // Store the other 8 days
                Utility.storeLocations(list);

                // Close progress bar
                openProgressBar(false);
            }

            @Override
            public void onFailure(Throwable t) {
                // Close progress bar
                openProgressBar(false);

                // Error message
                Utility.showMessage(AppController.getApp().getString(R.string.network_error));
            }

        });

    }

    private void openProgressBar(boolean open) {
        mIsUpdating = open;
        openLoadBar(open);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApi.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(LOG, "Closing Google api");
        if (mGoogleApi.isConnected()) {
            mGoogleApi.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isLocationServicesEnabled()) return;

        if (mGoogleApi.isConnected()) {
            requestData();
            Log.d(LOG, "Update");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Definitions.REQUEST_LOCATION_SERVICES: {
                if ((grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) || grantResults.length > 1
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates();
                } else {
                    Utility.showMessage(AppController.getApp().getString(R.string.enable_loc_services));
                    finish();
                }
                return;
            }
        }
    }

    protected boolean isLocationServicesEnabled() {
        boolean enabled = LocationServicesHelper.isLocationEnabled(this);
        if (!enabled && getSupportFragmentManager().findFragmentByTag("fragment_popup") == null) {
            DialogFragment dialogFragment = new DialogFragment();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), "fragment_popup");
        }
        return enabled;
    }

    protected void requestLocationUpdates() {
        if (verifyPermissions()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApi, mLocRequest, this);
        } else {
            getPermissions();
        }
        requestData();
    }
    protected void stopLocationUpdates() {
        if (mGoogleApi.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApi, this);
        }
    }

    protected void createLocationRequest() {
        mLocRequest = new LocationRequest();
        mLocRequest.setInterval(Definitions.TIME_REQUEST);
        mLocRequest.setFastestInterval(Definitions.TIME_FASTEST);
        mLocRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG, "Service running");
        if (LocationServicesHelper.isLocationEnabled(this)) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG, "Location change");
        mLocation1 = location;
        stopLocationUpdates();
        requestData();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

}
