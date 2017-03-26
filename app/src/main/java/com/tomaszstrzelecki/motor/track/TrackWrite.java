package com.tomaszstrzelecki.motor.track;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tomaszstrzelecki.motor.dbhelper.DatabaseHelper;
import com.tomaszstrzelecki.motor.dbhelper.DatabaseProvider;
import com.tomaszstrzelecki.motor.util.Notifications;

import java.util.ArrayList;

import static com.tomaszstrzelecki.motor.util.DateStamp.getStringDate;
import static com.tomaszstrzelecki.motor.util.DateStamp.getStringDateTime;
import static com.tomaszstrzelecki.motor.util.DateStamp.getStringTime;

public class TrackWrite {

    private String name;
    private String date;
    private String time;
    private String startTime;
    private String stopTime;
    private int speed;
    private int distance;
    private ArrayList<Waypoint> waypoints;
    private Context context;
    private Notifications note;

    public TrackWrite(Context context) {
        this.name = "Trasa z dnia " +
                getStringDate() +
                "r. godz. " +
                getStringTime();
        this.date = getStringDateTime();
        // TODO startTime = getStringTime();??
        this.context = context;
        note = new Notifications(context);
        waypoints = new ArrayList<Waypoint>();
    }

    public void addWaypoint(double latitude, double longitude) {
        waypoints.add(new Waypoint(latitude, longitude));
    }



    public void setTime() {
        // TODO Do the time setter and calculate time
    }

    public void setDistance(int distance) {
        this.distance += distance;
    }

    public void setAveregeSpeed(int speed) {
        this.speed = (this.speed + speed) / 2;
    }

    public void saveToDatabase() {
        if( waypoints.size() > 1) {
            note.showToastMsg("Zapisano trasę");
            DatabaseHelper dbh = new DatabaseHelper(context);
            SQLiteDatabase db = dbh.getWritableDatabase();
            DatabaseProvider dbp = new DatabaseProvider(db);
            dbp.addNewTrack(name, date, time, speed, distance, waypoints);
            db.close();
            dbh.close();
        } else {
            note.showToastMsg("Nie zapisano trasy, ponieważ jest za krótka");
        }
    }


}
