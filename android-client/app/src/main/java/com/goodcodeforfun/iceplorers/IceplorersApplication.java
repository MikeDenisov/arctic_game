package com.goodcodeforfun.iceplorers;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.yayandroid.locationmanager.LocationManager;

/**
 * Created by snigavig on 29.04.17.
 */

public class IceplorersApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        LocationManager.enableLog(true);
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}