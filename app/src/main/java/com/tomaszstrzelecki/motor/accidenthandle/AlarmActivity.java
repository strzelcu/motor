package com.tomaszstrzelecki.motor.accidenthandle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.tomaszstrzelecki.motor.R;
import com.tomaszstrzelecki.motor.phonehandle.SmsProvider;
import com.tomaszstrzelecki.motor.physicshandle.PhysicsService;
import com.tomaszstrzelecki.motor.util.Notifications;

import static com.tomaszstrzelecki.motor.AppService.alarmActivityRunning;

public class AlarmActivity extends Activity {

    private Alarm alarm;
    private Button alarmButton;
    private Thread alarmThread;
    private Thread vibratorThread;
    private static String TAG = "AlarmActivity";
    private int timePeriod = 30;
    private int timeCounter = timePeriod;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        final Window win= getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        alarm = Alarm.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        timePeriod = Integer.valueOf(sharedPreferences.getString("physics_message_delay", "30"));
        timeCounter = timePeriod;

        alarmButton = (Button) findViewById(R.id.alarmButton);
        alarmButton.setText(String.valueOf(timeCounter));

        alarmButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        alarmButton.setBackgroundResource(R.drawable.monitor_button_red_tint);
                        return false;

                    case MotionEvent.ACTION_UP:
                        alarmButton.setBackgroundResource(R.drawable.monitor_button_red);
                        return false;
                }
                return false;
            }
        });

        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAlarmThread();
        startVibratorThread();
    }

    @Override
    protected void onDestroy() {
        alarmActivityRunning = false;
        alarm.resetAlarm();
        stopAlarmThread();
        stopVibratorThread();
        PhysicsService.resetAlarm();
        super.onDestroy();
    }

    private void startAlarmThread() {
        alarmThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted() || timeCounter == 0) {
                        Thread.sleep(1000);
                        timeCounter--;
                        setAlarmButtonSeconds(timeCounter);
                        if(timeCounter == 0){ // TODO ZMIENić na 0
                            timeCounter = timePeriod;
                            setAlarmButtonSeconds(timeCounter);
                            startAlarmThread();
                            sendAlarmMessage();
                            this.interrupt();
                        }
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        Log.i(TAG, "Alarm thread started");
        alarmThread.start();
    }

    private  void stopAlarmThread() {
        alarmThread.interrupt();
        Log.i(TAG, "Alarm thread stoped");
    }

    private void startVibratorThread() {
        vibratorThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted() || timeCounter == 0) {
                        Thread.sleep(400);
                        Notifications.vibrate(200, getApplicationContext());
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        Log.i(TAG, "Vibrator thread started");
        vibratorThread.start();
    }

    private  void stopVibratorThread() {
        vibratorThread.interrupt();
        Log.i(TAG, "Vibrator thread stoped");
    }

    private void setAlarmButtonSeconds(int seconds) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alarmButton.setText(String.valueOf(timeCounter));
            }
        });
    }

    private void sendAlarmMessage() {
        SmsProvider.sendAlarmMessage(getApplication().getApplicationContext());
    }
}