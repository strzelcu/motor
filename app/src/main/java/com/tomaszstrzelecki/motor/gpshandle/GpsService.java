package com.tomaszstrzelecki.motor.gpshandle;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.track.TrackWrite;
import com.tomaszstrzelecki.motor.util.Notifications;

import java.util.List;
import java.util.Locale;

import static android.R.attr.value;
import static android.location.LocationProvider.AVAILABLE;
import static android.location.LocationProvider.OUT_OF_SERVICE;
import static android.location.LocationProvider.TEMPORARILY_UNAVAILABLE;

public class GpsService extends Service implements GpsInterface{

    private final IBinder mBinder = new LocalBinder();
    public static double latitude;
    public static double longitude;
    private double speed;
    private String SatellitesInView = "0";
    private String SatellitesInUse = "0";
    private GpsStatus mGpsStatus;
    LocationManager locationManager;
    LocationListener locationListener;
    protected GpsListener gpsListener = new GpsListener();
    private Notifications note = new Notifications(this);
    TrackWrite track;
    Vibrator v;

    public void startGPS() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showMsg("Brak uprawnień do usług lokalizacji");
            return;
        }
        track = new TrackWrite(this);
        note.showNotificationMonitoring();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 2, locationListener);
        locationManager.addGpsStatusListener(gpsListener);
    }

    public void stopGPS() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showMsg("Brak uprawnień do usług lokalizacji");
            return;
        }
        locationManager.removeUpdates(locationListener);
        note.hideNotification();
        track.saveToDatabase();
    }

    private class GpsListener implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
            int iCountInView = 0;
            int iCountInUse = 0;
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showMsg("Brak uprawnień do usług lokalizacji");
                return;
            }
            mGpsStatus = locationManager.getGpsStatus(mGpsStatus);
            Iterable<GpsSatellite> satellites = mGpsStatus.getSatellites();
            if (satellites != null) {
                for (GpsSatellite gpsSatellite : satellites) {
                    iCountInView++;
                    if (gpsSatellite.usedInFix()) {
                        iCountInUse++;
                    }
                }
            }

            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    showMsg("Szukanie lokalizacji");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    showMsg("Znaleziono lokalizację. Schowaj urządzenie!");
                    vibrate(1000);
                    break;
                default:
            }

            SatellitesInView = String.valueOf(iCountInView);
            SatellitesInUse = String.valueOf(iCountInUse);

        }
    }

    public void onStartCommand() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public GpsService getService() {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.e("Coordinates", "Latitude " + location.getLatitude());
                    Log.e("Coordinates", "Longitude " + location.getLongitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    speed = location.getSpeed();
                    track.addWaypoint(latitude, longitude);
                    track.setAveregeSpeed((int) (speed*3.6));
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
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            return GpsService.this;
        }
    }

    // Help Methods

    private void showMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    private void vibrate(int miliseconds) {
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(miliseconds);
    }

    private int getDistanceTo(double startLatitude, double startLongitude, Location endLocation) {
        Location startLocation = new Location("START");
        startLocation.setLatitude(startLatitude);
        startLocation.setLongitude(startLongitude);
        float distance = startLocation.distanceTo(endLocation);
        return Math.round(distance);
    }

    // Public getters

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getSpeed() {
        Double speed = this.speed*3.6;
        return "" + speed.intValue();
    }

    public String getSatellitesInView() {
        return SatellitesInView;
    }

    public String getSatellitesInUse() {
        return SatellitesInUse;
    }

}