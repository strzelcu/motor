package com.tomaszstrzelecki.motor;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tomaszstrzelecki.motor.accidenthandle.Alarm;
import com.tomaszstrzelecki.motor.accidenthandle.AlarmActivity;
import com.tomaszstrzelecki.motor.physicshandle.PhysicsService;
import com.tomaszstrzelecki.motor.gpshandle.GpsInterface;
import com.tomaszstrzelecki.motor.gpshandle.GpsService;

public class AppService extends Service implements GpsInterface {

    public static boolean isMonitorOn = false;
    public static boolean networkIsOn = false;
    public static boolean alarmIsOn = false;
    public static boolean alarmActivityRunning = false;

    // Declarations
    private final IBinder mBinder = new AppService.LocalBinder();
    private ConnectivityManager cm;
    private SharedPreferences sharedPreferences;
    private boolean physicsIsOn = false;
    private Thread systemCheckThread;
    Alarm alarm = Alarm.getInstance();

    // Service binder methods

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    class LocalBinder extends Binder {
        AppService getService() {
            return AppService.this;
        }
    }

    //GPS Service
    protected boolean isServiceGPSConnect = false;
    Intent gpsServiceIntent;
    GpsService gpsService;
    private ServiceConnection gpsConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            GpsService.LocalBinder binder = (GpsService.LocalBinder) service;
            gpsService = binder.getService();
            isServiceGPSConnect = true;
            Log.i("AppService", "GpsService is binded to AppService");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceGPSConnect = false;
            Log.i("AppService", "GpsService is unbinded from AppService");
        }
    };

    //Physics Service
    protected boolean isServicePhysicsConnect = false;
    Intent physicsServiceIntent;
    PhysicsService physicsService;
    private ServiceConnection physicsConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PhysicsService.LocalBinder binder = (PhysicsService.LocalBinder) service;
            physicsService = binder.getService();
            isServicePhysicsConnect = true;
            Log.i("AppService", "PhysicsService is binded to AppService");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServicePhysicsConnect = false;
            Log.i("AppService", "PhysicsService is unbinded from AppService");
        }
    };

    // Public methods controling workflow

    public void startMonitoring() {
        isMonitorOn = true;
        physicsIsOn = sharedPreferences.getBoolean("physics_general", physicsIsOn);
        startGPS();
        if (physicsIsOn) {
            startPhysicsMonitor();
            startSystemCheckThread();
            alarm.startAlarmCheckThread();
        }
        Log.i("AppService", "Monitor started");
    }

    public void stopMonitoring() {
        isMonitorOn = false;
        stopGPS();
        if(physicsIsOn){
            stopPhysicsMonitor();
            stopSystemCheckThread();
            alarm.stopAlarmCheckThread();
            alarm.resetAlarm();
            PhysicsService.resetAlarm();
            alarmIsOn = false;
        }
        Log.i("AppService", "Monitor stopped");
    }

    // Service lifecycle

    @Override
    public void onCreate() {
        Log.i("AppService", "AppService is created");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        gpsServiceIntent = new Intent(this, GpsService.class);
        bindService(gpsServiceIntent, gpsConnection, Context.BIND_AUTO_CREATE);

        physicsServiceIntent = new Intent(this, PhysicsService.class);
        bindService(physicsServiceIntent, physicsConnection, Context.BIND_AUTO_CREATE);

        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        startService(gpsServiceIntent);
        startService(physicsServiceIntent);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("AppService", "AppService is destroyed");
        alarm.stopAlarmCheckThread();
        gpsService.onDestroy();
        physicsService.onDestroy();
        stopService(gpsServiceIntent);
        stopService(physicsServiceIntent);
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

    public void startPhysicsMonitor() {
        physicsService.startPhysicsMonitor();
    }

    public void stopPhysicsMonitor() {
        physicsService.stopPhysicsMonitor();
    }

    // SystemCheckThread

    private void startSystemCheckThread() {
        systemCheckThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        networkIsOn = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                        if(alarmIsOn){
                            if(!alarmActivityRunning) {
                                alarmActivityRunning = true;
                                runAlarmActivity();
                            } else {
                                alarmIsOn = false;
                            }
                        }
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        Log.i("AppService", "System check thread started");
        systemCheckThread.start();
    }

    private  void stopSystemCheckThread() {
        systemCheckThread.interrupt();
        Log.i("AppService", "System check thread stoped");
    }

    private void runAlarmActivity() {
        Intent i = new Intent(this.getApplication(), AlarmActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.getApplication().startActivity(i);
    }
}