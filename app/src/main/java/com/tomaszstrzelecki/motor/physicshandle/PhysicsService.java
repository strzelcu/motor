package com.tomaszstrzelecki.motor.physicshandle;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PhysicsService extends Service{

    final String TAG = "PhysicsService";

    private AccelerometerData accData; // TODO Remove this after make data alhorythm
    private GyroscopeData gyrData;//

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private SensorEventListener accelerometerListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            accData.addData(x,y,z); // TODO Remove this after make data alhorythm

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

            gyrData.addData(x,y,z);

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
        accData = new AccelerometerData(); // TODO Remove this after make data alhorythm
        gyrData = new GyroscopeData(); // TODO Remove this after make data alhorythm
        mSensorManager.registerListener(accelerometerListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(gyroscopeListener, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        Log.i(TAG, "PhysicsMonitor started");
    }

    public void stopPhysicsMonitor() {
        accData.saveData(); // TODO Remove this after make data alhorythm
        gyrData.saveData(); // TODO Remove this after make data alhorythm
        mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(gyroscopeListener);
        Log.i(TAG, "PhysicsMonitor stoped");
    }

    @Override
    public void onDestroy() {
        stopPhysicsMonitor();
        super.onDestroy();
    }
}
