package com.tomaszstrzelecki.motor.util;

public class Distance {

    public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude)
    {
        double theta = startLongitude - endLongitude;
        double dist = Math.sin(deg2rad(startLatitude)) * Math.sin(deg2rad(endLatitude)) + Math.cos(deg2rad(startLatitude)) * Math.cos(deg2rad(endLatitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 1000;
        return dist;
    }

    private static double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }
}
