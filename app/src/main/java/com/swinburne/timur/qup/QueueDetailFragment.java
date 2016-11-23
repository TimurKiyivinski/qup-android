package com.swinburne.timur.qup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.swinburne.timur.qup.queue.Queue;
import com.swinburne.timur.qup.queue.QueueContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A fragment representing a single Queue detail screen.
 * This fragment is either contained in a {@link QueueListActivity}
 * in two-pane mode (on tablets) or a {@link QueueDetailActivity}
 * on handsets.
 */
public class QueueDetailFragment extends Fragment implements View.OnClickListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The queue content this fragment is presenting.
     */
    private Queue mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QueueDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the queue content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = QueueContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.queue_detail, container, false);

        // Show the queue content as text in a TextView.
        if (mItem != null) {
            // QR holders
            final ImageView qrView = (ImageView) rootView.findViewById(R.id.queue_qr);
            final ImageView qrParticipantView = (ImageView) rootView.findViewById(R.id.queue_participant_qr);

            final TextView textTitle = (TextView) rootView.findViewById(R.id.queue_title);
            final TextView textView = (TextView) rootView.findViewById(R.id.queue_detail);
            final TextView textCurrentHeader = (TextView) rootView.findViewById(R.id.textViewParticipantId);
            final View nextButton = rootView.findViewById(R.id.buttonNextParticipant);

            nextButton.setOnClickListener(this);

            // Run QR generation in separate thread to avoid hogging main thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Populate QR code area
                    QRCodeWriter writer = new QRCodeWriter();
                    try {
                        // Create QR bitmap
                        BitMatrix bitMatrix = writer.encode(mItem.getQueueId(), BarcodeFormat.QR_CODE, 512, 512);
                        int width = bitMatrix.getWidth();
                        int height = bitMatrix.getHeight();
                        final Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                if (isAdded())
                                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : getResources().getColor(R.color.colorMain));
                            }
                        }

                        if (isAdded()) {
                            // Update qrView back on UI thread
                            qrView.post(new Runnable() {
                                @Override
                                public void run() {
                                    qrView.setImageBitmap(qrBitmap);
                                    qrView.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                            android.content.ClipData clip = android.content.ClipData.newPlainText("QUEUE_ID", mItem.getQueueId());
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(getContext(), getString(R.string.clipboard), Toast.LENGTH_LONG).show();
                                            return false;
                                        }
                                    });
                                }
                            });
                        }

                    } catch (WriterException e) {
                        Log.e("QR", e.toString());
                    } catch (IllegalStateException e) {
                        Log.e("QR", e.toString());
                    }
                }
            }).start();

            // If user is participant, show QR otherwise change text header
            if (!mItem.getParticipantId().equals("")) {
                // Run participant QR generation in separate thread to avoid hogging main thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Populate QR code area
                        QRCodeWriter writer = new QRCodeWriter();
                        try {
                            // Create QR bitmap
                            BitMatrix bitMatrix = writer.encode(mItem.getParticipantId(), BarcodeFormat.QR_CODE, 512, 512);
                            int width = bitMatrix.getWidth();
                            int height = bitMatrix.getHeight();
                            final Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                            for (int x = 0; x < width; x++) {
                                for (int y = 0; y < height; y++) {
                                    if (isAdded())
                                        qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : getResources().getColor(R.color.colorMain));
                                }
                            }

                            if (isAdded()) {
                                // Update qrView back on UI thread
                                qrParticipantView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        qrParticipantView.setImageBitmap(qrBitmap);
                                        qrParticipantView.setVisibility(View.VISIBLE);
                                        textCurrentHeader.setVisibility(View.VISIBLE);
                                    }
                                });
                            }

                        } catch (WriterException e) {
                            Log.e("QR", e.toString());
                        } catch (IllegalStateException e) {
                            Log.e("QR", e.toString());
                        }
                    }
                }).start();
            } else {
                textCurrentHeader.setText(getString(R.string.current_qr));
            }

            // Update content
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Queue.BASE_URL + mItem.getQueueId(), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("VOLLEY", response.toString());
                            try {
                                if (!response.getBoolean("error")) {
                                    // Get participants
                                    JSONArray participants = response.getJSONArray("participants");
                                    if (mItem.getParticipantId().equals("")) {
                                        textTitle.setText(getString(R.string.text_remaining));
                                        textView.setText(String.valueOf(participants.length()));
                                        if (response.has("current")) {
                                            final String currentId = response.getString("current");

                                            // Run current QR generation in separate thread to avoid hogging main thread
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // Populate QR code area
                                                    QRCodeWriter writer = new QRCodeWriter();
                                                    try {
                                                        // Create QR bitmap
                                                        BitMatrix bitMatrix = writer.encode(currentId, BarcodeFormat.QR_CODE, 512, 512);
                                                        int width = bitMatrix.getWidth();
                                                        int height = bitMatrix.getHeight();
                                                        final Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                                                        for (int x = 0; x < width; x++) {
                                                            for (int y = 0; y < height; y++) {
                                                                if (isAdded())
                                                                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : getResources().getColor(R.color.colorMain));
                                                            }
                                                        }

                                                        if (isAdded()) {
                                                            // Update qrView back on UI thread
                                                            qrParticipantView.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    qrParticipantView.setImageBitmap(qrBitmap);
                                                                    qrParticipantView.setVisibility(View.VISIBLE);
                                                                    textCurrentHeader.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                        }

                                                    } catch (WriterException e) {
                                                        Log.e("QR", e.toString());
                                                    } catch (IllegalStateException e) {
                                                        Log.e("QR", e.toString());
                                                    }
                                                }
                                            }).start();
                                        } else {
                                            Toast.makeText(getContext(), getString(R.string.no_current), Toast.LENGTH_LONG).show();
                                        }
                                        nextButton.setVisibility(View.VISIBLE);
                                    } else {
                                        int count;
                                        for (count = 0; count < participants.length() && ! participants.getString(count).equals(mItem.getParticipantId()); count++) {
                                            Log.i("COUNT", "Skipping " + participants.getString(count));
                                        }
                                        Log.i("COUNT", String.valueOf(count));
                                        if (count == 0 && participants.getString(count).equals(mItem.getParticipantId())) {
                                            Log.i("COUNT", "User turn");
                                            textTitle.setText(getString(R.string.text_turn));
                                            textView.setText(String.valueOf(count));
                                        } else if (count == participants.length()){
                                            Log.i("COUNT", "User missed");
                                            textTitle.setText(getString(R.string.text_missed));
                                        } else if (count > 0) {
                                            Log.i("COUNT", "User wait");
                                            textTitle.setText(getString(R.string.text_before));
                                            textView.setText(String.valueOf(count));
                                        }
                                    }
                                } else {
                                    // Toast error message
                                    Toast toast = Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_LONG);
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
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {

        final Activity activity = this.getActivity();

        HashMap<String, String> postData = new HashMap();
        postData.put("token", mItem.getToken());
        JSONObject postJSONData = new JSONObject(postData);

        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Queue.BASE_URL + mItem.getQueueId(), postJSONData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("VOLLEY", response.toString());
                        try {
                            if (!response.getBoolean("error")) {
                                // Recreate intent as a refresh method
                                Intent intent = activity.getIntent();
                                activity.finish();
                                startActivity(intent);
                            } else {
                                Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
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
