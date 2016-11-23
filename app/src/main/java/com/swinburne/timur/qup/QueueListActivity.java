package com.swinburne.timur.qup;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.swinburne.timur.qup.queue.Queue;
import com.swinburne.timur.qup.queue.QueueContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * An activity representing a list of Queues. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link QueueDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class QueueListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SimpleItemRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        com.getbase.floatingactionbutton.FloatingActionButton fab = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), QueueCreateActivity.class);
                ((Activity) view.getContext()).startActivityForResult(intent, 1);
            }
        });

        com.getbase.floatingactionbutton.FloatingActionButton fabParticipate = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_participate);
        fabParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), QueueParticipateActivity.class);
                ((Activity) view.getContext()).startActivityForResult(intent, 1);
            }
        });

        View recyclerView = findViewById(R.id.queue_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.queue_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        this.adapter = new SimpleItemRecyclerViewAdapter(QueueContent.ITEMS);
        recyclerView.setAdapter(this.adapter);
    }

    /**
     * Update adapter whenever an intent returns
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.adapter != null)
            this.adapter.update();
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private List<Queue> mValues;

        public SimpleItemRecyclerViewAdapter(List<Queue> items) {
            mValues = items;
        }

        /**
         * Update list contents
         */
        public void update() {
            mValues = QueueContent.ITEMS;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.queue_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mContentView.setText(mValues.get(position).getName());

            if (holder.mItem.getParticipantId().equals("")) {
                holder.mCard.setCardBackgroundColor(getColor(R.color.colorAccentCyan));
            } else {
                holder.mCard.setCardBackgroundColor(getColor(R.color.colorAccentTeal));
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(QueueDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                        QueueDetailFragment fragment = new QueueDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.queue_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, QueueDetailActivity.class);
                        intent.putExtra(QueueDetailFragment.ARG_ITEM_ID, holder.mItem.getId());

                        context.startActivity(intent);
                    }
                }
            });

            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle(R.string.confirm_deletion_title);
                    builder.setMessage(R.string.confirm_deletion_text);

                    final Context context = v.getContext();

                    // Dialog action handlers
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (! holder.mItem.getParticipantId().equals("")) {
                                // Package data
                                HashMap<String, String> postData = new HashMap();
                                postData.put("token", holder.mItem.getToken());
                                JSONObject postJSONData = new JSONObject(postData);

                                RequestQueue requestQueue = Volley.newRequestQueue(context);
                                Log.i("VOLLEY", Queue.UNPARTICIPATE_URL + holder.mItem.getParticipantId());
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Queue.UNPARTICIPATE_URL + holder.mItem.getParticipantId(), postJSONData,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                Log.i("VOLLEY", response.toString());
                                                try {
                                                    if (!response.getBoolean("error")) {
                                                        // Remove Address based on ID
                                                        QueueContent.removeItem(holder.mItem.getId());
                                                        update();
                                                    }
                                                    Toast toast = Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT);
                                                    toast.show();
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
                                // Remove Address based on ID
                                QueueContent.removeItem(holder.mItem.getId());
                                update();
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });

                    // Create dialog
                    AlertDialog deleteDialog = builder.create();
                    deleteDialog.show();
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mContentView;
            public final CardView mCard;
            public Queue mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = (TextView) view.findViewById(R.id.content);
                mCard = (CardView) view.findViewById(R.id.cardItem);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
