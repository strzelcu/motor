package com.tomaszstrzelecki.motor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.tomaszstrzelecki.motor.dbhelper.DatabaseHelper;
import com.tomaszstrzelecki.motor.dbhelper.DatabaseProvider;

public class TracksActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private DatabaseHelper dbh;
    private DatabaseProvider dbp;
    private Cursor cursor;
    CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbh = new DatabaseHelper(this);
        db = dbh.getWritableDatabase();
        dbp = new DatabaseProvider(db);
        drawTrackList();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;

        switch (item.getItemId()) {

            case R.id.delete_tracks:
                AlertDialog deleteDialog = askAllTracksDelete();
                deleteDialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void drawTrackList() {
        cursor = db.query("TRACKS", new String[] {"_id", "NAME"}, null, null, null, null, "_id DESC");
        adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[] {"NAME"},
                new int[]{android.R.id.text1},
                0);
        TracklistFragment fragment = new TracklistFragment(adapter, this);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    public static class TracklistFragment extends ListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

        CursorAdapter adapter;
        TracksActivity context;

        public TracklistFragment() {

        }

        public TracklistFragment(CursorAdapter adapter, TracksActivity context) {
            this.context = context;
            this.adapter = adapter;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tracklist_fragment_layout, container, false);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(adapter);
            getListView().setOnItemClickListener(this);
            getListView().setOnItemLongClickListener(this);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            context.openMap(adapter.getCursor().getString(1));
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            AlertDialog deleteDialog = context.askTrackDelete(adapter.getCursor().getString(1));
            deleteDialog.show();
            return true;
        }
    }

    public void openMap(String trackName) {
        Intent i = new Intent(TracksActivity.this, MapsActivity.class);
        i.putExtra("TRACK_NAME", trackName);
        TracksActivity.this.startActivity(i);
    }

    private AlertDialog askTrackDelete(final String trackName) {
        AlertDialog myDeletingDialogBox = new AlertDialog.Builder(this)
                // Set message and others
                .setTitle("Usuwanie trasy")
                .setMessage("Czy chcesz usunąć wybraną trasę? Usunięcie jest nieodwracalne.")
                .setIcon(R.drawable.ic_delete_forever_black_24dp)
                .setPositiveButton("Usuń", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTrack(trackName);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myDeletingDialogBox;
    }

    private AlertDialog askAllTracksDelete() {
        AlertDialog myAllDeletingDialogBox = new AlertDialog.Builder(this)
                // Set message and others
                .setTitle("Usuwanie tras")
                .setMessage("Czy chcesz usunąć wszystkie zapisane trasy? Usunięcie jest nieodwracalne.")
                .setIcon(R.drawable.ic_delete_forever_black_24dp)
                .setPositiveButton("Usuń wszystkie", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllTracks();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myAllDeletingDialogBox;
    }

    public void deleteTrack(String trackName) {
        dbp.deleteTrack(trackName);
        Toast toast = Toast.makeText(getApplicationContext(), trackName + " została usunięta ", Toast.LENGTH_SHORT);
        toast.show();
        drawTrackList();
    }

    public void deleteAllTracks() {
        dbp.deleteAllRecords();
        Toast toast = Toast.makeText(getApplicationContext(), "Wszystkie trasy zostały usunięte", Toast.LENGTH_SHORT);
        toast.show();
        drawTrackList();
    }

    public void onDestroy() {
        Log.e("System", "Tracks activity destroyed");
        super.onDestroy();
        cursor.close();
        db.close();
    }
}