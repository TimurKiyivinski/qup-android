package com.swinburne.timur.qup.queue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class QueueContent {

    /**
     * An array of sample (queue) items.
     */
    public static final List<Queue> ITEMS = new ArrayList<Queue>();
    public static Context context;

    /**
     * A map of sample (queue) items, by ID.
     */
    public static final Map<String, Queue> ITEM_MAP = new HashMap<String, Queue>();

    private static final int COUNT = 25;

    /**
     * Set global application context and load database
     * @param context
     */
    public static void setContext(Context context) {
        QueueContent.context = context;
        // Load values
        QueueDbHelper dbHelper = new QueueDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Execute query
        Cursor cursor = db.rawQuery("SELECT * FROM QUEUE", null);
        cursor.moveToFirst();

        // Get all results
        if (cursor.moveToFirst()) {
            do {
                int id = (int) cursor.getLong(0);
                String queueId = cursor.getString(1);
                String participantId = cursor.getString(2);
                String name = cursor.getString(3);
                String token = cursor.getString(4);
                Queue queue = new Queue(id, queueId, participantId, name, token);
                Log.d("Queue", queue.toString());
                addItem(queue);
            } while (cursor.moveToNext());
            cursor.close();
            db.close();
        }
    }

    /**
     * Adds Queue to SQLite database and local context
     * @param item Queue item
     */
    public static void addItem(Queue item) {
        // Only insert into database if ID is not initialized
        if (item.getId().equals("0")) {
            QueueDbHelper dbHelper = new QueueDbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("queue_id", item.getQueueId());
            values.put("participant_id", item.getParticipantId());
            values.put("name", item.getName());
            values.put("token", item.getToken());

            // Insert row
            long newID = db.insert("QUEUE", null, values);
            db.close();
            item.setId((int) newID); // Nobody will reach a 64 bit count of contacts o.o
            Log.d("CREATE", "New Queue with ID: " + valueOf(newID));
        }

        // Update local store
        ITEMS.add(item);
        ITEM_MAP.put(item.getId().toString(), item);
    }

    /**
     * Updates Queue SQLite item and local context based on ID
     * @param id Queue item ID
     * @param item Queue item
     */
    public static void updateItem(String id, Queue item) {
        Log.d("UPDATE", id + ": " + item.toString());
        QueueDbHelper dbHelper = new QueueDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("queue_id", item.getQueueId());
        values.put("participant_id", item.getParticipantId());
        values.put("name", item.getName());
        values.put("token", item.getToken());

        String selection = "id =? ";
        String[] selectionArgs = { id };

        int count = db.update("QUEUE", values, selection, selectionArgs);
        db.close();
        Log.d("UPDATE", valueOf(count) + " rows updated");

        item.setId(Integer.valueOf(id));
        ITEM_MAP.put(id, item);
        for (int i = 0; i < ITEMS.size(); i++) {
            Queue mValue = ITEMS.get(i);
            if (mValue.getId().toString().equals(id)) {
                ITEMS.set(i, item);
            }
        }
    }

    /**
     * Remove Queue item from SQLite and local context based on ID
     * @param id Queue item ID
     */
    public static void removeItem(String id) {
        QueueDbHelper dbHelper = new QueueDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = "id =? ";
        String[] selectionArgs = { id };

        db.delete("QUEUE", selection, selectionArgs);
        ITEM_MAP.remove(id);
        for (int i = 0; i < ITEMS.size(); i++) {
            Queue mValue = ITEMS.get(i);
            if (mValue.getId().toString().equals(id)) {
                Log.d("REMOVE", mValue.toString());
                ITEMS.remove(i);
            }
        }
    }
}
