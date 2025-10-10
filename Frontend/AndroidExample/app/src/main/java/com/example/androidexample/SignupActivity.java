package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class SignupActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword, etUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmailSignup);
        etPassword = findViewById(R.id.etPasswordSignup);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etUserName = findViewById(R.id.etUsername);
        Button btnSignup = findViewById(R.id.btnSignup);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        btnSignup.setOnClickListener(v ->{
            String email = etEmail.getText().toString().trim();
            String username = etUserName.getText().toString().trim();
            String pw = etPassword.getText().toString().trim();
            String confirmPw = etConfirmPassword.getText().toString().trim();

            if(!isValidEmail(email)){
                etEmail.setError("Enter a valid email");
                return;
            }

            if(!isValidPassword(pw)){
                etPassword.setError("Password must be 6+ chars, 1 digit, 1 letter");
                return;
            }

            if(!pw.equals(confirmPw)){
                etConfirmPassword.setError("Passwords do not match");
                return;
            }

            signupUser(username, email, pw);

        });
        tvLoginLink.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));

    }

    private void signupUser(String username, String email, String password) {
        JSONObject userJson = new JSONObject();
        try {
            userJson.put("username", username);
            userJson.put("email", email);
            userJson.put("password", password);
            userJson.put("role", "TRIP_MEMBER");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v1/auth/signup";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                userJson,
                response -> {
                    // Successful signup response
                    Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show();

                    // Move to Login screen
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                },
                error -> {
                    // Parse server error for specific message
                    String errorMessage = "Signup failed.";
                    if (error.networkResponse != null) {
                        Log.e("VolleyError", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("VolleyError", "Response: " + new String(error.networkResponse.data, StandardCharsets.UTF_8));
                    }
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            responseBody = responseBody.trim();

                            // Try parsing JSON manually
                            if (responseBody.startsWith("{")) {
                                JSONObject data = new JSONObject(responseBody);
                                if (data.has("error")) {
                                    String errorCode = data.getString("error");

                                    switch (errorCode) {
                                        case "username_exists":
                                            etUserName.setError("Username already exists");
                                            errorMessage = "Username already exists";
                                            break;
                                        case "email_exists":
                                            etEmail.setError("Email already registered");
                                            errorMessage = "Email already registered";
                                            break;
                                        default:
                                            errorMessage = "Signup failed: " + errorCode;
                                            break;
                                    }
                                }
                            } else {
                                // Handle non-JSON plain text errors
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

    // regex for email
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // regex for password: at least 6 chars, at least 1 letter and at least 1 digit
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");
    }
}
