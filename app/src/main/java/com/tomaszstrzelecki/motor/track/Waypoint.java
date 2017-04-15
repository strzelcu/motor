package com.tomaszstrzelecki.motor.track;

import com.google.android.gms.maps.model.LatLng;

public class Waypoint {

    private double latitude;
    private double longitude;

    Waypoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getLatLng() { return new LatLng(latitude, longitude); }
}
