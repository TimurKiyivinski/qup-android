package com.swinburne.timur.qup;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.swinburne.timur.qup.queue.Queue;
import com.swinburne.timur.qup.queue.QueueContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class QueueCreateActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_create);

        findViewById(R.id.buttonCreate).setOnClickListener(this);
        findViewById(R.id.editTextToken).setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final String name = ((EditText) findViewById(R.id.editTextName)).getText().toString();
        final String token = ((EditText) findViewById(R.id.editTextToken)).getText().toString();

        if (name.length() > 0 && token.length() > 0) {
            // Package data
            HashMap<String, String> postData = new HashMap();
            postData.put("name", name);
            postData.put("token", token);
            JSONObject postJSONData = new JSONObject(postData);

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Queue.BASE_URL, postJSONData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("VOLLEY", response.toString());
                            try {
                                if (!response.getBoolean("error")) {
                                    Queue queue = new Queue(response.getString("queue"), "", name, token);
                                    QueueContent.addItem(queue);
                                }
                                Toast toast = Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT);
                                toast.show();
                                finish();
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
            queue.add(jsonObjectRequest);
        } else {
            Toast toast = Toast.makeText(this, getString(R.string.text_create_error), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        ((EditText) findViewById(R.id.editTextToken)).setText(UUID.randomUUID().toString());
        return false;
    }
}
