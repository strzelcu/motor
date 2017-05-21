package com.tomaszstrzelecki.motor.gpshandle;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.R;
import com.tomaszstrzelecki.motor.accidenthandle.Alarm;
import com.tomaszstrzelecki.motor.gpshandle.track.TrackWrite;
import com.tomaszstrzelecki.motor.util.Distance;
import com.tomaszstrzelecki.motor.util.Notifications;

import static com.tomaszstrzelecki.motor.AppService.isMonitorOn;

public class GpsService extends Service implements GpsInterface{

    private final IBinder mBinder = new LocalBinder();
    private double previousLatitude = 0;
    private double previousLongitude = 0;
    private boolean hasRaisedAlarm = false;
    public static double latitude;
    public static double longitude;
    private double speed;
    private String SatellitesInView = "0";
    private String SatellitesInUse = "0";
    private GpsStatus mGpsStatus;
    private LocationManager locationManager;
    private LocationListener locationListener;
    protected GpsListener gpsListener = new GpsListener();
    private TrackWrite track;
    private Thread distanceCheckThread;
    private Alarm alarm;
    private final String TAG = "GpsService";

    public void startGPS() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Notifications.showLongToastMsg(getString(R.string.no_localization_rights), getApplicationContext());
            return;
        }
        alarm = Alarm.getInstance();
        startDistanceCheckThread();
        track = new TrackWrite(this);
        Notifications.showNotificationMonitoring(getApplicationContext());
        Toast.makeText(this, R.string.gps_prepare_device, Toast.LENGTH_LONG).show();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 2, locationListener);
        locationManager.addGpsStatusListener(gpsListener);
    }

    public void stopGPS() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Notifications.showLongToastMsg(getString(R.string.no_localization_rights), getApplicationContext());
            return;
        }
        stopDistanceCheckThread();
        locationManager.removeUpdates(locationListener);
        Notifications.hideNotification();
        track.saveToDatabase();
    }

    private class GpsListener implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
            int iCountInView = 0;
            int iCountInUse = 0;
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Notifications.showLongToastMsg(getString(R.string.no_localization_rights), getApplicationContext());
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
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    if(isMonitorOn){
                        Notifications.vibrate(1000, getApplicationContext());
                        Notifications.showLongToastMsg(getString(R.string.gps_localization_found), getApplicationContext());
                    }
                    break;
                default:
            }

            SatellitesInView = String.valueOf(iCountInView);
            SatellitesInUse = String.valueOf(iCountInUse);

        }
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
                    Log.i(TAG, "Latitude " + location.getLatitude());
                    Log.i(TAG, "Longitude " + location.getLongitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    speed = location.getSpeed();
                    track.addWaypoint(latitude, longitude);
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

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        Notifications.hideNotification();
        super.onDestroy();
        }

    private void startDistanceCheckThread() {
        distanceCheckThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        double distance = getLastDistance();
                        if(distance < 10 && !hasRaisedAlarm){
                            alarm.raiseAlarm();
                            hasRaisedAlarm = true;
                        } else if(distance >= 10 && hasRaisedAlarm){
                            alarm.reduceAlarm();
                            hasRaisedAlarm = false;
                        }
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        Log.i(TAG, "System check thread started");
        distanceCheckThread.start();
    }

    private  void stopDistanceCheckThread() {
        distanceCheckThread.interrupt();
        Log.i(TAG, "System check thread stoped");
    }

    private double getLastDistance() {
        if(previousLatitude == 0 && previousLongitude == 0) {
            previousLatitude = latitude;
            previousLongitude = longitude;
            return 10;
        } else {
            double distance = Distance.calculateDistance(previousLatitude, previousLongitude, latitude, longitude);
            Log.i(TAG, "Distance: " + distance);
            previousLatitude = latitude;
            previousLongitude = longitude;
            return distance;
        }
    }
}