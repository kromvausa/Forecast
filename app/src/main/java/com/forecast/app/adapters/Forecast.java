package com.forecast.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.forecast.app.R;
import com.forecast.app.definitions.Definitions;
import com.forecast.app.db.DailyForecast;
import com.forecast.app.helpers.Utility;
import com.forecast.app.definitions.AppController;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Written by Mark Alvarez.
 */
public class Forecast extends ArrayAdapter<DailyForecast> {
    private static final int TODAY = 0;
    private static final int FORECAST_DAYS = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    private ViewHolder mHolder;
    private DailyForecast mForecast;
    private LayoutInflater mLayoutInflater;
    private ArrayList<DailyForecast> mForecasts;
    private Calendar mCalendar = Calendar.getInstance();
    private DecimalFormat mFormat = new DecimalFormat("##.##");

    public Forecast(Context context, ArrayList<DailyForecast> list) {
        super(context, 0, list);
        mForecasts = list;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public int getItemViewType(int pos) {
        return pos == 0 ? TODAY : FORECAST_DAYS;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null || !(view.getTag() instanceof ViewHolder)) {
            view = buildView(position, parent);
            mHolder = new ViewHolder(view);
            view.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) view.getTag();
        }

        mForecast = mForecasts.get(position);

        mCalendar.setTimeInMillis(1000L * mForecast.getTime());

        float temperature1;
        float temperature2;

        if (position > 0) {
            mHolder.day.setText(Utility.getDay(mCalendar.get(Calendar.DAY_OF_WEEK)));
        } else {
            temperature1 = Utility.temperatureConverterF2C(AppController.getApp().getSharedPreferences()
                    .getFloat(Definitions.TODAY_TEMPERATURE, 0));
            String temperature = String.format(AppController.getApp().getString(R.string.temperature_grades), mFormat.format(temperature1));
            mHolder.day.setText(temperature);
        }

        mHolder.summary.setText(mForecast.getSummary());
        temperature1 = Utility.temperatureConverterF2C(mForecast.getTemperatureMax());
        temperature2 = Utility.temperatureConverterF2C(mForecast.getTemperatureMin());
        String range = String.format(AppController.getApp().getString(R.string.temperature_interval), mFormat.format(temperature1),
                mFormat.format(temperature2));
        mHolder.temperatureRange.setText(range);

        return view;
    }

    private View buildView(int position, ViewGroup parent) {
        View view;

        switch(position) {
            case 0:
                view = mLayoutInflater.inflate(R.layout.today_layout, parent, false);
                break;
            default:
                view = mLayoutInflater.inflate(R.layout.week_days_layout, parent, false);
                break;
        }

        return view;
    }

    public ArrayList<DailyForecast> getList() {
        return mForecasts;
    }

    public void setList(ArrayList<DailyForecast> list) {
        this.mForecasts.clear();
        this.mForecasts.addAll(list);
        this.notifyDataSetChanged();
    }

    static class ViewHolder {
        @Bind(R.id.day) TextView day;
        @Bind(R.id.summary) TextView summary;
        @Bind(R.id.temperature_range) TextView temperatureRange;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
