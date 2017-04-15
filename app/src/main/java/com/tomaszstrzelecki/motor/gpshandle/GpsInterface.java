package com.tomaszstrzelecki.motor.gpshandle;

public interface GpsInterface {
    void startGPS();
    void stopGPS();
    Double getLatitude();
    Double getLongitude();
    String getSpeed();
    String getSatellitesInView();
    String getSatellitesInUse();
}