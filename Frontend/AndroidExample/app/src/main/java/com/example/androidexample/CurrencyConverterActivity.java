package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class CurrencyConverterActivity extends AppCompatActivity {

    private EditText etBaseCurrency, etTargetCurrency, etNewRate;
    private TextView tvRateResult;
    private Button btnFetchRate, btnAddRate;
    private RequestQueue queue;
    private int userId;

    // Mock server URL
    private final String BASE_URL = "https://49b6c56b-2abb-40b2-94cb-f501f3d6d562.mock.pstmn.io/api/v1/converter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        etBaseCurrency = findViewById(R.id.etBaseCurrency);
        etTargetCurrency = findViewById(R.id.etTargetCurrency);
        etNewRate = findViewById(R.id.etNewRate);
        tvRateResult = findViewById(R.id.tvRateResult);
        btnFetchRate = findViewById(R.id.btnFetchRate);
        btnAddRate = findViewById(R.id.btnUpdateRate);
        Button btnBackHome = findViewById(R.id.btnBackHome);
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        queue = Volley.newRequestQueue(this);

        btnFetchRate.setOnClickListener(v -> fetchExchangeRate());
        btnAddRate.setOnClickListener(v -> postExchangeRate());
        btnBackHome.setOnClickListener(v ->
        {
            Intent intent = new Intent(CurrencyConverterActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    private void fetchExchangeRate() {
        String base = etBaseCurrency.getText().toString().trim();
        String target = etTargetCurrency.getText().toString().trim();

        if (base.isEmpty() || target.isEmpty()) {
            Toast.makeText(this, "Enter both currencies!", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "?base=" + base + "&target=" + target;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    double rate = response.optDouble("exchangeRate", 0);
                    tvRateResult.setText("1 " + base + " = " + rate + " " + target);
                },
                error -> {
                    Log.e("API_ERROR", "GET failed: " + error.toString());
                    Toast.makeText(this, "Failed to fetch rate", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }

    private void postExchangeRate() {
        String base = etBaseCurrency.getText().toString().trim();
        String target = etTargetCurrency.getText().toString().trim();
        String rateStr = etNewRate.getText().toString().trim();

        if (base.isEmpty() || target.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        double rate = Double.parseDouble(rateStr);

        try {
            JSONObject body = new JSONObject();
            body.put("baseCurrency", base);
            body.put("targetCurrency", target);
            body.put("exchangeRate", rate);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BASE_URL,
                    body,
                    response -> Toast.makeText(this,
                            "Rate added: " + response.optDouble("exchangeRate", 0),
                            Toast.LENGTH_SHORT).show(),
                    error -> {
                        Log.e("API_ERROR", "POST failed: " + error.toString());
                        Toast.makeText(this, "Failed to add rate", Toast.LENGTH_SHORT).show();
                    }
            );

            queue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}