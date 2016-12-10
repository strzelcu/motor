package com.example.strzelcu.motor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import static com.example.strzelcu.motor.R.drawable.monitor_button_yellow;


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

    // Declarations

    public final static String EXTRA_MESSAGE = "com.example.strzelcu.motor.MESSAGE";
    boolean isMonitorOn = false;
    Notification note = new Notification();

    //Instance of GpsService class
    protected GpsService gps;
    protected boolean isServiceGPSConnect = false;

    //Interface
    protected TextView latitudeText;
    protected TextView longitudeText;
    protected TextView satellites;
    protected TextView speedText;
    protected TextView cityText;
    protected TextView pincodeText;
    protected TextView streetText;
    protected Button monitoring;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
                if (!isMonitorOn) {
                    monitoring.setBackgroundResource(R.drawable.monitor_button_green_tint);
                } else {
                    monitoring.setBackgroundResource(R.drawable.monitor_button_yellow_tint);
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
                        note.showNotification(getApplicationContext());

                        /*if (isGPSEnabled(MainActivity.this)) {
                            gps.startGPS();
                        }*/

                    } else {
                        gps.showGPSAlert();
                    }

                } else {
                    monitoring.setBackgroundResource(R.drawable.monitor_button_green);
                    Log.e("System", "Monitor stopped");
                    note.hideNotification();
                    isMonitorOn = false;
                    if (isServiceGPSConnect) {
                        gps.stopGPS();
                    }
                }
            }
        });

        /* Start GPS service */

        Intent gps = new Intent(this, GpsService.class);
        bindService(gps, mConnection, Context.BIND_AUTO_CREATE);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onResume() {
        Log.e("System", "Main activity resumed");
        locationAlert();
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.e("System", "Main activity stopped");
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    public void onDestroy() {
        Log.e("System", "Main activity destroyed");
        super.onDestroy();
        if (isServiceGPSConnect) {
            unbindService(mConnection);
            isServiceGPSConnect = false;
        }
    }

    // Saving activity state

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isMonitorOn = savedInstanceState.getBoolean(STATE_MONITOR);
            if (isMonitorOn) {
                monitoring.setBackgroundResource(R.drawable.monitor_button_yellow);
            } else {
                monitoring.setBackgroundResource(R.drawable.monitor_button_green);
            }
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
        } else {

        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            GpsService.LocalBinder binder = (GpsService.LocalBinder) service;
            gps = binder.getService();
            isServiceGPSConnect = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceGPSConnect = false;
        }
    };

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

}