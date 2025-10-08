package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity{
    private EditText etEmail, etPassword;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pw = etPassword.getText().toString().trim();

            if (!isValidEmail(email)) { etEmail.setError("Enter a valid email"); return; }
            if (TextUtils.isEmpty(pw)) { etPassword.setError("Password required"); return; }

            loginUser(email, pw);
        });

    }

    private void loginUser(String email, String password) {
        JSONObject loginJson = new JSONObject();
        try {
            loginJson.put("email", email);
            loginJson.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v1/auth/login";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                loginJson,
                response -> {
                    try {
                        if (response.has("userId")) {
                            int userId = response.getInt("userId");

                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                            // Go to AccountActivity with userId
                            Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Login failed: Invalid credentials", Toast.LENGTH_SHORT).show();
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
                            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            responseBody = responseBody.trim();

                            // Try to parse JSON error
                            if (responseBody.startsWith("{")) {
                                JSONObject data = new JSONObject(responseBody);
                                if (data.has("error")) {
                                    String errorCode = data.getString("error");

                                    switch (errorCode) {
                                        case "invalid_credentials":
                                            errorMessage = "Invalid email or password";
                                            etEmail.setError("Check email");
                                            etPassword.setError("Check password");
                                            break;
                                        default:
                                            errorMessage = "Login failed: " + errorCode;
                                            break;
                                    }
                                }
                            } else {
                                // Non-JSON (plain string)
                                errorMessage = responseBody;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private boolean isValidEmail(String s) {
        return !TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches();
    }
}
