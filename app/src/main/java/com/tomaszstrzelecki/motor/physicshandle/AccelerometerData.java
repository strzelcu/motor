package com.tomaszstrzelecki.motor.physicshandle;


import android.os.Environment;
import android.util.Log;

import com.tomaszstrzelecki.motor.util.DateStamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class AccelerometerData {

    private StringBuffer data;

    AccelerometerData() {
        data = new StringBuffer();
        data.append("X-axis;Y-axis;Z-axis\n");
    }

    void addData(Float x, Float y, Float z){
        String record = x + ";" + y + ";" + z + "\n";
        data.append(record);
    }

    void saveData(){

        String fileName = DateStamp.getStringDateTime().toLowerCase().replace(" ", "_") + "_acc.csv";
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
                    Log.e("KML", "Something happend while saving acc.csv file");
                }
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
