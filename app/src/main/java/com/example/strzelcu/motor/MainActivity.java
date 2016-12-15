package com.example.strzelcu.motor;

import android.*;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Instance state
    static final String STATE_MONITOR = "false";
    static final String STATE_LATITUDE = "LATITUDE";
    static final String STATE_LONGITUDE = "LONGITUDE";
    static final String STATE_SPEED = "SPEED";
    static final String STATE_CITY = "CITY";
    static final String STATE_PINCODE = "PINCODE";
    static final String STATE_STREET = "STREET";
    static final String STATE_MONITORING_TEXT = "MONITORING";
    static final String STATE_MONITORING_COLOR = "0";

    // Permissions static variable
    static final int PERMISSION_LOCATION_CODE = 1;

    // Declarations

    boolean isMonitorOn = false;
    Notification note = new Notification();
    Thread UIThread;

    //GPS Service
    GpsService gpsService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GpsService.LocalBinder binder = (GpsService.LocalBinder) service;
            gpsService = binder.getService();
            isServiceGPSConnect = true;
            Log.e("System", "GpsService is binded");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceGPSConnect = false;
            Log.e("System", "GpsService is unbinded");
        }
    };
    protected boolean isServiceGPSConnect = false;
    Intent gpsServiceIntent;

    //Interface
    protected TextView latitudeText;
    protected TextView longitudeText;
    protected TextView satellites;
    protected TextView speedText;
    protected TextView cityText;
    protected TextView pincodeText;
    protected TextView streetText;
    protected Button monitoring;

    // Activity life cycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        Log.e("System", "Main activity created");

        //Graphics objects
        monitoring = (Button) findViewById(R.id.mainButton);
        latitudeText = (TextView) findViewById(R.id.latitudeValue);
        longitudeText = (TextView) findViewById(R.id.longitudeValue);
        satellites = (TextView) findViewById(R.id.satellitesValue);
        speedText = (TextView) findViewById(R.id.speedValue);
        cityText = (TextView) findViewById(R.id.cityValue);
        pincodeText = (TextView) findViewById(R.id.pincodeValue);
        streetText = (TextView) findViewById(R.id.streetValue);

        monitoring.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isMonitorOn) {
                            monitoring.setBackgroundResource(R.drawable.monitor_button_green_tint);
                        } else {
                            monitoring.setBackgroundResource(R.drawable.monitor_button_yellow_tint);
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                        if (!isMonitorOn) {
                            monitoring.setBackgroundResource(R.drawable.monitor_button_green);
                        } else {
                            monitoring.setBackgroundResource(R.drawable.monitor_button_yellow);
                        }
                        return false;
                }
                return false;
            }
        });

        monitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMonitorOn) {
                    if (isGPSEnabled(MainActivity.this)) {
                        isMonitorOn = true;
                        monitoring.setBackgroundResource(R.drawable.monitor_button_yellow);
                        Log.e("System", "Monitor started");
                        note.showNotificationMonitoring(getApplicationContext());
                        gpsService.startGPS();
                        updateUI();
                    } else {
                        showMsg("Lokalizacja wyłączona.");
                    }

                } else {
                    monitoring.setBackgroundResource(R.drawable.monitor_button_green);
                    Log.e("System", "Monitor stopped");
                    gpsService.stopGPS();
                    note.hideNotification();
                    isMonitorOn = false;
                }
            }
        });

    }

    @Override
    public void onStart() {
        gpsServiceIntent = new Intent(this, GpsService.class);
        bindService(gpsServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(gpsServiceIntent);
        startUIThread();
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.e("System", "Main activity resumed");
        checkPermissions();
        locationAlert();
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.e("System", "Main activity stopped");
        if (isServiceGPSConnect) {
            unbindService(mConnection);
            isServiceGPSConnect = false;
        }
        stopUIThread();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.e("System", "Main activity destroyed");
        super.onDestroy();
    }

    // Saving activity state

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isMonitorOn = savedInstanceState.getBoolean(STATE_MONITOR);
            cityText.setText(savedInstanceState.getCharSequence(STATE_CITY));
            latitudeText.setText(savedInstanceState.getCharSequence(STATE_LATITUDE));
            longitudeText.setText(savedInstanceState.getCharSequence(STATE_LONGITUDE));
            pincodeText.setText(savedInstanceState.getCharSequence(STATE_PINCODE));
            speedText.setText(savedInstanceState.getCharSequence(STATE_SPEED));
            streetText.setText(savedInstanceState.getCharSequence(STATE_STREET));
            monitoring.setText(savedInstanceState.getCharSequence(STATE_MONITORING_TEXT));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_MONITOR, isMonitorOn);
        savedInstanceState.putInt(STATE_MONITORING_COLOR, monitoring.getBackground().hashCode());
        savedInstanceState.putString(STATE_CITY, (String) cityText.getText());
        savedInstanceState.putString(STATE_LATITUDE, (String) latitudeText.getText());
        savedInstanceState.putString(STATE_LONGITUDE, (String) longitudeText.getText());
        savedInstanceState.putString(STATE_PINCODE, (String) pincodeText.getText());
        savedInstanceState.putString(STATE_SPEED, (String) speedText.getText());
        savedInstanceState.putString(STATE_STREET, (String) streetText.getText());
        savedInstanceState.putString(STATE_MONITORING_TEXT, (String) this.monitoring.getText());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent i;

        switch (item.getItemId()) {

            case R.id.action_routes:
                i = new Intent(this, RoutesActivity.class);
                this.startActivity(i);
                return true;

            case R.id.action_settings:
                i = new Intent(this, SettingsActivity.class);
                this.startActivity(i);
                return true;

            case R.id.action_exit:
                stopService(gpsServiceIntent);
                super.finish();
                Process.killProcess(Process.myPid());
                return true;
            case R.id.action_gps:
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /* %%%%%%%%%%%%%%%%%%%%%%%%%%%% Help methods %%%%%%%%%%%%%%%%%%%%%%%%%%%%  */

    private void showMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public boolean isGPSEnabled(Context mContext) {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void locationAlert() {
        if (!isGPSEnabled(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Usługi lokalizacji wyłączone");
            builder.setMessage("Aby korzystać z aplikacji należy włączyć usługi lokalizacji.");
            builder.setPositiveButton("Ustawienia", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            });
            builder.setNegativeButton("Anuluj", null);
            builder.create().show();
            return;
        }

    }

    public void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_LOCATION_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showMsg("Brak uprawnień do korzystania z lokalizacji.");
                    checkPermissions();
                }
                return;
            }
        }
    }

    // Updating UI
    private void startUIThread() {
        UIThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI();
                            }
                        });
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        Log.e("System", "UI Thread starts");
        UIThread.start();
    }

    private  void stopUIThread() {
        UIThread.interrupt();
        Log.e("System", "UI Thread stops");
    }

    public void updateUI() {
        latitudeText.setText(gpsService.getLatitude());
        longitudeText.setText(gpsService.getLongitude());
        satellites.setText(gpsService.getSatellitesInUse() + " / " + gpsService.getSatellitesInView());
        speedText.setText(gpsService.getSpeed());
        cityText.setText(gpsService.getCity());
        pincodeText.setText(gpsService.getPincode());
        streetText.setText(gpsService.getStreet());
    }
}