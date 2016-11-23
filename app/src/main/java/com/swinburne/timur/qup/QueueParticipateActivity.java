package com.swinburne.timur.qup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.swinburne.timur.qup.queue.Queue;
import com.swinburne.timur.qup.queue.QueueContent;

import org.json.JSONException;
import org.json.JSONObject;

public class QueueParticipateActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_participate);

        findViewById(R.id.buttonScan).setOnClickListener(this);
        findViewById(R.id.buttonParticipate).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonScan) {
            // Scanning activity
            new IntentIntegrator(this).initiateScan();
        } else {
            // Participate activity
            final String queueId = ((EditText) findViewById(R.id.editTextQueueId)).getText().toString();
            final String name = ((EditText) findViewById(R.id.editTextQueueName)).getText().toString();

            if (name.length() > 0 && queueId.length() > 23) {
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Queue.PARTICIPATE_URL + queueId, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i("VOLLEY", response.toString());
                                try {
                                    if (!response.getBoolean("error")) {
                                        Queue queue = new Queue(response.getString("queueId"), response.getString("_id"), name, response.getString("token"));
                                        QueueContent.addItem(queue);
                                        finish();
                                    } else {
                                        Toast toast = Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT);
                                        toast.show();
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
            } else {
                Toast toast = Toast.makeText(this, getString(R.string.text_create_error), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getString(R.string.text_scan_error), Toast.LENGTH_SHORT).show();
            } else {
                ((EditText) findViewById(R.id.editTextQueueId)).setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
