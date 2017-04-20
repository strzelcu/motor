package com.tomaszstrzelecki.motor;

import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tomaszstrzelecki.motor.R;
import com.tomaszstrzelecki.motor.popup.StatisticsPopup;
import com.tomaszstrzelecki.motor.track.TrackRead;
import com.tomaszstrzelecki.motor.util.KML;
import com.tomaszstrzelecki.motor.util.Notifications;

import static android.R.attr.value;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TrackRead track;
    private LatLngBounds trackBounds;
    private KML kml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton myFab = (FloatingActionButton) this.findViewById(R.id.floatingActionButton);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showStatistics();
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String name = extras.getString("TRACK_NAME");
            track = new TrackRead(name, this);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toast toast = Toast.makeText(this, "" + track.getName(), Toast.LENGTH_SHORT);
        toast.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Add Poliline and boundries

        PolylineOptions polyLineOptions = new PolylineOptions();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(int i = 0; i < track.getWaypoints().size(); i++) {
            polyLineOptions.add(new LatLng(track.getWaypoints().get(i).getLatitude(), track.getWaypoints().get(i).getLongitude()));
            builder.include(track.getWaypoints().get(i).getLatLng());
        }
        polyLineOptions.color(Color.rgb(79, 195, 247));
        trackBounds = builder.build();
        mMap.addPolyline(polyLineOptions);

        //Add markers of Start and End of track

        LatLng start = new LatLng(track.getWaypoints().get(0).getLatitude(), track.getWaypoints().get(0).getLongitude());
        LatLng end = new LatLng(track.getWaypoints().get(track.getWaypoints().size() - 1).getLatitude(),
                track.getWaypoints().get(track.getWaypoints().size() - 1).getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(start).draggable(false).title("Start").visible(true).flat(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(markerOptions);

        markerOptions.position(end).draggable(false).title("Koniec").visible(true).flat(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(markerOptions);

        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
        mMap.moveCamera(CameraUpdateFactory.zoomBy(15));

        // Show the Track in bounds

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(trackBounds, 300);
                mMap.animateCamera(cu, 1000, null);
            }
        });

        // Set marker not clickable

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;

        switch (item.getItemId()) {

            case R.id.edit_map:
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return false;
                    }
                });
                item.setVisible(false);
                Toast toast = Toast.makeText(this, "Mapa została odblokowana", Toast.LENGTH_SHORT);
                toast.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void showStatistics() {
        Intent popupWindow = new Intent(MapsActivity.this, StatisticsPopup.class);
        popupWindow.putExtra("NAME", track.getName());
        popupWindow.putExtra("SPEED", "" + track.getSpeed());
        popupWindow.putExtra("DISTANCE", "" + track.getDistance());
        popupWindow.putExtra("TIME", "" + track.getTime());
        popupWindow.putExtra("WAYPOINTS", "" + track.getWaypoints().size());
        startActivity(popupWindow);
        kml = new KML(track.getName(), track.getWaypoints()); // TODO zrobić przycisk do eksportu trasy do KML
    }
}
