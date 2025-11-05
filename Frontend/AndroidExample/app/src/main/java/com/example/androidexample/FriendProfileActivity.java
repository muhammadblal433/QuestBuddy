package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class FriendProfileActivity extends AppCompatActivity {

    private static final String BASE = "http://coms-3090-026.class.las.iastate.edu:8080/api/v8";

    private String username;
    private String displayName;
    private String currentUser;

    private String id;

    private TextView tvProfileName, tvProfileUsername, tvProfileEmail;
    private Button btnMessage, btnUnfriend, btnBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        // retrieve data passed from previous activity
        username = getIntent().getStringExtra("username");
        id = getIntent().getStringExtra("id");
        displayName = getIntent().getStringExtra("displayName");
        currentUser = getIntent().getStringExtra("currentUser");

        // initialize ui components
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        btnMessage = findViewById(R.id.btnMessage);
        btnUnfriend = findViewById(R.id.btnUnfriend);
        btnBlock = findViewById(R.id.btnBlock);

        // display basic user info
        tvProfileName.setText(displayName);
        tvProfileUsername.setText("@" + username);
        tvProfileEmail.setText("Email: Loading...");

        // fetch user's email from server
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v2/users/by-username/" + username;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    String email = res.optString("email", "Not available");
                    tvProfileEmail.setText("Email: " + email);
                },
                err -> tvProfileEmail.setText("Email: Not available"));
        Volley.newRequestQueue(this).add(req);

        // handle message button click
        btnMessage.setOnClickListener(v ->
                Toast.makeText(this, "Messaging feature coming soon!", Toast.LENGTH_SHORT).show());

        // handle unfriend button click
        btnUnfriend.setOnClickListener(v -> {
            String unfriendUrl = BASE + "/users/" + currentUser + "/friends/" + username;
            JsonObjectRequest r = new JsonObjectRequest(Request.Method.DELETE, unfriendUrl, null,
                    res -> Toast.makeText(this, "Unfriended @" + username, Toast.LENGTH_SHORT).show(),
                    err -> Toast.makeText(this, "Unfriend failed: " + err.getMessage(), Toast.LENGTH_SHORT).show());
            Volley.newRequestQueue(this).add(r);
        });

        // handle block button click
        btnBlock.setOnClickListener(v -> {
            String blockUrl = BASE + "/users/" + currentUser + "/friends/" + username + "/block";
            JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, blockUrl, null,
                    res -> Toast.makeText(this, "Blocked @" + username, Toast.LENGTH_SHORT).show(),
                    err -> Toast.makeText(this, "Block failed: " + err.getMessage(), Toast.LENGTH_SHORT).show());
            Volley.newRequestQueue(this).add(r);
        });
    }
}
