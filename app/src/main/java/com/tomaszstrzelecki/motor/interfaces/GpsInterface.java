package com.tomaszstrzelecki.motor.interfaces;

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