package com.tomaszstrzelecki.motor.physicshandle;

import android.os.Environment;
import android.util.Log;

import com.tomaszstrzelecki.motor.util.DateStamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class GyroscopeData {

    private StringBuffer data;
    private int id = 1;
    private String date;

    GyroscopeData() {
        data = new StringBuffer();
        date = DateStamp.getStringDateTime().toLowerCase().replace(" ", "_");
    }

    void addData(Float x, Float y, Float z){
        String record = id + "," + x + "," + y + "," + z + "\n";
        data.append(record);
    }

    void saveData(){

        String fileName = date + "_gyr.csv";
        File file;

        if(isExternalStorageWritable()) {

            File path = new File(Environment.getExternalStorageDirectory() + File.separator + "Motor", "physics");
            file = new File(path, fileName);

            if(!path.exists()) {
                path.mkdirs();
            }

            if(!file.exists()) {
                try {
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(data.toString().getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e("KML", "Something happend while saving gyr.csv file");
                }
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}