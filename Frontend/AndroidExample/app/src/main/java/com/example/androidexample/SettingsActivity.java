package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvName, tvUsername, tvEmail;
    private ImageView ivAvatar;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        ivAvatar = findViewById(R.id.ivAvatar);
        Button btnHome = findViewById(R.id.btnBackHome);
        Button delete = findViewById(R.id.deleteAccount);

        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile(userId);

        // Click listeners to edit each field
        tvName.setOnClickListener(v -> showEditDialog("Name"));
        tvUsername.setOnClickListener(v -> showEditDialog("Username"));
        tvEmail.setOnClickListener(v -> showEditDialog("Email"));
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
        delete.setOnClickListener(v -> showDeleteConfirmation());

    }

    private void loadUserProfile(int userId) {
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v2/users/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String firstName = response.optString("firstName", "None");
                    String lastName = response.optString("lastName", "None");
                    String email = response.optString("email", "N/A");
                    String username = response.optString("username", "N/A");
                    String avatarUrl = response.optString("avatarUrl", "");

                    String fullName;
                    if (firstName.equals("null") && lastName.equals("null")){
                        fullName = "None";
                    }

                    else if(lastName.equals("null")){
                        fullName = firstName;
                    }
                    else{
                        fullName = firstName + " " + lastName;
                    }


                    tvName.setText(fullName);
                    tvUsername.setText("@" + username);
                    tvEmail.setText(email);

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        loadAvatar(avatarUrl);
                    } else {
                        ivAvatar.setImageResource(R.drawable.default_pic);
                    }
                },
                error -> Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadAvatar(String imageUrl) {
        ImageRequest imageRequest = new ImageRequest(
                imageUrl,
                response -> ivAvatar.setImageBitmap(response),
                0, 0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                error -> ivAvatar.setImageResource(R.drawable.default_pic)
        );
        Volley.newRequestQueue(this).add(imageRequest);
    }

    /** Dialog for editing profile fields **/
    private void showEditDialog(String field) {
        EditText input = new EditText(this);
        input.setHint("Enter new " + field.toLowerCase());

        new AlertDialog.Builder(this)
                .setTitle("Edit " + field)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = input.getText().toString().trim();
                    if (!newValue.isEmpty()) {
                        updateUserField(field, newValue);
                    } else {
                        Toast.makeText(this, "Value cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** PUT request to update single field **/
    private void updateUserField(String field, String value) {
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v1/users/" + userId;

        JSONObject updateJson = new JSONObject();
        try {
            switch (field) {
                case "Name":
                    // Split into first and last name if user enters two words
                    String[] parts = value.split(" ", 2);
                    updateJson.put("firstName", parts[0]);
                    if (parts.length > 1)
                        updateJson.put("lastName", parts[1]);
                    else
                        updateJson.put("lastName", "");
                    break;
                case "Username":
                    updateJson.put("username", value);
                    break;
                case "Email":
                    updateJson.put("email", value);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                updateJson,
                response -> {
                    Toast.makeText(this, field + " updated successfully!", Toast.LENGTH_SHORT).show();
                    loadUserProfile(userId);
                },
                error -> {
                    String msg = "Update failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(putRequest);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUserAccount() {
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v2/users/" + userId;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, SignupActivity.class));
                    finish();
                },
                error -> {
                    String msg = "Delete failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    }
                    Log.e("DELETE_ERROR", msg);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}