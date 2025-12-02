package com.example.androidexample.payments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * PremiumActivity
 *
 * Shows the QuestBuddy Premium benefits and calls the backend payments API
 * to create a Stripe PaymentIntent for QuestBuddy Premium.
 *
 * Backend endpoint:
 *   POST /api/v14/payments/intents
 *
 * Request:
 *   Header: X-User-Id: <userId>
 *   Body: { "amount": 4.99, "currency": "usd", "tripId": null, "description": "QuestBuddy Premium" }
 *
 * Response (PaymentResponseDTO):
 *   { "paymentId": ..., "paymentIntentId": "...", "clientSecret": "..." }
 */
public class PremiumActivity extends AppCompatActivity {

    private static final String HOST = "http://coms-3090-026.class.las.iastate.edu:8080";
    private static final String PAYMENTS_INTENT_URL = HOST + "/api/v14/payments/intents";

    private RequestQueue queue;
    private int userId;

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvFeatures;
    private Button btnUpgrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        // Grab session info (saved in LoginActivity)
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "No user session found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        queue = Volley.newRequestQueue(this);

        tvTitle = findViewById(R.id.tvPremiumTitle);
        tvSubtitle = findViewById(R.id.tvPremiumSubtitle);
        tvFeatures = findViewById(R.id.tvPremiumFeatures);
        btnUpgrade = findViewById(R.id.btnUpgradePremium);

        setupPremiumText();

        btnUpgrade.setOnClickListener(v -> startCheckout());
    }

    // Sets the marketing copy
    private void setupPremiumText() {
        tvTitle.setText("Unlock QuestBuddy Premium, Travel Like a Pro");
        tvSubtitle.setText("Plan smarter trips with powerful premium tools.");

        String featuresText =
                "• Unlimited trips\n" +
                        "• Custom group chat names\n" +
                        "• AI budget planner for your trip\n" +
                        "• AI-generated packing lists tailored to your plans";

        tvFeatures.setText(featuresText);
    }

    /**
     * Calls backend to create a Stripe PaymentIntent for QuestBuddy Premium.
     * For demo purposes we:
     *   - Send a fixed 4.99 USD amount
     *   - Treat a successful response as a "fake payment success" in the UI
     */
    private void startCheckout() {
        // Build request body to match PaymentCreateDTO
        JSONObject body = new JSONObject();
        try {
            body.put("amount", 4.99);              // $4.99
            body.put("currency", "usd");
            body.put("tripId", JSONObject.NULL);   // Premium not tied to a specific trip
            body.put("description", "QuestBuddy Premium");
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating payment request.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                PAYMENTS_INTENT_URL,
                body,
                response -> {
                    // Expected response:
                    // { "paymentId": <long>, "paymentIntentId": "pi_...", "clientSecret": "..." }
                    String paymentId = response.optString("paymentId", null);
                    String clientSecret = response.optString("clientSecret", null);

                    if (clientSecret == null || clientSecret.isEmpty()) {
                        Toast.makeText(this,
                                "Payment intent created but no client secret returned.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // For class demo: treat this as a successful "fake payment" step
                    String msg = "Payment created! id=" + paymentId;
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    // TODO (optional): navigate to a dedicated success screen
                    // startActivity(new Intent(PremiumActivity.this, HomeActivity.class));
                    // finish();
                },
                error -> {
                    Toast.makeText(this,
                            "Error contacting payment server.",
                            Toast.LENGTH_SHORT).show();
                }
        ) {
            // Add X-User-Id header to match PaymentController.requireUserId(...)
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };

        queue.add(request);
    }

    // If later you switch back to browser-based Stripe Checkout, you can call this
    // from a different endpoint that returns sessionUrl.
    @SuppressWarnings("unused")
    private void openStripeCheckout(String sessionUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sessionUrl));
        startActivity(intent);
    }
}