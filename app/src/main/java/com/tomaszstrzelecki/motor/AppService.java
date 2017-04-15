package com.tomaszstrzelecki.motor;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tomaszstrzelecki.motor.dbhelper.DatabaseHelper;
import com.tomaszstrzelecki.motor.dbhelper.DatabaseProvider;
import com.tomaszstrzelecki.motor.gpshandle.GpsInterface;
import com.tomaszstrzelecki.motor.gpshandle.GpsService;
import com.tomaszstrzelecki.motor.util.Notifications;

public class AppService extends Service implements GpsInterface {

    public static boolean isMonitorOn = false;
    public static boolean isNetworkOn = false;

    // Declarations
    private final IBinder mBinder = new AppService.LocalBinder();
    ConnectivityManager cm;
    Thread systemCheckThread;

    // Service binder methods

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    public class LocalBinder extends Binder {
        AppService getService() {
            return AppService.this;
        }
    }

    //GPS Service
    protected boolean isServiceGPSConnect = false;
    Intent gpsServiceIntent;
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

    // Public methods controling workflow

    public void startMonitoring() {
        isMonitorOn = true;
        startGPS();
        Log.e("System", "Monitor started");
    }

    public void stopMonitoring() {
        isMonitorOn = false;
        stopGPS();
        Log.e("System", "Monitor stopped");
    }

    // Service lifecycle

    @Override
    public void onCreate() {
        Log.e("System", "AppService is created");
        gpsServiceIntent = new Intent(this, GpsService.class);
        bindService(gpsServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        startSystemCheckThread();
        startService(gpsServiceIntent);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.e("System", "AppService is destroyed");
        stopService(gpsServiceIntent);
        stopSystemCheckThread();
        super.onDestroy();
    }

    // GpsService control

    @Override
    public void startGPS() {
        gpsService.startGPS();
    }

    @Override
    public void stopGPS() {
        gpsService.stopGPS();
    }

    @Override
    public Double getLatitude() {
        return gpsService.getLatitude();
    }

    @Override
    public Double getLongitude() {
        return gpsService.getLongitude();
    }

    @Override
    public String getSpeed() {
        return gpsService.getSpeed();
    }

    @Override
    public String getSatellitesInView() {
        return gpsService.getSatellitesInView();
    }

    @Override
    public String getSatellitesInUse() {
        return gpsService.getSatellitesInUse();
    }

    // SystemCheckThread

    private void startSystemCheckThread() {
        systemCheckThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(2000);
                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        isNetworkOn = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                        }
                } catch (InterruptedException ignored) {
                }
            }
        };
        Log.e("System", "System check thread starts");
        systemCheckThread.start();
    }

    private  void stopSystemCheckThread() {
        systemCheckThread.interrupt();
        Log.e("System", "System check thread stops");
    }

}