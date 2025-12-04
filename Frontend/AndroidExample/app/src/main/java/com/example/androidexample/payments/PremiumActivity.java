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
 * The backend returns a StripeResponse with a session URL that we open
 * in the browser so the user can pay for QuestBuddy Premium.
 */
public class PremiumActivity extends AppCompatActivity {

    private static final String HOST =
            "http://coms-3090-026.class.las.iastate.edu:8080";

    // New Stripe Checkout endpoint (matches Postman)
    private static final String CHECKOUT_URL =
            HOST + "/api/v15/payments/checkout/premium/";

    // Amount is in *cents* because Stripe expects the smallest currency unit
    // 399 = $3.99
    private static final long PREMIUM_PRICE_CENTS = 399L;

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
            Toast.makeText(this,
                    "No user session found. Please log in again.",
                    Toast.LENGTH_SHORT).show();
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

        // Start Stripe checkout when user taps Upgrade
        btnUpgrade.setOnClickListener(v -> startCheckout());

        // Return Home button: go back to HomeActivity and pass userId
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
     * Calls backend to create a Stripe Checkout session for QuestBuddy Premium.
     * On success, opens the returned session URL in the browser.
     */
    private void startCheckout() {
        // URL: /api/v15/payments/checkout/premium/{userId}
        String url = CHECKOUT_URL + userId;

        // Body matches ProductRequest on the backend
        JSONObject body = new JSONObject();
        try {
            body.put("productName", "QuestBuddy Premium");
            body.put("amount", PREMIUM_PRICE_CENTS);  // cents, so 399 = $3.99
            body.put("currency", "usd");
            body.put("quantity", 1L);
        } catch (JSONException e) {
            Toast.makeText(this,
                    "Error creating payment request.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    // StripeResponse: { status, message, sessionId, sessionUrl } OR { ..., url }
                    String checkoutUrl = response.optString("sessionUrl", null);

                    // In case backend uses "url" instead of "sessionUrl"
                    if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                        checkoutUrl = response.optString("url", null);
                    }

                    if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Checkout created but no URL returned.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    openStripeCheckout(checkoutUrl);
                },
                error -> {
                    String msg = "Error contacting payment server.";
                    if (error.networkResponse != null) {
                        msg += " (HTTP " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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