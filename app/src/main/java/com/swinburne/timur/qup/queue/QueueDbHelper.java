package com.swinburne.timur.qup.queue;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class QueueDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Queues.db";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE QUEUE "
            + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "QUEUE_ID TEXT, PARTICIPANT_ID TEXT, "
            + "NAME TEXT, TOKEN TEXT)";

    public QueueDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Ignore
    }
}
