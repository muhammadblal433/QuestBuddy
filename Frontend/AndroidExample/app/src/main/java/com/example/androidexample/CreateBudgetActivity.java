package com.example.androidexample;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;

// lets user create a new budget with participants
public class CreateBudgetActivity extends AppCompatActivity {

    private EditText etBudgetName;
    private LinearLayout participantsContainer;
    private RequestQueue queue;
    private String username;

    private static final String BASE_URL =
            "http://coms-3090-026.class.las.iastate.edu:8080/api/v11/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_create);

        etBudgetName = findViewById(R.id.etBudgetName);
        participantsContainer = findViewById(R.id.participantsContainer);
        Button btnAddParticipant = findViewById(R.id.btnAddParticipant);
        Button btnCreate = findViewById(R.id.btnCreateBudget);

        queue = Volley.newRequestQueue(this);
        username = getSharedPreferences("session", MODE_PRIVATE)
                .getString("username", "alice123");

        addParticipantView();
        addParticipantView();

        btnAddParticipant.setOnClickListener(v -> {
            addParticipantView();
            scrollToBottom();
        });

        btnCreate.setOnClickListener(v -> createBudget());
    }

    // adds a participant card to layout
    private void addParticipantView() {
        View participantView = LayoutInflater.from(this)
                .inflate(R.layout.item_participant_input, participantsContainer, false);

        TextView label = participantView.findViewById(R.id.tvParticipantLabel);
        label.setText("Participant " + (participantsContainer.getChildCount() + 1));

        Button btnDelete = participantView.findViewById(R.id.btnDeleteParticipant);
        btnDelete.setOnClickListener(v -> {
            if (participantsContainer.getChildCount() > 1) {
                participantsContainer.removeView(participantView);
                renumberParticipants();
            } else {
                Toast.makeText(this, "At least one participant required", Toast.LENGTH_SHORT).show();
            }
        });

        participantsContainer.addView(participantView);
    }

    // renumbers participant labels after delete
    private void renumberParticipants() {
        for (int i = 0; i < participantsContainer.getChildCount(); i++) {
            View pView = participantsContainer.getChildAt(i);
            TextView label = pView.findViewById(R.id.tvParticipantLabel);
            label.setText("Participant " + (i + 1));
        }
    }

    // scrolls view to bottom
    private void scrollToBottom() {
        final ScrollView scrollView = findViewById(R.id.scrollViewBudgetCreate);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    // builds json and sends post request to create budget
    private void createBudget() {
        String name = etBudgetName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etBudgetName.setError("Budget name required");
            return;
        }

        JSONArray splitsArray = new JSONArray();

        for (int i = 0; i < participantsContainer.getChildCount(); i++) {
            View pView = participantsContainer.getChildAt(i);
            EditText etUsername = pView.findViewById(R.id.etUsername);
            EditText etShare = pView.findViewById(R.id.etShareAmount);
            EditText etPaid = pView.findViewById(R.id.etPaidAmount);

            String uname = etUsername.getText().toString().trim();
            String shareStr = etShare.getText().toString().trim();
            String paidStr = etPaid.getText().toString().trim();

            if (TextUtils.isEmpty(uname)) continue; // skip if no username

            BigDecimal share = new BigDecimal(TextUtils.isEmpty(shareStr) ? "0" : shareStr);
            BigDecimal paid = new BigDecimal(TextUtils.isEmpty(paidStr) ? "0" : paidStr);

            JSONObject split = new JSONObject();
            try {
                split.put("username", uname);
                split.put("shareAmount", share);
                split.put("paidAmount", paid);
                splitsArray.put(split);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (splitsArray.length() == 0) {
            Toast.makeText(this, "Add at least one participant", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject(); // main request body
        try {
            body.put("name", name);
            body.put("splits", splitsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + username + "/budgets"; // full api endpoiint

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, body,
                response -> {
                    Toast.makeText(this, "Budget created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Error creating budget", Toast.LENGTH_SHORT).show()
        );

        queue.add(request); // send request
    }
}
