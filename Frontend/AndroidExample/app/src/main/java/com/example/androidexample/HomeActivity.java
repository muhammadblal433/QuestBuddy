package com.example.androidexample;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import org.json.JSONObject;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import com.nex3z.notificationbadge.NotificationBadge;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private String[] drawerItems;

    private int userId;
    private NotificationBadge notificationBadge;
    private int notificationCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get userId passed from LoginActivity
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.left_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);


        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");
        loadUserProfile(userId);

        // Enable home button as drawer toggle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        notificationBadge = findViewById(R.id.badge);
        ImageView btnNotifications = findViewById(R.id.btnNotifications);
        btnNotifications.setOnClickListener(v -> {
            // Reset badge when opening notifications
            notificationCount = 0;
            notificationBadge.setNumber(0);
            Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        WebSocketManager.getInstance().setWebSocketListener(new WebSocketListener() {
            @Override
            public void onWebSocketOpen(org.java_websocket.handshake.ServerHandshake handshake) {}

            @Override
            public void onWebSocketMessage(String message) {
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(message);
                        String title = json.optString("title", "New Notification");
                        String body = json.optString("message", "");

                        Toast.makeText(
                                HomeActivity.this,
                                "🔔 " + title + (body.isEmpty() ? "" : ": " + body),
                                Toast.LENGTH_LONG
                        ).show();

                    } catch (org.json.JSONException e) {
                        Toast.makeText(
                                HomeActivity.this,
                                "🔔 New notification received!",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    notificationCount++;
                    notificationBadge.setNumber(notificationCount);

                    ImageView bell = findViewById(R.id.btnNotifications);
                    bell.animate().rotation(15).setDuration(120)
                            .withEndAction(() -> bell.animate().rotation(0).setDuration(120))
                            .start();

                    getSharedPreferences("session", MODE_PRIVATE)
                            .edit()
                            .putInt("badgeCount", notificationCount)
                            .apply();
                });
            }


            @Override public void onWebSocketClose(int code, String reason, boolean remote) {}
            @Override public void onWebSocketError(Exception ex) {}
        });

        if (!WebSocketManager.getInstance().isConnected()) {
            WebSocketManager.getInstance()
                    .connectWebSocket("ws://coms-3090-026.class.las.iastate.edu:8080/ws/notifications/" + userId);
        }


        // Add items (ex. Home, Profile, etc) into the navigation bar
        drawerItems = getResources().getStringArray(R.array.drawer_items);
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_item, drawerItems));


        // Handles how to react when each item is clicked on the navigation bar
        drawerList.setOnItemClickListener((parent, view, position, id) -> {
            if(drawerItems[position].equals("Home")){
                Toast.makeText(this, "Already at Home!", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            else if(drawerItems[position].equals("Calendar")){
                Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
            else if(drawerItems[position].equals("Tasks Manager")){
                Intent intent = new Intent(HomeActivity.this, TaskManagerActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
            else if(drawerItems[position].equals("Friends")){
                Intent intent = new Intent(HomeActivity.this, FriendsListActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            else if(drawerItems[position].equals("Currency Converter")){
                Intent intent = new Intent(HomeActivity.this, CurrencyConverterActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
            else if(drawerItems[position].equals("Settings")){
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
            else if(drawerItems[position].equals("Packing CheckList")){
                Intent intent = new Intent(HomeActivity.this, PackingChecklistActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
            else if(drawerItems[position].equals("Group Chat")){
                Intent intent = new Intent(HomeActivity.this, TripChatActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }

            else if(drawerItems[position].equals("Budget Manager")){
                Intent intent = new Intent(HomeActivity.this, BudgetListActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Let's manage your trip budgets!", Toast.LENGTH_SHORT).show();
            }

            else if(drawerItems[position].equals("Logout")){
                Intent intent = new Intent(HomeActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, drawerItems[position] + " clicked", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        // Setup Drawer Toggle (for hamburger icon)
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerToggle.getDrawerArrowDrawable()
                .setColor(getResources().getColor(android.R.color.white));

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    // loads the username of the logged-in user from the server and displays it
    private void loadUserProfile(int userId) {
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v2/users/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // Extract only username
                    String username = response.optString("username", "N/A");

                    TextView tvUsername = findViewById(R.id.tvUsername);
                    tvUsername.setText(username.equals("null") ? "N/A" : username);
                },
                error -> Toast.makeText(this, "Failed to load username", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    // saves the current notification badge count when the activity pauses
    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences("session", MODE_PRIVATE)
                .edit()
                .putInt("badgeCount", notificationCount)
                .apply();
    }

    // restores the badge count and reconnects websocket if needed
    @Override
    protected void onResume() {
        super.onResume();
        notificationCount = getSharedPreferences("session", MODE_PRIVATE)
                .getInt("badgeCount", 0);
        notificationBadge.setNumber(notificationCount);
        if (!WebSocketManager.getInstance().isConnected()) {
            WebSocketManager.getInstance()
                    .connectWebSocket("ws://coms-3090-026.class.las.iastate.edu:8080/ws/notifications/" + userId);
        }
    }

    // disconnects websocket if the activity is closing
    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            WebSocketManager.getInstance().disconnectWebSocket();
        }
    }

    // removes websocket listener to prevent memory leaks
    protected void onDestroy() {
        super.onDestroy();
        WebSocketManager.getInstance().removeWebSocketListener();
    }
}
