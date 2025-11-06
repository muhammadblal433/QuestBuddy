package com.example.androidexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity
        implements NotificationsAdapter.OnNotificationActionListener, WebSocketListener {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<NotificationModel> allNotifications = new ArrayList<>();
    private List<NotificationModel> filteredNotifications = new ArrayList<>();
    private RequestQueue queue;

    private int userId;
    private static final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080/api/v7/notifications";
    private static final String WS_URL = "ws://coms-3090-026.class.las.iastate.edu:8080/ws/notifications/";

    private Button btnAll, btnRead;
    private String currentFilter = "ALL";

    // sets up the activity, initializes views, adapters, websocket, and fetches notifications
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAll = findViewById(R.id.btnAll);
        btnRead = findViewById(R.id.btnRead);

        Button btnReturnHome = findViewById(R.id.btnReturnHome);
        btnReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationsActivity.this, HomeActivity.class);

            int userId = getSharedPreferences("session", MODE_PRIVATE).getInt("userId", -1);
            String username = getSharedPreferences("session", MODE_PRIVATE).getString("username", "");

            intent.putExtra("userId", userId);
            intent.putExtra("username", username);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();
        });


        queue = Volley.newRequestQueue(this);

        adapter = new NotificationsAdapter(this, filteredNotifications, this);
        recyclerView.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        int savedId = prefs.getInt("userId", -1);
        userId = getIntent().getIntExtra("userId", savedId);

        if (userId == -1) {
            Toast.makeText(this, "User ID missing. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WebSocketManager wsManager = WebSocketManager.getInstance();
        wsManager.setWebSocketListener(this);

        if (!wsManager.isConnected()) {
            wsManager.connectWebSocket(WS_URL + userId);
        }

        btnAll.setOnClickListener(v -> switchFilter("ALL"));
        btnRead.setOnClickListener(v -> switchFilter("READ"));

        fetchNotifications();
    }

    // removes websocket listener when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketManager.getInstance().removeWebSocketListener();
    }

    // switches between showing all notifications or only read ones
    private void switchFilter(String filter) {
        currentFilter = filter;
        filteredNotifications.clear();

        if (filter.equals("READ")) {
            for (NotificationModel n : allNotifications) {
                if (n.isRead()) filteredNotifications.add(n);
            }
        } else {
            filteredNotifications.addAll(allNotifications);
        }

        adapter.notifyDataSetChanged();
    }

    // retrieves all notifications from the server and updates the display
    private void fetchNotifications() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, BASE_URL, null,
                response -> {
                    allNotifications.clear();
                    parseNotifications(response);
                    switchFilter(currentFilter);
                },
                error -> Toast.makeText(this, "Error fetching notifications", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };
        queue.add(request);
    }

    // parses the json response into notification objects
    private void parseNotifications(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                NotificationModel n = new NotificationModel(
                        obj.getLong("id"),
                        obj.optLong("recipientId"),
                        obj.optString("title"),
                        obj.optString("message"),
                        obj.optString("type"),
                        obj.optString("createdAt"),
                        obj.optBoolean("read")
                );
                allNotifications.add(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // logs when the websocket successfully connects
    @Override
    public void onWebSocketOpen(org.java_websocket.handshake.ServerHandshake handshake) {
        Log.d("WebSocket", "Connected to notifications WebSocket for user " + userId);
    }

    // handles incoming websocket messages for new or read notifications
    @Override
    public void onWebSocketMessage(String message) {
        Log.d("WebSocket", "Received: " + message);
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(message);
                if (!json.has("kind")) return;

                String kind = json.getString("kind");
                if ("NEW".equals(kind)) {
                    NotificationModel n = new NotificationModel(
                            json.getLong("id"),
                            json.optLong("recipientId"),
                            json.optString("title"),
                            json.optString("message"),
                            json.optString("type"),
                            json.optString("createdAt"),
                            json.optBoolean("read")
                    );
                    allNotifications.add(0, n);
                    if (!"READ".equals(currentFilter)) filteredNotifications.add(0, n);
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                    findViewById(R.id.tvNoNotifications).setVisibility(View.GONE);
                } else if ("READ".equals(kind)) {
                    handleReadUpdate(json.getLong("id"));
                }
            } catch (Exception e) {
                Log.e("WebSocket", "Error parsing message", e);
            }
        });
    }

    // marks a notification as read in the local list
    private void handleReadUpdate(long id) {
        for (NotificationModel n : allNotifications) {
            if (n.getId() == id) {
                n.setRead(true);
                break;
            }
        }
        switchFilter(currentFilter);
    }

    @Override public void onWebSocketClose(int code, String reason, boolean remote) {}
    @Override public void onWebSocketError(Exception ex) {}

    // marks a specific notification as read both locally and on the server
    @Override
    public void onMarkAsRead(NotificationModel n) {
        String url = BASE_URL + "/" + n.getId() + "/read";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    n.setRead(true);

                    if (!"READ".equals(currentFilter)) {
                        filteredNotifications.remove(n);
                    }

                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Failed to mark as read", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };
        queue.add(req);
    }

    // deletes a notification from both the server and the local list
    @Override
    public void onDelete(NotificationModel n) {
        String url = BASE_URL + "/" + n.getId();
        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    allNotifications.remove(n);
                    filteredNotifications.remove(n);
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };
        queue.add(req);
    }
}
