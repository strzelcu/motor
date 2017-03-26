package com.tomaszstrzelecki.motor.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Tracks.db";
    private static final int DB_VERSION = 1;

    // SQL QUERIES

    public static final String SQL_CREATE_TRACKS_TABLE =
            "CREATE TABLE " + DatabaseContents.Tracks.TABLE_NAME +
            " (" + DatabaseContents.Tracks._ID + " INTEGER PRIMARY KEY," +
            DatabaseContents.Tracks.NAME + " TEXT," +
            DatabaseContents.Tracks.DATE + " NUMERIC," +
            DatabaseContents.Tracks.TIME_OF_TRAVEL + " TEXT," +
            DatabaseContents.Tracks.AVARAGE_SPEED + " INTEGER," +
            DatabaseContents.Tracks.DISTANCE + " INTEGER)";

    public static final String SQL_CREATE_WAYPOINTS_TABLE =
            "CREATE TABLE " + DatabaseContents.Waypoints.TABLE_NAME +
            " (" + DatabaseContents.Waypoints._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DatabaseContents.Waypoints.LATITUDE + " TEXT," +
            DatabaseContents.Waypoints.LONGITUDE + " TEXT," +
            DatabaseContents.Waypoints.TRACK_ID + " INTEGER," +
            "FOREIGN KEY(" + DatabaseContents.Waypoints.TRACK_ID + ") REFERENCES " +
            DatabaseContents.Tracks.TABLE_NAME + "(" + DatabaseContents.Tracks._ID + ")" +
                    ")";

    public static final String SQL_DELETE_ALL_TRACKS =
            "DELETE FROM " + DatabaseContents.Tracks.TABLE_NAME;

    public static final String SQL_DELETE_ALL_WAYPOINTS =
            "DELETE FROM " + DatabaseContents.Waypoints.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRACKS_TABLE);
        db.execSQL(SQL_CREATE_WAYPOINTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int currentVersion, int newVersion) {

    }

}
