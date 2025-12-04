package com.example.androidexample.payments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.HomeActivity;
import com.example.androidexample.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

    // Endpoint that lists all premium users
    private static final String PREMIUM_USERS_URL =
            HOST + "/api/v1/users/premium";

    // Amount is in *cents* because Stripe expects the smallest currency unit
    // 399 = $3.99
    private static final long PREMIUM_PRICE_CENTS = 399L;

    private RequestQueue queue;
    private int userId;
    private boolean isPremium = false;   // track if this user is already premium

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

        // Check from backend if this user is already premium
        checkPremiumStatus();

        // Start Stripe checkout when user taps Upgrade
        btnUpgrade.setOnClickListener(v -> {
            if (isPremium) {
                Toast.makeText(
                        this,
                        "You are already a premium member",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                startCheckout();
            }
        });

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
                "• Unlimited trips and saved itineraries\n" +
                        "• Custom group chat names for every trip\n" +
                        "• Shared budgets to split trip costs with friends";

        tvFeatures.setText(featuresText);
    }

    /**
     * Calls backend /api/v1/users/premium and checks
     * if this user's id is in the list.
     */
    private void checkPremiumStatus() {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                PREMIUM_USERS_URL,
                null,
                response -> {
                    isPremium = isUserInPremiumList(response, userId);
                    // no UI change needed; we just flip the flag
                },
                error -> {
                    // If this fails, we just assume not premium and let them try
                    isPremium = false;
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Many of your APIs expect X-User-Id
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };

        queue.add(request);
    }

    // Helper: check if current userId appears in the premium users array
    private boolean isUserInPremiumList(JSONArray array, int currentUserId) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject userObj = array.optJSONObject(i);
            if (userObj == null) continue;
            int id = userObj.optInt("id", -1);
            if (id == currentUserId) {
                return true;
            }
        }
        return false;
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