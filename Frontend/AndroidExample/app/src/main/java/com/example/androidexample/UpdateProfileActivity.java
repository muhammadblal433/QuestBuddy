package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etFirstName, etLastName;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        etEmail = findViewById(R.id.etEmailUpdate);
        etUsername = findViewById(R.id.etUsernameUpdate);
        etFirstName = findViewById(R.id.etFirstNameUpdate);
        etLastName = findViewById(R.id.etLastNameUpdate);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Get userId from intent (passed from AccountActivity)
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSaveProfile.setOnClickListener(v -> updateUserProfile());
    }

    private void updateUserProfile() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();

        if (email.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Email and username are required", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject updateJson = new JSONObject();
        try {
            updateJson.put("email", email);
            updateJson.put("username", username);
            updateJson.put("firstName", firstName);
            updateJson.put("lastName", lastName);
            updateJson.put("avatarUrl", "https://yo.com/sonic.png");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v1/users/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                updateJson,
                response -> {
                    String message = response.optString("message", "Profile updated successfully!");
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    // Go back to account screen
                    Intent intent = new Intent(UpdateProfileActivity.this, AccountActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    String msg = "Update failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
