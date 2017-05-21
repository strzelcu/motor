package com.tomaszstrzelecki.motor.phonehandle;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CharArrayBuffer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.R;
import com.tomaszstrzelecki.motor.gpshandle.GpsService;
import java.util.ArrayList;

import static android.R.attr.phoneNumber;
import static com.tomaszstrzelecki.motor.phonehandle.SmsProvider.replacePolishSigns;


public class SmsProvider {

    public static void sendSMS(String phoneNumber, String message, Context context) {

        String SENT = "SMS_SENT";
        int MAX_SMS_MESSAGE_LENGTH = 70;
        boolean isOff = false;
        boolean addLocalization = true;
        boolean addSignature = false;
        boolean removePolishSigns = false;
        String TAG = "SmsProvider";
        String signature = "";

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(!sharedPreferences.getBoolean("auto_messages_auto_send", isOff)){
            return;
        } else {
            Toast.makeText(context, "Wysyłam wiadomość automatyczną", Toast.LENGTH_LONG).show();
        }
        addLocalization = sharedPreferences.getBoolean("auto_messages_add_localization", addLocalization);
        signature = sharedPreferences.getString("auto_messages_signature", signature);
        removePolishSigns = sharedPreferences.getBoolean("auto_messages_remove_polish_signs", removePolishSigns);
        addSignature = sharedPreferences.getBoolean("auto_messages_add_signature", addSignature);

        Log.i(TAG, "Add Localization preference = " + addLocalization);
        if (GpsService.latitude != 0 && GpsService.longitude != 0 && addLocalization) {
            message += " Aktualnie znajduję się tutaj: http://www.google.com/maps/place/" +
                    String.valueOf(GpsService.latitude) + "," + String.valueOf(GpsService.longitude);
        }

        // Add user signature
        if(addSignature){
            if(signature.length() > 3 && !signature.equals(context.getResources().getString(R.string.default_signature))){
                message += " " + signature;
            }
        }

        // Add app stamp
        message += " " + context.getResources().getString(R.string.app_short_description);

        if(removePolishSigns){
            message = replacePolishSigns(message);
        }

        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentPI;
        sentPI = PendingIntent.getBroadcast(context, 0,new Intent(SENT), 0);

        try {
            Log.i("SmsProvider", "Auto message is send");
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

    public static void sendAlarmMessage(Context context) {

        String SENT = "SMS_SENT";
        int MAX_SMS_MESSAGE_LENGTH = 70;
        boolean removePolishSigns = false;
        String TAG = "SmsProvider";
        String signature = "";
        String phoneNumber = "";

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        signature = sharedPreferences.getString("auto_messages_signature", signature);
        removePolishSigns = sharedPreferences.getBoolean("auto_messages_remove_polish_signs", removePolishSigns);
        phoneNumber = sharedPreferences.getString("physics_telephone_number", phoneNumber);

        String message = "Coś jest nie tak. Spróbuj się ze mną skontaktować.";
        message += " Aktualnie znajduję się tutaj:";
        message += " http://www.google.com/maps/place/" +
                String.valueOf(GpsService.latitude) + "," + String.valueOf(GpsService.longitude);
        message += " " + signature;
        message += " " + context.getResources().getString(R.string.app_short_description);

        if(removePolishSigns){
            message = replacePolishSigns(message);
        }

        Log.i(TAG, "Alarm message is send");

        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentPI;
        sentPI = PendingIntent.getBroadcast(context, 0,new Intent(SENT), 0);

        try {
            Log.i("SmsProvider", "Alarm message is send");
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

    @NonNull
    static String replacePolishSigns(String message) {

        char[] buffer = message.toCharArray();
        StringBuilder result = new StringBuilder();

        for (char c : buffer) {
            switch (c) {
                case 'ą': {
                    c = 'a';
                    break;
                }
                case 'ć': {
                    c = 'c';
                    break;
                }
                case 'ę': {
                    c = 'e';
                    break;
                }
                case 'ł': {
                    c = 'l';
                    break;
                }
                case 'ń': {
                    c = 'n';
                    break;
                }
                case 'ó': {
                    c = 'o';
                    break;
                }
                case 'ś': {
                    c = 's';
                    break;
                }
                case 'ż': {
                    c = 'z';
                    break;
                }
                case 'ź': {
                    c = 'z';
                    break;
                }
                case 'Ą': {
                    c = 'A';
                    break;
                }
                case 'Ć': {
                    c = 'C';
                    break;
                }
                case 'Ę': {
                    c = 'E';
                    break;
                }
                case 'Ł': {
                    c = 'L';
                    break;
                }
                case 'Ń': {
                    c = 'N';
                    break;
                }
                case 'Ó': {
                    c = 'O';
                    break;
                }
                case 'Ś': {
                    c = 'S';
                    break;
                }
                case 'Ż': {
                    c = 'Z';
                    break;
                }
                case 'Ź': {
                    c = 'Z';
                    break;
                }
            }
            result.append(c);
        }
        return result.toString();
    }
}
