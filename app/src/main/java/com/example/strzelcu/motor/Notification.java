package com.example.strzelcu.motor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

/**
 * Created by Strzelcu on 2016-12-10.
 */

public class Notification {

    NotificationCompat.Builder mBuilder;
    NotificationCompat.Builder notification;
    TaskStackBuilder stackBuilder;
    PendingIntent pIntent;
    Intent resultIntent;
    android.app.NotificationManager mNotificationManager;
    int id = 1;

    public void showNotificationMonitoring(Context context) {
        mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker("Rozpoczynam monitorowanie")
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
        mNotificationManager.notify(id, mBuilder.build());
    }

    public void hideNotification() {
        mNotificationManager.cancelAll();
    }

}
