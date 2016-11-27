package com.swinburne.timur.qup;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.swinburne.timur.qup.queue.QueueContent;

import static com.swinburne.timur.qup.queue.QueueContent.context;

public class QueueApplication extends Application {

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    public void onCreate() {
        QueueContent.setContext(this);
        // Set alarm manager to check for updates periodically
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, QueueReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        // Inexact repeating alarm to conserve battery life
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
    }
}
