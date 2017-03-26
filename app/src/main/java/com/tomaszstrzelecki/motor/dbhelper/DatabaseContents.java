package com.tomaszstrzelecki.motor.dbhelper;

import android.provider.BaseColumns;

public final class DatabaseContents {

    private DatabaseContents() {}

    public static class Tracks implements BaseColumns {

        // TABLE TRACKS

        public static final String TABLE_NAME = "TRACKS";
        public static final String NAME = "NAME";
        public static final String DATE = "DATE";
        public static final String TIME_OF_TRAVEL = "TIME";
        public static final String AVARAGE_SPEED = "SPEED";
        public static final String DISTANCE = "DISTANCE";

    }

    public static class Waypoints implements BaseColumns {

        // TABLE WAYPOINTS

        public static final String TABLE_NAME = "WAYPOINTS";
        public static final String LATITUDE = "LATITUDE";
        public static final String LONGITUDE = "LONGITUDE";
        public static final String TRACK_ID = "TRACKID";

    }

}
