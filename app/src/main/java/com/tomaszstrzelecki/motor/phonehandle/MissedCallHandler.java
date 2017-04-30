package com.tomaszstrzelecki.motor.phonehandle;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.AppService;

public class MissedCallHandler extends BroadcastReceiver {

    static boolean isRinging = false;
    static boolean isReceived = false;
    static String callerPhoneNumber;

    @Override
    public void onReceive(Context context, Intent intent){

        // Get current phone state
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if(state==null)
            return;

        //phone is ringing
        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
            isRinging =true;
            //get caller's number
            Bundle bundle = intent.getExtras();
            callerPhoneNumber= bundle.getString("incoming_number");
        }

        //phone is received
        if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
            isReceived=true;
        }

        // phone is idle
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
            // detect missed call
            if(isRinging && !isReceived){
                Toast.makeText(context, "Wysyłam wiadomość automatyczną", Toast.LENGTH_LONG).show();
                if (AppService.isMonitorOn) {
                    String message = "Jadę motocyklem i nie mogę odebrać.";
                    SmsProvider.sendSMS(callerPhoneNumber, message, context);
                }
            }
        }
    }
}