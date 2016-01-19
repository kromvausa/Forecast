package com.forecast.app.definitions;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.forecast.app.db.DaoMaster;
import com.forecast.app.db.DaoSession;

/**
 * @author  Written by Mark Alvarez.
 */
public class AppController extends Application {
    private static final String DATABASE_NAME = "Database.db";
    private static final String FORECAST_SHARE_PREFERENCES = "_my_share_preferences_";
    private static AppController mAppContext;

    private SQLiteDatabase mDatabase;

    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    private SharedPreferences mSharedPreferences;

    public void onCreate() {
        super.onCreate();
        mSharedPreferences = getSharedPreferences(FORECAST_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        openSQLiteDatabase();
        mAppContext = this;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public static AppController getApp() {
        return mAppContext;
    }

    public SQLiteDatabase openSQLiteDatabase() throws SQLiteException {
        if (mDatabase == null) {
            mDatabase = new DaoMaster.DevOpenHelper(this, DATABASE_NAME,
                    null).getWritableDatabase();
        } else if (!mDatabase.isOpen()) {
            mDatabase = new DaoMaster.DevOpenHelper(this, DATABASE_NAME,
                    null).getWritableDatabase();
        }
        return mDatabase;
    }

    public DaoSession getDAOSession() {
        DaoMaster dm = getDAOMaster();
        if (mDaoSession == null) {
            mDaoSession = dm.newSession();
        }
        return mDaoSession;
    }

    public DaoMaster getDAOMaster() {
        SQLiteDatabase d = openSQLiteDatabase();

        if (mDaoMaster == null) {
            mDaoMaster = new DaoMaster(d);
        } else if (mDaoMaster.getDatabase() != d) {
            mDaoMaster = new DaoMaster(d);
        }

        return mDaoMaster;
    }

}
