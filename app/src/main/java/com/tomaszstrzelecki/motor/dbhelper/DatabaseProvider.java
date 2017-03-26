package com.tomaszstrzelecki.motor.dbhelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tomaszstrzelecki.motor.track.Waypoint;

import java.util.ArrayList;

public class DatabaseProvider {

    private SQLiteDatabase db;
    private Cursor cursor;

    // SQL Queries

    public DatabaseProvider(SQLiteDatabase db) {
        this.db = db;
    }

    public void addNewTrack(String name, String date, String time, int speed, int distance, ArrayList<Waypoint> waypoints) {

        int id;
        ContentValues trackValues = new ContentValues();
        trackValues.put(DatabaseContents.Tracks.NAME, name);
        trackValues.put(DatabaseContents.Tracks.DATE, date);
        trackValues.put(DatabaseContents.Tracks.TIME_OF_TRAVEL, time);
        trackValues.put(DatabaseContents.Tracks.AVARAGE_SPEED, speed);
        trackValues.put(DatabaseContents.Tracks.DISTANCE, distance);
        db.insert(DatabaseContents.Tracks.TABLE_NAME, null, trackValues);
        Cursor trackIDCursor = db.rawQuery("SELECT * FROM " +
                DatabaseContents.Tracks.TABLE_NAME + " WHERE " +
                DatabaseContents.Tracks.NAME + " = '" + name + "'", null);
        trackIDCursor.moveToFirst();
        id = trackIDCursor.getInt(1);
        Log.e("System", "Save track with id: " + id);
        for (Waypoint waypoint: waypoints) {
            ContentValues waypointValue = new ContentValues();
            waypointValue.put(DatabaseContents.Waypoints.LATITUDE, waypoint.getLatitude());
            waypointValue.put(DatabaseContents.Waypoints.LONGITUDE, waypoint.getLongitude());
            waypointValue.put(DatabaseContents.Waypoints.TRACK_ID, id);
            db.insert(DatabaseContents.Waypoints.TABLE_NAME, null, waypointValue);
        }
    }

    public void deleteAllRecords() {
        db.execSQL(DatabaseHelper.SQL_DELETE_ALL_WAYPOINTS);
        db.execSQL(DatabaseHelper.SQL_DELETE_ALL_TRACKS);
    }

}
