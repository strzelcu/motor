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

    private Context context;
    private NotificationCompat.Builder mBuilder;
    private android.app.NotificationManager mNotificationManager;
    private int id = 1;

    public Notifications(Context context) {
        this.context = context;
    }

    public void showToastMsg(String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public void showNotificationMonitoring() {
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

    void hideAllNotifications() {
        hideNotification();
    }
}
