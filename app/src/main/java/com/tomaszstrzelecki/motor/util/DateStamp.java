package com.tomaszstrzelecki.motor.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateStamp {

    private static Calendar c;

    public static String getStringDate() {
        return new SimpleDateFormat("dd.MM.yyyy").format(new Date());
    }

    public static String getStringTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String getStringDateTime() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
    }

    public static String getDifferenceBetweenTwoDates(Date startDate, Date endDate) {

        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        String difference = "";

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if(elapsedHours > 0) difference += elapsedHours + " godz ";
        if(elapsedMinutes > 0) difference += elapsedMinutes + " min ";
        difference += elapsedSeconds + " sek";

        return difference;
    }
}
