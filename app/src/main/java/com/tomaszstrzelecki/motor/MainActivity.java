package com.tomaszstrzelecki.motor;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.dbhelper.DatabaseHelper;
import com.tomaszstrzelecki.motor.dbhelper.DatabaseProvider;
import com.tomaszstrzelecki.motor.util.Notifications;

import java.util.List;
import java.util.Locale;

import static com.tomaszstrzelecki.motor.AppService.isMonitorOn;
import static com.tomaszstrzelecki.motor.AppService.isNetworkOn;
import static com.tomaszstrzelecki.motor.MapsActivity.REQUEST_WRITE_EXTERNAL_STORAGE_RESULT;
import static com.tomaszstrzelecki.motor.R.mipmap.ic_launcher;

public class MainActivity extends AppCompatActivity {

    // Permissions static variable
    static final int PERMISSION_LOCATION_CODE = 1;

    // Declarations

    Notifications note = new Notifications(this);
    Thread UIThread;
    private String city;
    private String pincode;
    private String street;

    //App Service
    AppService appService;
    protected boolean isAppServiceConnect = false;
    Intent appServiceIntent;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            isAppServiceConnect = true;
            updateUI();
            Log.i("MainActivity", "AppService is binded to MainActivity");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isAppServiceConnect = false;
            Log.i("MainActivity", "AppService is unbinded from MainActivity");
        }
    };

    //Interface
    protected TextView latitudeText;
    protected TextView longitudeText;
    protected TextView satellites;
    protected TextView speedText;
    protected TextView cityText;
    protected TextView pincodeText;
    protected TextView streetText;
    protected Button monitoring;
    protected ImageView monitoringImage;
    protected TextView monitoringTextView;

    // Activity life cycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setLogo(ic_launcher);
        } catch (NullPointerException e) {
            Log.e("MainActivity", "Can't set logo in the action bar. " + e.getMessage());
        }
        Log.i("MainActivity", "Main activity created");

        //Graphics objects
        monitoring = (Button) findViewById(R.id.mainButton);
        latitudeText = (TextView) findViewById(R.id.latitudeValue);
        longitudeText = (TextView) findViewById(R.id.longitudeValue);
        satellites = (TextView) findViewById(R.id.satellitesValue);
        speedText = (TextView) findViewById(R.id.speedValue);
        cityText = (TextView) findViewById(R.id.cityValue);
        pincodeText = (TextView) findViewById(R.id.pincodeValue);
        streetText = (TextView) findViewById(R.id.streetValue);
        monitoringImage = (ImageView) findViewById(R.id.main_button_image_view);
        monitoringTextView = (TextView) findViewById(R.id.main_button_text);

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
                        monitoring.setBackgroundResource(R.drawable.monitor_button_yellow);
                        monitoringImage.setImageResource(R.drawable.ic_stop_black_48dp);
                        monitoringTextView.setText(R.string.main_button_text_stop);
                        appService.startMonitoring();
                    } else {
                        note.showToastMsg("Lokalizacja wyłączona.");
                    }

                } else {
                    monitoring.setBackgroundResource(R.drawable.monitor_button_green);
                    monitoringImage.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                    monitoringTextView.setText(R.string.main_button_text_start);
                    appService.stopMonitoring();
                }
            }
        });

    }

    @Override
    public void onStart() {
        appServiceIntent = new Intent(this, AppService.class);
        bindService(appServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(appServiceIntent);
        startUIThread();
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.i("MainActivity", "Main activity resumed");
        checkPermissions();
        refreshMonitoringButton();
        locationAlert();
        super.onResume();
    }

    @Override
    public void onStop() {
        if (isAppServiceConnect) {
            unbindService(mConnection);
            isAppServiceConnect = false;
        }
        stopUIThread();
        super.onStop();
        Log.i("MainActivity", "Main activity stopped");
    }

    @Override
    public void onDestroy() {
        Log.i("MainActivity", "Main activity destroyed");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;

        switch (item.getItemId()) {

            case R.id.action_routes:
                i = new Intent(this, TracksActivity.class);
                this.startActivity(i);
                return true;

            case R.id.action_settings:
                i = new Intent(this, SettingsActivity.class);
                this.startActivity(i);
                return true;

            case R.id.action_exit:
                //deleteAllRecords();
                appService.onDestroy();
                stopService(appServiceIntent);
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
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_CODE: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    note.showToastMsg("Brak uprawnień do korzystania z lokalizacji.");
                    checkPermissions();
                }
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
        Log.i("MainActivity", "UI Thread starts");
        UIThread.start();
    }

    private  void stopUIThread() {
        UIThread.interrupt();
        Log.i("MainActivity", "UI Thread stops");
    }

    public void updateUI() {

        if(isMonitorOn) {
            try {
                latitudeText.setText("" + appService.getLatitude());
                longitudeText.setText("" + appService.getLongitude());
                satellites.setText(getString(R.string.using) + appService.getSatellitesInUse()
                        + " | "
                        + getString(R.string.available) +
                        appService.getSatellitesInView());
                speedText.setText(appService.getSpeed());
                if (isNetworkOn) {
                    updateAddress(appService.getLatitude(), appService.getLongitude());
                } else {
                    cityText.setText(R.string.not_available);
                    pincodeText.setText(R.string.not_available);
                    streetText.setText(R.string.not_available);
                }
            } catch (Exception e) {
                latitudeText.setText(R.string.searching);
                longitudeText.setText(R.string.searching);
                satellites.setText(getString(R.string.using) + " 0 / 0 " + getString(R.string.available));
                speedText.setText("0");
                cityText.setText(R.string.not_available);
                pincodeText.setText(R.string.not_available);
                streetText.setText(R.string.not_available);
            }
        } else {
            latitudeText.setText(null);
            longitudeText.setText(null);
            satellites.setText(null);
            speedText.setText(null);
            cityText.setText(null);
            pincodeText.setText(null);
            streetText.setText(null);
        }
    }

    public void refreshMonitoringButton() {
        if (!isMonitorOn) {
            monitoring.setBackgroundResource(R.drawable.monitor_button_green);
            monitoringTextView.setText(R.string.main_button_text_start);
            monitoringImage.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        } else {
            monitoring.setBackgroundResource(R.drawable.monitor_button_yellow);
            monitoringTextView.setText(R.string.main_button_text_stop);
            monitoringImage.setImageResource(R.drawable.ic_stop_black_48dp);
        }
    }

    private void updateAddress(double latitude, double longitude)
    {
        try
        {
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0)
            {
                cityText.setText(addresses.get(0).getLocality());
                pincodeText.setText(addresses.get(0).getPostalCode());
                streetText.setText(addresses.get(0).getAddressLine(0));
            }
        }

        catch (Exception e)
        {
            Log.e("MainActivity","" + e);
        }

    }

    // TO REMOVE AFTER TESTS

    public void deleteAllRecords() {
        DatabaseHelper dbh = new DatabaseHelper(this);
        SQLiteDatabase db = dbh.getWritableDatabase();
        DatabaseProvider dbp = new DatabaseProvider(db);
        dbp.deleteAllRecords();
        db.close();
        dbh.close();
    }
}