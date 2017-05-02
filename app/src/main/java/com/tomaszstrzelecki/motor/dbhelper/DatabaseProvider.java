package com.tomaszstrzelecki.motor.dbhelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import com.tomaszstrzelecki.motor.MainActivity;
import com.tomaszstrzelecki.motor.track.Waypoint;

import java.util.ArrayList;

import static android.R.attr.name;

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
        Cursor trackIDCursor  = db.rawQuery("SELECT _id, NAME FROM TRACKS WHERE NAME = '" + name + "' ORDER BY _id", null);
        trackIDCursor.moveToFirst();
        id = trackIDCursor.getInt(trackIDCursor.getColumnIndex("_id"));
        Log.i("DatabaseProvider", "Save track with id: " + id + " trackIDCursor.getCount() = " + trackIDCursor.getCount());
        for (Waypoint waypoint: waypoints) {
            ContentValues waypointValue = new ContentValues();
            waypointValue.put(DatabaseContents.Waypoints.LATITUDE, waypoint.getLatitude());
            waypointValue.put(DatabaseContents.Waypoints.LONGITUDE, waypoint.getLongitude());
            waypointValue.put(DatabaseContents.Waypoints.TRACK_ID, id);
            db.insert(DatabaseContents.Waypoints.TABLE_NAME, null, waypointValue);
        }
        trackIDCursor.close();
    }

    public void deleteTrack(String trackName) {
        String SQLQuery = null;
        try {
            Cursor trackCursor = db.rawQuery("SELECT * FROM TRACKS WHERE NAME = '" + trackName + "'", null);
            trackCursor.moveToFirst();
            SQLQuery = "DELETE FROM " +
                    DatabaseContents.Tracks.TABLE_NAME +
                    " WHERE " + DatabaseContents.Tracks.NAME +
                    "= '" + trackName + "'";
            db.execSQL(SQLQuery);
            SQLQuery = "DELETE FROM " +
                    DatabaseContents.Waypoints.TABLE_NAME +
                    " WHERE " + DatabaseContents.Waypoints.TRACK_ID +
                    "= " + trackCursor.getString(0);
            db.execSQL(SQLQuery);
            trackCursor.close();
        } catch (SQLException e) {
            Log.e("DatabaseProvider", "There is some problem with database or SQLQuery: " + e);
            Log.e("DatabaseProvider", "SQLQuery = " + SQLQuery);
        }
    }

    public void deleteAllRecords() {
        db.execSQL(DatabaseHelper.SQL_DELETE_ALL_WAYPOINTS);
        db.execSQL(DatabaseHelper.SQL_DELETE_ALL_TRACKS);
    }

}
