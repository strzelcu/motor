package com.tomaszstrzelecki.motor.physicshandle;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import static android.R.attr.delay;

public class PhysicsService extends Service{

    final String TAG = "PhysicsService";

    private AccelerometerData accData;
    private GyroscopeData gyrData;
    private SharedPreferences sharedPreferences;
    private boolean saveAccGyrData;
    private boolean physicsIsOn;


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;


    private SensorEventListener accelerometerListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if(saveAccGyrData) {
                accData.addData(x,y,z);
            }

            Log.i(TAG, "Accelerometer - X-axis: " + x + " Y-axis: " + y + " Z-axis: " + z);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener gyroscopeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if(saveAccGyrData) {
                gyrData.addData(x,y,z);
            }

            Log.i(TAG, "Gyroscope - X-axis: " + x + " Y-axis: " + y + " Z-axis: " + z);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public class LocalBinder extends Binder {
        public PhysicsService getService() {
            // Init methods
            return PhysicsService.this;
        }
    }

    public void startPhysicsMonitor() {
        physicsIsOn = sharedPreferences.getBoolean("physics_general", physicsIsOn);
        if(physicsIsOn) {
            saveAccGyrData = sharedPreferences.getBoolean("physics_save_acc_gyr", saveAccGyrData);
            if (saveAccGyrData) {
                accData = new AccelerometerData();
                gyrData = new GyroscopeData();
            }
            mSensorManager.registerListener(accelerometerListener, mAccelerometer, getPhysicsDelay());
            mSensorManager.registerListener(gyroscopeListener, mGyroscope, getPhysicsDelay());
            Log.i(TAG, "PhysicsMonitor started");
        }
    }

    public void stopPhysicsMonitor() {
        if(physicsIsOn){
            if (saveAccGyrData) {
                accData.saveData();
                gyrData.saveData();
            }

            mSensorManager.unregisterListener(accelerometerListener);
            mSensorManager.unregisterListener(gyroscopeListener);
            Log.i(TAG, "PhysicsMonitor stoped");
        }
    }

    public int getPhysicsDelay() {
        String delay = "";
        delay = sharedPreferences.getString("physics_delay", delay);
        switch (Integer.valueOf(delay)) {
            case 0: {
                return SensorManager.SENSOR_DELAY_NORMAL;
            }
            case 1: {
                return SensorManager.SENSOR_DELAY_GAME;
            }
            case 2: {
                return SensorManager.SENSOR_DELAY_FASTEST;
            }
            default:{
                return SensorManager.SENSOR_DELAY_NORMAL;
            }
        }
    }

    @Override
    public void onDestroy() {
        stopPhysicsMonitor();
        super.onDestroy();
    }
}