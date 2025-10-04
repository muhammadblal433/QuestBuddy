package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Button btnDelete = findViewById(R.id.btnDelete);

        btnDelete.setOnClickListener(v -> deleteUser());
    }

    private void deleteUser() {
        String url = "https://7e3a174b-bdb2-4e74-b53f-b245e9400d84.mock.pstmn.io/user_info";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(this,
                            "DELETE Success: " + response.toString(),
                            Toast.LENGTH_SHORT).show();

                    // after delete, go back to login page
                    startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                    finish();
                },
                error -> {
                    Toast.makeText(this,
                            "DELETE Error: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
