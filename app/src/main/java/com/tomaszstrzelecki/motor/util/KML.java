package com.tomaszstrzelecki.motor.util;


import android.util.Log;
import android.util.Xml;

import com.tomaszstrzelecki.motor.track.Waypoint;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.ArrayList;

public class KML {

    String name;
    String description;
    private ArrayList<Waypoint> waypoints;

    public KML(String name, String description, ArrayList<Waypoint> waypoints) {
        this.name = name;
        this.description = description + " (Wygenerowano w aplikacji Motor - Motorcyclist Rescue)";
        this.waypoints = waypoints;

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

                serializer.endTag("", "Document");
            serializer.endTag("", "kml");
        } catch (Exception e) {
            Log.e("KML", "There is some problem with making KML");
        }
        return writer.toString();
    }

    public void saveKMLFile() {

    }
}
