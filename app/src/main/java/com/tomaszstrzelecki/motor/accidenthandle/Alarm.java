package com.tomaszstrzelecki.motor.accidenthandle;

import android.app.Application;
import android.util.Log;

import com.tomaszstrzelecki.motor.AppService;

public class Alarm extends Application {

    private static Alarm alarm;
    private Thread alarmCheckThread;
    private static final String TAG = "Alarm";
    private int timeCounter = 0;

    private static final int GREEN = 0;
    private static final int BLUE = 1;
    private static final int YELLOW = 2;
    private static final int RED = 3;
    private static final int BLACK = 4;
    private static int alarmStatus = Alarm.GREEN;

    private Alarm() { }

    public static Alarm getInstance() {
        if (alarm == null) {
            alarm = new Alarm();
        }
        return alarm;
    }

    public static void raiseAlarm() {
        if(alarmStatus < 4) {
            alarmStatus++;
        }
    }

    public static void reduceAlarm() {
        if(alarmStatus > 0) {
            alarmStatus--;
        }
    }

    public void startAlarmCheckThread() {
        alarmCheckThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        Log.i(TAG, "Current alarm is " + getStringAlarmStatus());
                        if(alarmStatus == Alarm.RED) {
                            timeCounter++;
                            if(timeCounter > 30) { // seconds TODO po jakim czasie włączyć alarm?
                                raiseAlarm();
                            }
                        }
                        if(alarmStatus == Alarm.BLACK) {
                            turnOnAlarm();
                        }
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        Log.i(TAG, "Alarm check thread started");
        alarmCheckThread.start();
    }

    public void stopAlarmCheckThread() {
        if(alarmCheckThread != null) {
            alarmCheckThread.interrupt();
        }
        Log.i(TAG, "Alarm check thread stoped");
    }

    private void turnOnAlarm() {
        AppService.alarmIsOn = true;
    }

    private String getStringAlarmStatus() {
        switch(alarmStatus){
            case Alarm.GREEN: {
                return "GREEN";
            }

            case Alarm.BLUE: {
                return "BLUE";
            }

            case Alarm.YELLOW: {
                return "YELLOW";
            }

            case Alarm.RED: {
                return "RED";
            }

            case Alarm.BLACK: {
                return "BLACK";
            }
        }
        return String.valueOf(alarmStatus);
    }

    public void resetAlarm() {
        alarmStatus = Alarm.GREEN;
        timeCounter = 0;
    }
}