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

import com.tomaszstrzelecki.motor.accidenthandle.Alarm;

import java.util.ArrayList;

public class PhysicsService extends Service{

    final String TAG = "PhysicsService";

    private AccelerometerData accData;
    private GyroscopeData gyrData;
    private SharedPreferences sharedPreferences;
    private boolean saveAccGyrData;
    private static boolean accHasRaisedAlarm = false;
    private static boolean gyrHasRaisedAlarm = false;
    private float acc_max = 25;
    private float acc_min = -25;
    private float gyr_max = 25;
    private float gyr_min = -25;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;


    private SensorEventListener accelerometerListener = new SensorEventListener() {

        private ArrayList<Float> xAxis = new ArrayList<Float>();
        private ArrayList<Float> yAxis = new ArrayList<Float>();
        private ArrayList<Float> zAxis = new ArrayList<Float>();

        private int timeWindow = 10; // Time window for data analysis

        private float averagexAxis;
        private float averageyAxis;
        private float averagezAxis;

        private float sumOfX;
        private float sumOfY;
        private float sumOfZ;

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if(saveAccGyrData) {
                accData.addData(x,y,z);
            }

            if(xAxis.size() < timeWindow) {
                xAxis.add(x);
                yAxis.add(y);
                zAxis.add(z);
            } else {
                xAxis.remove(0);
                xAxis.add(x);
                yAxis.remove(0);
                yAxis.add(y);
                zAxis.remove(0);
                zAxis.add(z);

                sumOfX = 0;
                sumOfY = 0;
                sumOfZ = 0;

                for(int i = 0; i < xAxis.size(); i++) {
                    sumOfX += xAxis.get(i);
                    sumOfY += yAxis.get(i);
                    sumOfZ += zAxis.get(i);
                }

                averagexAxis = sumOfX / timeWindow;
                //Log.i(TAG, "Accelerometer AverageXAxis: " + averagexAxis + " MAX = " + acc_max + " MIN = " + acc_min);
                averageyAxis = sumOfY / timeWindow;
                //Log.i(TAG, "Accelerometer AverageYAxis: " + averageyAxis + " MAX = " + acc_max + " MIN = " + acc_min);
                averagezAxis = sumOfZ / timeWindow;
                //Log.i(TAG, "Accelerometer AverageZAxis: " + averagezAxis + " MAX = " + acc_max + " MIN = " + acc_min);

                if(!accHasRaisedAlarm) {
                    if(averagexAxis > acc_max || averagexAxis < acc_min){
                        Alarm.raiseAlarm();
                        accHasRaisedAlarm = true;
                    } else if(averageyAxis > acc_max || averageyAxis < acc_min){
                        Alarm.raiseAlarm();
                        accHasRaisedAlarm = true;
                    } else if(averagezAxis > acc_max || averagezAxis < acc_min){
                        Alarm.raiseAlarm();
                        accHasRaisedAlarm = true;
                    }
                }
            }

            // Log.i(TAG, "Accelerometer - X-axis: " + x + " Y-axis: " + y + " Z-axis: " + z);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener gyroscopeListener = new SensorEventListener() {

        private ArrayList<Float> xAxis = new ArrayList<Float>();
        private ArrayList<Float> yAxis = new ArrayList<Float>();
        private ArrayList<Float> zAxis = new ArrayList<Float>();

        private int timeWindow = 10; // Time window for data analysis

        private float averagexAxis;
        private float averageyAxis;
        private float averagezAxis;

        private float sumOfX;
        private float sumOfY;
        private float sumOfZ;

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if(saveAccGyrData) {
                gyrData.addData(x,y,z);
            }

            if(xAxis.size() < timeWindow) {
                xAxis.add(x);
                yAxis.add(y);
                zAxis.add(z);
            } else {
                xAxis.remove(0);
                xAxis.add(x);
                yAxis.remove(0);
                yAxis.add(y);
                zAxis.remove(0);
                zAxis.add(z);

                sumOfX = 0;
                sumOfY = 0;
                sumOfZ = 0;

                for(int i = 0; i < xAxis.size(); i++) {
                    sumOfX += xAxis.get(i);
                    sumOfY += yAxis.get(i);
                    sumOfZ += zAxis.get(i);
                }

                averagexAxis = sumOfX / timeWindow;
                //Log.i(TAG, "Gyroscope AverageXAxis: " + averagexAxis + " MAX = " + gyr_max + " MIN = " + gyr_min);
                averageyAxis = sumOfY / timeWindow;
                //Log.i(TAG, "Gyroscope AverageYAxis: " + averageyAxis + " MAX = " + gyr_max + " MIN = " + gyr_min);
                averagezAxis = sumOfZ / timeWindow;
                //Log.i(TAG, "Gyroscope AverageZAxis: " + averagezAxis + " MAX = " + gyr_max + " MIN = " + gyr_min);

                if(!gyrHasRaisedAlarm) {
                    if(averagexAxis > gyr_max || averagexAxis < gyr_min){
                        Alarm.raiseAlarm();
                        gyrHasRaisedAlarm = true;
                    } else if(averageyAxis > gyr_max || averageyAxis < gyr_min){
                        Alarm.raiseAlarm();
                        gyrHasRaisedAlarm = true;
                    } else if(averagezAxis > gyr_max || averagezAxis < gyr_min){
                        Alarm.raiseAlarm();
                        gyrHasRaisedAlarm = true;
                    }
                }
            }

            // Log.i(TAG, "Gyroscope - X-axis: " + x + " Y-axis: " + y + " Z-axis: " + z);
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
        saveAccGyrData = sharedPreferences.getBoolean("physics_save_acc_gyr", saveAccGyrData);
        setAccGyrSensitivity();
        if (saveAccGyrData) {
            accData = new AccelerometerData();
            gyrData = new GyroscopeData();
        }
        mSensorManager.registerListener(accelerometerListener, mAccelerometer, getPhysicsDelay());
        mSensorManager.registerListener(gyroscopeListener, mGyroscope, getPhysicsDelay());
        Log.i(TAG, "PhysicsMonitor started");

    }

    public void stopPhysicsMonitor() {
        if (saveAccGyrData) {
            accData.saveData();
            gyrData.saveData();
            resetAlarm();
        }

        mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(gyroscopeListener);
        Log.i(TAG, "PhysicsMonitor stoped");

    }

    public void setAccGyrSensitivity(){
        acc_max = Float.valueOf(sharedPreferences.getString("acc_sensitivity", ""));
        acc_min = -Float.valueOf(sharedPreferences.getString("acc_sensitivity", ""));
        gyr_max = Float.valueOf(sharedPreferences.getString("gyr_sensitivity", ""));
        gyr_min = -Float.valueOf(sharedPreferences.getString("gyr_sensitivity", ""));
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

    public static void resetAlarm() {
        accHasRaisedAlarm = false;
        accHasRaisedAlarm = false;
    }

    @Override
    public void onDestroy() {
        stopPhysicsMonitor();
        super.onDestroy();
    }
}