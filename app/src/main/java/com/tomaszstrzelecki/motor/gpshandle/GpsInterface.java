package com.tomaszstrzelecki.motor.gpshandle;

public interface GpsInterface {
    void startGPS();
    void stopGPS();
    String getLatitude();
    String getLongitude();
    String getSpeed();
    String getSatellitesInView();
    String getSatellitesInUse();
    String getStreet();
    String getCity();
    String getPincode();
}