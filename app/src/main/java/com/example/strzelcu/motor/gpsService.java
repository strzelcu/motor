package com.example.strzelcu.motor;

import android.*;
import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Strzelcu on 2016-12-05.
 */

public class GpsService extends Service {

    /* Deklaracje */

    /* ## Obiekty */

    private final IBinder mBinder = new LocalBinder();
    LocationManager myManager;
    LocationListener gps;
    private Handler handler = new Handler();

    /* ## Zmienne */
    private double latitude;
    private double longitude;
    private double speed;
    private int satellites;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startGPS() {

        Toast.makeText(getApplicationContext(), "Monitorowanie rozpoczęte", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 2, gps);

    }

    public void stopGPS() {

        Toast.makeText(getApplicationContext(), "Monitorowanie zakończone", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Napisać metodę żądającą dostępu do lokalizacji dla API > 21
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myManager.removeUpdates(gps);
        latitude = 0;
        longitude = 0;
        speed = 0;
        satellites = 0;

    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getSpeed() {
        return (int) (speed * 3.6);
    }

    public int getSatellites() {return satellites; }

    public class LocalBinder extends Binder {

        GpsService getService() {
            gps = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.e("System", "Latitude " + location.getLatitude());
                    Log.e("System", "Longitude " + location.getLongitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    speed = location.getSpeed() * 1.3;

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }

            };
            myManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            return GpsService.this;
        }
    }

    public int showGPSAlert() {
        Toast.makeText(getApplicationContext(), "Usługi lokalizacji wyłączone", Toast.LENGTH_SHORT).show();
        return 0;
    }
}