package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class AccountActivity extends AppCompatActivity {

    private int userId;
    private TextView tvUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        tvUserInfo = findViewById(R.id.tvUserInfo);
        Button btnViewProfile = findViewById(R.id.btnViewProfile);
        Button btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        Button btnDeleteProfile = findViewById(R.id.btnDeleteProfile);

        // Get userId passed from LoginActivity
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        btnViewProfile.setOnClickListener(v -> viewProfile(userId));
        btnUpdateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, UpdateProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
        btnDeleteProfile.setOnClickListener(v -> deleteProfile(userId));
    }

    private void viewProfile(int userId) {
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v2/users/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String email = response.optString("email", "N/A");
                        String username = response.optString("username", "N/A");
                        String firstName = response.optString("firstName", "N/A");
                        String lastName = response.optString("lastName", "N/A");

                        if (firstName == null || firstName.equals("null")) firstName = "None";
                        if (lastName == null || lastName.equals("null")) lastName = "None";

                        String info = "Username: " + username +
                                "\nEmail: " + email +
                                "\nFirst Name: " + firstName +
                                "\nLast Name: " + lastName;

                        tvUserInfo.setText(info);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error reading profile.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void updateProfile(int userId) {
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v2/users/" + userId;

        // For simplicity, let’s assume we update username only
        JSONObject updateJson = new JSONObject();
        try {
            updateJson.put("username", "updatedUser");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                updateJson,
                response -> Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show(),
                error -> {
                    String msg = "Update failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void deleteProfile(int userId) {
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v2/users/" + userId;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(this, "Account deleted.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AccountActivity.this, SignupActivity.class));
                    finish();
                },
                error -> Toast.makeText(this, "Delete failed.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}