package com.swinburne.timur.qup;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.swinburne.timur.qup.queue.Queue;
import com.swinburne.timur.qup.queue.QueueContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueueReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        for (final Queue queue: QueueContent.ITEMS) {
            if (!queue.getParticipantId().equals("")) {
                Log.i("NOTIFICATION", queue.getName());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Queue.BASE_URL + queue.getQueueId(), null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i("VOLLEY", response.toString());
                                try {
                                    if (!response.getBoolean("error")) {
                                        JSONArray participants = response.getJSONArray("participants");
                                        int count;
                                        for (count = 0; count < participants.length() && ! participants.getString(count).equals(queue.getParticipantId()); count++) {
                                            Log.i("COUNT", "Skipping " + participants.getString(count));
                                        }
                                        if (count == 0 && participants.getString(count).equals(queue.getParticipantId())) {
                                            // Create notification
                                            NotificationCompat.Builder mBuilder =
                                                    new NotificationCompat.Builder(context)
                                                            .setSmallIcon(R.drawable.notification)
                                                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification))
                                                            .setColor(context.getResources().getColor(R.color.colorAccent))
                                                            .setAutoCancel(true)
                                                            .setContentTitle(context.getString(R.string.app_name))
                                                            .setContentText(context.getString(R.string.text_turn));

                                            // Open notification detail on press
                                            Intent resultIntent = new Intent(context, QueueDetailActivity.class);
                                            resultIntent.putExtra(QueueDetailFragment.ARG_ITEM_ID, queue.getId());
                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                            stackBuilder.addParentStack(QueueDetailActivity.class);
                                            stackBuilder.addNextIntent(resultIntent);
                                            PendingIntent resultPendingIntent =
                                                    stackBuilder.getPendingIntent(
                                                            0,
                                                            PendingIntent.FLAG_UPDATE_CURRENT
                                                    );
                                            mBuilder.setContentIntent(resultPendingIntent);
                                            NotificationManager mNotificationManager =
                                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                            // Use separate notification queue IDs so each shows separately
                                            mNotificationManager.notify(Integer.valueOf(queue.getId()), mBuilder.build());
                                        }
                                    }
                                } catch (JSONException e) {
                                    Log.e("JSON", e.toString());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("VOLLEY", error.toString());
                            }
                        }
                );
                requestQueue.add(jsonObjectRequest);
            }
        }
    }
}