package com.tomaszstrzelecki.motor.util;


import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.track.Waypoint;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import static android.R.attr.path;
import static android.content.Context.MODE_PRIVATE;

public class KML {

    private String name;
    private String description;
    private ArrayList<Waypoint> waypoints;
    private Context context;

    public KML(String name, ArrayList<Waypoint> waypoints, Context applicationContext) {
        this.name = name;
        description = name + " (Wygenerowano w aplikacji Motor - Motorcyclist Rescue - " +
                    DateStamp.getStringDateTime() + ")";
        this.waypoints = waypoints;
        context = applicationContext;
    }

    public String makeKMLFile() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", false);
            serializer.startTag("", "kml");
            serializer.attribute("", "xmlns", "http://www.opengis.net/kml/2.2");
            serializer.startTag("", "Document");
            serializer.startTag("", "name");
            serializer.text(name);
            serializer.endTag("", "name");
            serializer.startTag("", "description");
            serializer.text(description);
            serializer.endTag("", "description");
            serializer.startTag("", "Style");
            serializer.attribute("", "id", "4dpBluePolyline");
            serializer.startTag("", "LineStyle");
            serializer.startTag("", "color");
            serializer.text("50F06414");
            serializer.endTag("", "color");
            serializer.startTag("", "width");
            serializer.text("4");
            serializer.endTag("", "width");
            serializer.endTag("", "LineStyle");
            serializer.startTag("", "PolyStyle");
            serializer.startTag("", "color");
            serializer.text("50F06414");
            serializer.endTag("", "color");
            serializer.endTag("", "PolyStyle");
            serializer.endTag("", "Style");

            serializer.startTag("", "Placemark");
            serializer.startTag("", "name");
            serializer.text("Start trasy");
            serializer.endTag("", "name");
            serializer.startTag("", "description");
            serializer.text(description);
            serializer.endTag("", "description");
            serializer.startTag("", "Point");
            serializer.startTag("", "coordinates");
            serializer.text(waypoints.get(0).getLongitude() + "," + waypoints.get(0).getLatitude());
            serializer.endTag("", "coordinates");
            serializer.endTag("", "Point");
            serializer.endTag("", "Placemark");

            serializer.startTag("", "Placemark");
            serializer.startTag("", "name");
            serializer.text("Trasa");
            serializer.endTag("", "name");
            serializer.startTag("", "description");
            serializer.text(description);
            serializer.endTag("", "description");
            serializer.startTag("", "styleUrl");
            serializer.text("#4dpBluePolyline");
            serializer.endTag("", "styleUrl");
            serializer.startTag("", "LineString");
            serializer.startTag("", "extrude");
            serializer.text("1");
            serializer.endTag("", "extrude");
            serializer.startTag("", "tessellate");
            serializer.text("1");
            serializer.endTag("", "tessellate");
            serializer.startTag("", "altitudeMode");
            serializer.text("absolute");
            serializer.endTag("", "altitudeMode");
            serializer.startTag("", "coordinates");
            for (Waypoint waypoint : waypoints) {
                serializer.text("\n" + waypoint.getLongitude() + "," + waypoint.getLatitude());
            }
            serializer.endTag("", "coordinates");
            serializer.endTag("", "LineString");
            serializer.endTag("", "Placemark");

            serializer.startTag("", "Placemark");
            serializer.startTag("", "name");
            serializer.text("Koniec trasy");
            serializer.endTag("", "name");
            serializer.startTag("", "description");
            serializer.text(description);
            serializer.endTag("", "description");
            serializer.startTag("", "Point");
            serializer.startTag("", "coordinates");
            serializer.text(waypoints.get(waypoints.size()-1).getLongitude() + "," + waypoints.get(waypoints.size()-1).getLatitude());
            serializer.endTag("", "coordinates");
            serializer.endTag("", "Point");
            serializer.endTag("", "Placemark");

            serializer.endTag("", "Document");
            serializer.endTag("", "kml");
            serializer.endDocument();
        } catch (Exception e) {
            Log.e("KML", "There is some problem with making KML: " + e);
        }
        return writer.toString();
    }

    public File saveFile(String source) {

        String fileName = name.toLowerCase().replace(" ", "_") + ".kml";
        File file = null;

        if(isExternalStorageWritable()) {

            File path = new File(Environment.getExternalStorageDirectory() + File.separator + "Motor", "tracks");
            file = new File(path, fileName);

            if(!path.exists()) {
                path.mkdirs();
            }

            if(!file.exists()) {
                try {
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(source.getBytes());
                    fileOutputStream.close();
                    Toast.makeText(context.getApplicationContext(), "Zapisano KML w /Motor/tracks", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("KML", "Somethig happen while saving KML file");
                }
            }
        }
        return file;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
