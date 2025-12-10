package com.example.androidexample.trips;
import com.example.androidexample.R;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import java.util.List;


public class TripChatActivity extends ComponentActivity implements MessageAdapter.Listener {

    // --- Replace these with your real values or pass via Intent extras ---
    private final String baseUrl = "http://coms-3090-026.class.las.iastate.edu:8080"; // REST base (no trailing slash ok)
    private final String baseWsUrl = "ws://coms-3090-026.class.las.iastate.edu:8080";   // WS base
    private long me;
    private MessageAdapter adapter;
    private long tripId;                              // trip/conversation id
    private TripChatViewModel vm;

    private final Map<Long, String> usernameCache = new HashMap<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_chat);

        ImageButton btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> {
            finish(); // Simply close this activity and return to the previous one
        });

        me = getIntent().getLongExtra("userId", -1);
        tripId = getIntent().getLongExtra("tripId", -1);

        if (me == -1 || tripId == -1) {
            Toast.makeText(this, "Missing userId or tripId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView recycler = findViewById(R.id.recycler);
        EditText input = findViewById(R.id.input);
        View send = findViewById(R.id.send);

        // Volley queue (pass into ViewModel via factory)
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // ViewModel with custom factory (provides URLs, ids, and Volley queue)
        vm = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new TripChatViewModel(baseUrl, baseWsUrl, me, tripId, requestQueue);
            }
        }).get(TripChatViewModel.class);

        // RecyclerView setup
        adapter = new MessageAdapter(me);
        adapter.setListener(this);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);                // start list at the bottom like chat apps
        recycler.setLayoutManager(lm);
        recycler.setAdapter(adapter);

        // Observe messages and keep scrolled to the latest
        vm.getMessages().observe(this, (List<TripMessageResponseDTO> list) -> {
            if (list != null) {
                // Fetch usernames for all messages
                for (TripMessageResponseDTO msg : list) {
                    fetchUsernameIfNeeded(msg);
                }
            }

            adapter.submitList(list, () -> {
                if (list != null && !list.isEmpty()) {
                    recycler.scrollToPosition(Math.max(0, list.size() - 1));
                }
            });
        });

        // Send button
        send.setOnClickListener(v -> {
            String text = (input.getText() == null) ? "" : input.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                vm.send(text);
                input.setText("");
            }
        });

        // Infinite scroll: load more when near the top
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                LinearLayoutManager layout = (LinearLayoutManager) rv.getLayoutManager();
                if (layout != null && layout.findFirstVisibleItemPosition() <= 2) {
                    vm.loadMore();
                }
            }
        });
    }

    // get username from get user by id endpoint
    private void fetchUsernameIfNeeded(TripMessageResponseDTO msg) {
        long senderId = msg.getSenderId();

        Log.d("TripChat", "fetchUsernameIfNeeded start");
        Log.d("TripChat", "sender id " + senderId);
        Log.d("TripChat", "current username " + msg.getSenderUsername());

        // skip if we already have the username
        if (msg.getSenderUsername() != null && !msg.getSenderUsername().isEmpty()) {
            Log.d("TripChat", "username already set, skipping");
            return;
        }

        // check local cache first
        if (usernameCache.containsKey(senderId)) {
            Log.d("TripChat", "found in cache " + usernameCache.get(senderId));
            msg.setSenderUsername(usernameCache.get(senderId));
            adapter.notifyDataSetChanged();
            return;
        }

        // fetch the username from the api
        String url = baseUrl + "/api/v2/users/" + senderId;
        Log.d("TripChat", "fetching from url " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("TripChat", "api response received " + response.toString());
                    String username = response.optString("username", "Unknown");
                    Log.d("TripChat", "username " + username);
                    usernameCache.put(senderId, username);
                    msg.setSenderUsername(username);

                    // update the ui after we get the username
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                    });
                },
                error -> {
                    Log.e("TripChat", "failed to fetch username for userId " + senderId);
                    Log.e("TripChat", "error " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("TripChat", "status code " + error.networkResponse.statusCode);
                    }
                    msg.setSenderUsername("Unknown");
                }
        );

        requestQueue.add(request);
        Log.d("TripChat", "request added to queue");
    }

    @Override
    public void onEdit(TripMessageResponseDTO msg) {
        // Simple edit popup
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        EditText edit = new EditText(this);
        edit.setText(msg.getContent());
        b.setTitle("Edit Message");
        b.setView(edit);
        b.setPositiveButton("Save", (d, w) ->
                vm.edit(msg.getId(), edit.getText().toString(), msg.getVersion())
        );
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    @Override
    public void onDelete(TripMessageResponseDTO msg) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Message?")
                .setPositiveButton("Delete", (d, w) ->
                        vm.delete(msg.getId(), msg.getVersion()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onReact(TripMessageResponseDTO msg) {
        // Reaction picker
        String[] emojis = {"👍", "❤️", "😂", "🔥", "🎉", "😮", "😢"};

        new android.app.AlertDialog.Builder(this)
                .setTitle("React")
                .setItems(emojis, (d, which) ->
                        vm.react(msg.getId(), emojis[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onUnreact(TripMessageResponseDTO msg, String emoji) {
        vm.unreact(msg.getId(), emoji);
    }
}