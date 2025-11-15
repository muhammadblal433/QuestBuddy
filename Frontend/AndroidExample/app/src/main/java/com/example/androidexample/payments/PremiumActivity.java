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
import com.example.androidexample.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * PremiumActivity
 *
 * Shows the QuestBuddy Premium benefits and starts a Stripe Checkout
 * session via the backend:
 *
 *   POST /checkout/premium/{user_id}
 *
 * The backend returns a StripeResponse with a sessionUrl that we open
 * in the browser so the user can pay for QuestBuddy Premium.
 */
public class PremiumActivity extends AppCompatActivity {

    private static final String HOST = "http://coms-3090-026.class.las.iastate.edu:8080";

    //may need to change this later
    private static final String CHECKOUT_URL = HOST + "/checkout/premium/";

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

    // Sets the marketing copy you requested
    private void setupPremiumText() {
        tvTitle.setText("Unlock QuestBuddy Premium, Travel Like a Pro");
        // We no longer need a separate subtitle line
        tvSubtitle.setText("");

        String featuresText =
                "- Unlimited trips\n" +
                        "- Custom group chat names\n" +
                        "- AI budget planner for your trip\n" +
                        "- AI-generated packing lists tailored to your plans";

        tvFeatures.setText(featuresText);
    }

    // Calls backend to create a Stripe Checkout session
    private void startCheckout() {
        String url = CHECKOUT_URL + userId;

        // This matches ProductRequest on the backend
        JSONObject body = new JSONObject();
        try {
            // Stripe expects amount in the smallest currency unit (cents).
            // 499 = $4.99, adjust if you want a different price.
            body.put("amount", 499L);
            body.put("quantity", 1L);
            body.put("productName", "QuestBuddy Premium");
            body.put("currency", "USD");
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
                    String sessionUrl = response.optString("sessionUrl", null);
                    if (sessionUrl == null || sessionUrl.isEmpty()) {
                        Toast.makeText(this, "Checkout started, but no session URL returned.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    openStripeCheckout(sessionUrl);
                },
                error -> {
                    Toast.makeText(this, "Error contacting payment server.", Toast.LENGTH_SHORT).show();
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