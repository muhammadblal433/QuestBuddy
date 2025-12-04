package com.example.androidexample.payments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.HomeActivity;
import com.example.androidexample.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * PremiumActivity
 *
 * Shows the QuestBuddy Premium benefits and starts a Stripe Checkout
 * session via the backend:
 *
 *   POST /api/v15/payments/checkout/premium/{userId}
 *
 * The backend returns a StripeResponse with a sessionUrl that we open
 * in the browser so the user can pay for QuestBuddy Premium.
 */
public class PremiumActivity extends AppCompatActivity {

    private static final String HOST =
            "http://coms-3090-026.class.las.iastate.edu:8080";

    // New billing endpoint (note the /api/v15 + /checkout/premium/{userId})
    private static final String CHECKOUT_URL =
            HOST + "/api/v15/payments/checkout/premium/";

    private static final long PREMIUM_PRICE_CENTS = 399L; // $3.99 (matches backend default)

    private RequestQueue queue;
    private int userId;

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvFeatures;
    private Button btnUpgrade;
    private Button btnReturnHome;

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
        btnReturnHome = findViewById(R.id.btnReturnHome);

        setupPremiumText();

        // Upgrade = start Stripe Checkout session
        btnUpgrade.setOnClickListener(v -> startCheckout());

        // Return Home button
        btnReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PremiumActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
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
     * Calls backend to create a Stripe Checkout Session for QuestBuddy Premium.
     * Backend endpoint:
     *   POST /api/v15/payments/checkout/premium/{userId}
     *
     * Body (ProductRequest):
     *   { "amount": 399, "quantity": 1, "productName": "QuestBuddy Premium", "currency": "usd" }
     *
     * Response (StripeResponse):
     *   { "status": "success", "message": "...", "sessionId": "...", "sessionUrl": "https://checkout.stripe.com/..." }
     */
    private void startCheckout() {
        // Build URL with userId path variable
        String url = CHECKOUT_URL + userId;

        // Build request body to match ProductRequest
        JSONObject body = new JSONObject();
        try {
            body.put("amount", PREMIUM_PRICE_CENTS);       // 399 cents = $3.99
            body.put("quantity", 1L);
            body.put("productName", "QuestBuddy Premium");
            body.put("currency", "usd");
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating payment request.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    // StripeResponse: { status, message, sessionId, sessionUrl }
                    String status = response.optString("status", "error");
                    String sessionUrl = response.optString("sessionUrl", null);

                    if (!"success".equalsIgnoreCase(status) || sessionUrl == null || sessionUrl.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Failed to start checkout. Please try again.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    openStripeCheckout(sessionUrl);
                },
                error -> {
                    Toast.makeText(
                            this,
                            "Error contacting payment server.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        queue.add(request);
    }

    // Open Stripe Checkout URL in browser / custom tab
    private void openStripeCheckout(String sessionUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sessionUrl));
        startActivity(intent);
    }
}