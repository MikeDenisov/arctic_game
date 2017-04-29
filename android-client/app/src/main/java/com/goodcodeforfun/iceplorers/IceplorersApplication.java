package com.goodcodeforfun.iceplorers;

import android.app.Application;

import com.yayandroid.locationmanager.LocationManager;

/**
 * Created by snigavig on 29.04.17.
 */

public class IceplorersApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LocationManager.enableLog(true);
    }
}