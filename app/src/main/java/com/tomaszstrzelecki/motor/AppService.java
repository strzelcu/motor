package com.tomaszstrzelecki.motor;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tomaszstrzelecki.motor.interfaces.GpsInterface;

public class AppService extends Service implements GpsInterface {

    // Declarations
    private final IBinder mBinder = new AppService.LocalBinder();


    // Service binder methods

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    public class LocalBinder extends Binder {
        AppService getService() {
            return AppService.this;
        }
    }

    //GPS Service
    GpsService gpsService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            GpsService.LocalBinder binder = (GpsService.LocalBinder) service;
            gpsService = binder.getService();
            isServiceGPSConnect = true;
            Log.e("System", "GpsService is binded to AppService");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceGPSConnect = false;
            Log.e("System", "GpsService is unbinded from AppService");
        }
    };
    protected boolean isServiceGPSConnect = false;
    Intent gpsServiceIntent;

    // Service lifecycle

    @Override
    public void onCreate() {
        Log.e("System", "AppService is created");
        gpsServiceIntent = new Intent(this, GpsService.class);
        bindService(gpsServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(gpsServiceIntent);
        gpsService.startGPS();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        gpsService.stopGPS();
        stopService(gpsServiceIntent);
        super.onDestroy();
    }

    // GpsService control

    @Override
    public void startGPS() {

    }

    @Override
    public void stopGPS() {

    }

    @Override
    public String getLatitude() {
        return "";
    }

    @Override
    public String getLongitude() {
        return "";
    }

    @Override
    public String getSpeed() {
        return "";
    }

    @Override
    public String getSatellitesInView() {
        return "";
    }

    @Override
    public String getSatellitesInUse() {
        return "";
    }

    @Override
    public String getStreet() {
        return "";
    }

    @Override
    public String getCity() {
        return "";
    }

    @Override
    public String getPincode() {
        return "";
    }
}
