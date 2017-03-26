package com.tomaszstrzelecki.motor;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.dbhelper.DatabaseHelper;

public class TracksActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private Cursor cursor;
    CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        try {
            DatabaseHelper dbh = new DatabaseHelper(this);
            db = dbh.getReadableDatabase();
            cursor = db.query("TRACKS", new String[] {"_id", "NAME"}, null, null, null, null, "_id");
            adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    cursor,
                    new String[] {"NAME"},
                    new int[]{android.R.id.text1},
                    0);
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna", Toast.LENGTH_SHORT);
            toast.show();
        }

        TracklistFragment fragment = new TracklistFragment(adapter);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }


    public class TracklistFragment extends ListFragment implements AdapterView.OnItemClickListener {

        CursorAdapter adapter;

        public TracklistFragment(CursorAdapter adapter) {
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
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getActivity(), adapter.getCursor().getString(1).toString(), Toast.LENGTH_SHORT).show();
            openMap();
        }

    }

    public void onDestroy() {
        Log.e("System", "Tracks activity destroyed");
        super.onDestroy();
        cursor.close();
        db.close();
    }

    public void openMap() {
        Intent i = new Intent(TracksActivity.this, MapsActivity.class);
        TracksActivity.this.startActivity(i);
    }

}

/*    @Override
            public void onClick(View view) {
                Intent i = new Intent(RoutesActivity.this, MapsActivity.class);
                RoutesActivity.this.startActivity(i);
            }
        });
    }*/