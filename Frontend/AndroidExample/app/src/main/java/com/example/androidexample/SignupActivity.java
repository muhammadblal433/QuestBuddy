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

public class SignupActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmailSignup);
        etPassword = findViewById(R.id.etPasswordSignup);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSignup = findViewById(R.id.btnSignup);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        btnSignup.setOnClickListener(v ->{
            String email = etEmail.getText().toString().trim();
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

            Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
        });
        tvLoginLink.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));

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
