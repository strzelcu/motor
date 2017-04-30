package com.tomaszstrzelecki.motor.phonehandle;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.AppService;

import java.util.Objects;

public class ReceiveSMSHandler extends BroadcastReceiver {

    private static String senderPhoneNumber;

    @Override
    public void onReceive(Context context, Intent intent) {

        final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
        final String TAG = "ReceiveSMSHandler";

        if (Objects.equals(intent.getAction(), SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }
                if (messages.length > -1) {
                    senderPhoneNumber = messages[0].getOriginatingAddress();
                }
                if (AppService.isMonitorOn) {
                    Toast.makeText(context, "Wysyłam wiadomość automatyczną", Toast.LENGTH_LONG).show();
                    String message = "Jadę motocyklem i nie mogę odpisać.";
                    SmsProvider.sendSMS(senderPhoneNumber, message, context);
                }
            }
        }
    }
}
