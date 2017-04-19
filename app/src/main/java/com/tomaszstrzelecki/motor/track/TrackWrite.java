package com.tomaszstrzelecki.motor.track;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.util.Log;

import com.tomaszstrzelecki.motor.dbhelper.DatabaseHelper;
import com.tomaszstrzelecki.motor.dbhelper.DatabaseProvider;
import com.tomaszstrzelecki.motor.util.Notifications;

import java.util.ArrayList;
import java.util.Date;

import static com.tomaszstrzelecki.motor.util.DateStamp.getDifferenceBetweenTwoDates;
import static com.tomaszstrzelecki.motor.util.DateStamp.getStringDate;
import static com.tomaszstrzelecki.motor.util.DateStamp.getStringDateTime;
import static com.tomaszstrzelecki.motor.util.DateStamp.getStringTime;

public class TrackWrite {

    private String name;
    private String date;
    private String time;
    private Date startTime;
    private int speed = 0;
    private double distance;
    private ArrayList<Waypoint> waypoints;
    private Context context;
    private Notifications note;
    private Waypoint previousWaypoint;

    public TrackWrite(Context context) {
        this.name = "Trasa z dnia " +
                getStringDate() +
                "r. godz. " +
                getStringTime();
        this.date = getStringDateTime();
        this.startTime = new Date();
        this.context = context;
        distance = 0;
        note = new Notifications(context);
        waypoints = new ArrayList<>();
    }

    public void addWaypoint(double latitude, double longitude) {
        addDistance(latitude, longitude);
        waypoints.add(new Waypoint(latitude, longitude));
    }

    private void addDistance(double newLatitude, double newLongitude) {
        if (previousWaypoint != null) {
            distance += calculateDistance(
                    previousWaypoint.getLatitude(),
                    previousWaypoint.getLongitude(),
                    newLatitude,
                    newLongitude);
            previousWaypoint = new Waypoint(newLatitude, newLongitude);
        } else {
            previousWaypoint = new Waypoint(newLatitude, newLongitude);
        }
        Log.i("TrackWrite", "Trasa ma długość " + distance + " metrów."); // TODO REMOVE THIS!
    }

    public void setAveregeSpeed(int speed) {
        this.speed = (this.speed + speed) / 2;
    }

    public void saveToDatabase() {
        if( distance > 1000) { // If track distance is more than 1 KM.
            Date stopTime = new Date();
            time = getDifferenceBetweenTwoDates(startTime, stopTime);
            saveTrack();
        } else {
            note.showToastMsg("Nie zapisano trasy, ponieważ jest za krótka");
        }
    }

    private void saveTrack() {
        Thread saveThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                while (!isInterrupted()) {
                    DatabaseHelper dbh = new DatabaseHelper(context);
                    SQLiteDatabase db = dbh.getWritableDatabase();
                    DatabaseProvider dbp = new DatabaseProvider(db);
                    dbp.addNewTrack(name, date, time, speed, (int) distance, waypoints);
                    db.close();
                    dbh.close();
                    this.interrupt();
                }
            }
        };
        Log.i("TrackWrite", "Saving track to database.");
        saveThread.start();
    }

    private double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude)
    {
        double theta = startLongitude - endLongitude;
        double dist = Math.sin(deg2rad(startLatitude)) * Math.sin(deg2rad(endLatitude)) + Math.cos(deg2rad(startLatitude)) * Math.cos(deg2rad(endLatitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 1000;
        return dist;
    }

    private double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }
}
