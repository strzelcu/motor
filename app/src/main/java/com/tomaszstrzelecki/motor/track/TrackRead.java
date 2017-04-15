package com.tomaszstrzelecki.motor.track;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tomaszstrzelecki.motor.dbhelper.DatabaseHelper;

import java.util.ArrayList;

public class TrackRead {

    private String name;
    private String date;
    private String time;
    private int speed;
    private int distance;
    private ArrayList<Waypoint> waypoints;

    public TrackRead(String name, Context context) {
        waypoints = new ArrayList<>();
        DatabaseHelper dbh = new DatabaseHelper(context);
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor trackCursor  = db.rawQuery("SELECT * FROM TRACKS WHERE NAME = '" + name + "'", null);
        trackCursor.moveToFirst();
        this.name = trackCursor.getString(1);
        this.date = trackCursor.getString(2);
        this.time = trackCursor.getString(3);
        this.speed = Integer.valueOf(trackCursor.getString(4));
        this.distance = Integer.valueOf(trackCursor.getString(5));
        try {
            Cursor waypointsCursor = db.rawQuery("SELECT * FROM WAYPOINTS WHERE TRACKID = '"
                    + trackCursor.getString(0) + "' ORDER BY _id", null);
            waypointsCursor.moveToFirst();
            for(int i = 0; i < waypointsCursor.getCount(); i++) {
                waypoints.add(new Waypoint(waypointsCursor.getDouble(1), waypointsCursor.getDouble(2)));
                waypointsCursor.moveToNext();

            }
            Log.i("TrackRead", "Waypoints are loaded");
            waypointsCursor.close();
        } catch (SQLException e) {
            Log.e("TrackRead", "Database problem");
        }
        trackCursor.close();

    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDistance() {
        return distance;
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }
}
