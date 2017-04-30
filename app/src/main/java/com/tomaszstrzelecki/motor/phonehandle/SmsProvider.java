package com.tomaszstrzelecki.motor.phonehandle;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.tomaszstrzelecki.motor.gpshandle.GpsService;
import java.util.ArrayList;


public class SmsProvider {

    public static void sendSMS(String phoneNumber, String message, Context context) {

        String SENT = "SMS_SENT";
        int MAX_SMS_MESSAGE_LENGTH = 70;


        if (GpsService.latitude != 0 && GpsService.longitude != 0) {
            //TODO Warunek z ustawień, czy ma być dodawana lokalizacja do wiadomości
            message += " Aktualnie znajduję się tutaj: http://www.google.com/maps/place/" +
                    String.valueOf(GpsService.latitude) + "," + String.valueOf(GpsService.longitude);
        }
        message += " (MotoRAppBeta)";

        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentPI;
        sentPI = PendingIntent.getBroadcast(context, 0,new Intent(SENT), 0);

        try {
            if(message.length() > MAX_SMS_MESSAGE_LENGTH) {
                ArrayList<String> messageList = SmsManager.getDefault().divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, messageList, null, null);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null);
            }
        } catch (Exception e) {
            Log.e("SmsProvider", "" + e);
        }

    }

    public static void sendAlarmMessage() {

    }
}
