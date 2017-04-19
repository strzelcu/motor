package com.tomaszstrzelecki.motor.popup;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import com.tomaszstrzelecki.motor.R;

public class StatisticsPopup extends Activity {

    protected TextView trackNameText;
    protected TextView trackSpeedText;
    protected TextView trackDistanceText;
    protected TextView trackTimeText;
    protected TextView trackWaypoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popupwindow);

        trackNameText = (TextView) findViewById(R.id.track_name);
        trackSpeedText = (TextView) findViewById(R.id.track_speed);
        trackDistanceText = (TextView) findViewById(R.id.track_distance);
        trackTimeText = (TextView) findViewById(R.id.track_time);
        trackWaypoints = (TextView) findViewById(R.id.track_waypoints);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width =  (int) (dm.widthPixels * 0.8);
        int height = (int) (dm.heightPixels * 0.4);

        getWindow().setLayout(width,height);

        Bundle extras = getIntent().getExtras();
        trackNameText.setText(extras.getString("NAME"));
        trackSpeedText.setText("Średnia prędkość: " + extras.getString("SPEED") + " km/h");
        trackDistanceText.setText("Dystans: " + getKilometers(extras.getString("DISTANCE")));
        trackTimeText.setText("Czas trasy: " + extras.getString("TIME"));
        trackWaypoints.setText("Ilość punktów pomiarowych: " + extras.getString("WAYPOINTS"));

    }

    public String getKilometers(String meters) {

        String kilometers;

        try {
            kilometers = (Double.valueOf(meters)/1000) + " km";
        } catch (Exception e) {
            Log.e("StatisticsPopup", "There is no value in meters string (" + meters + ")");
            kilometers = "null";
        }
        return kilometers;
    }
}