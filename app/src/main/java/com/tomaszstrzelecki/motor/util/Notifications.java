package com.tomaszstrzelecki.motor.util;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.tomaszstrzelecki.motor.MainActivity;
import com.tomaszstrzelecki.motor.R;

public class Notifications {


    private static android.app.NotificationManager mNotificationManager;

    public static void showToastMsg(String msg, Context context) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showLongToastMsg(String msg, Context context) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void showNotificationMonitoring(Context context) {
        mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Monitoruję jazdę.")
                .setContentTitle("MotoR")
                .setContentText("Monitoruję jazdę.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        int id = 1;
        mNotificationManager.notify(id, mBuilder.build());
    }

    public static void hideNotification() {
        if(mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }
}
