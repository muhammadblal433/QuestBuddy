package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private RequestQueue queue;

    private static final String HOST = "http://coms-3090-026.class.las.iastate.edu:8080";
    private static final String LOGIN_URL = HOST + "/api/v1/auth/login";
    private static final String USER_BY_ID_URL = HOST + "/api/v2/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        Button btnLogin = findViewById(R.id.btnLogin);

        queue = Volley.newRequestQueue(this);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pw = etPassword.getText().toString().trim();

            if (!isValidEmail(email)) { etEmail.setError("Enter a valid email"); return; }
            if (TextUtils.isEmpty(pw)) { etPassword.setError("Password required"); return; }

            loginUser(email, pw);
        });
    }

    private void loginUser(String email, String password) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                LOGIN_URL,
                body,
                response -> {
                    try {
                        if (!response.has("userId")) {
                            Toast.makeText(this, "Login failed: Invalid credentials", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int userId = response.getInt("userId");
                        String username = null;
                        if (response.has("username")) {
                            username = response.optString("username", null);
                        } else if (response.has("user")) {
                            JSONObject userObj = response.optJSONObject("user");
                            if (userObj != null) username = userObj.optString("username", null);
                        }

                        if (!TextUtils.isEmpty(username)) {
                            saveSession(userId, username);
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            goFriends(userId, username);
                        } else {
                            fetchUsernameById(userId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error reading response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = "Login failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8).trim();
                            if (responseBody.startsWith("{")) {
                                JSONObject data = new JSONObject(responseBody);
                                if (data.has("error")) {
                                    String errorCode = data.getString("error");
                                    if ("invalid_credentials".equals(errorCode)) {
                                        errorMessage = "Invalid email or password";
                                        etEmail.setError("Check email");
                                        etPassword.setError("Check password");
                                    } else {
                                        errorMessage = "Login failed: " + errorCode;
                                    }
                                }
                            } else {
                                errorMessage = responseBody;
                            }
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
        );

        queue.add(request);
    }

    private void fetchUsernameById(int userId) {
        String url = USER_BY_ID_URL + userId;

        JsonObjectRequest getReq = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    String username = resp.optString("username", null);
                    if (!TextUtils.isEmpty(username)) {
                        saveSession(userId, username);
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        goFriends(userId, username);
                    } else {
                        promptForUsernameThenContinue(userId);
                    }
                },
                err -> {
                    promptForUsernameThenContinue(userId);
                }
        );

        queue.add(getReq);
    }

    private void promptForUsernameThenContinue(int userId) {
        final EditText et = new EditText(this);
        et.setHint("Your username (e.g., alice123)");

        new AlertDialog.Builder(this)
                .setTitle("Set Username")
                .setMessage("Enter your username once. We’ll remember it for your friends features.")
                .setView(et)
                .setPositiveButton("Save", (d, w) -> {
                    String uname = et.getText().toString().trim();
                    if (TextUtils.isEmpty(uname)) {
                        Toast.makeText(this, "Username required to continue", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveSession(userId, uname);
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    goFriends(userId, uname);
                })
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    private void saveSession(int userId, String username) {
        getSharedPreferences("session", MODE_PRIVATE)
                .edit()
                .putInt("userId", userId)
                .putString("username", username)
                .apply();
    }

    private void goFriends(int userId, String username) {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String s) {
        return !TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches();
    }
}
