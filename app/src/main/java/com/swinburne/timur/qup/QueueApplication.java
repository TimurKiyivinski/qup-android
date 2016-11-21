package com.swinburne.timur.qup;

import android.app.Application;

import com.swinburne.timur.qup.queue.QueueContent;

public class QueueApplication extends Application {
    @Override
    public void onCreate() {
        QueueContent.setContext(this);
    }
}
