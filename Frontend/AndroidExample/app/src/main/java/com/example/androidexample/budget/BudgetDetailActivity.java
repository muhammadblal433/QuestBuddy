package com.example.androidexample.budget;
import com.example.androidexample.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AlertDialog;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

// shows details for one budget

/**
 * Activity that shows the details of a single budget, including its owner,
 * totals, creation time, and per-user splits. It loads data from the backend,
 * displays the list of splits, and allows the owner to edit or delete the budget.
 */
public class BudgetDetailActivity extends AppCompatActivity {

    private TextView tvBudgetName, tvOwner, tvTotals, tvCreatedAt;
    private RecyclerView recyclerSplits;
    private RequestQueue queue;
    private SplitAdapter splitAdapter;
    private List<Split> splits = new ArrayList<>();
    private long budgetId;
    private SplitEditAdapter editAdapter;
    private String username, ownerUsername;
    private boolean isOwner = false;
    private static final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080/api/v11";

    /**
     * Initializes the activity, binds view references, sets up the RecyclerView,
     * retrieves the active budget id and current username, and triggers the initial load
     * of budget details. Also wires up click listeners for update and delete buttons.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     *                           if the activity is being created for the first time
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_detail);

        tvBudgetName = findViewById(R.id.tvBudgetName);
        tvOwner = findViewById(R.id.tvOwner);
        tvTotals = findViewById(R.id.tvTotals);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        recyclerSplits = findViewById(R.id.recyclerSplits);
        Button btnUpdate = findViewById(R.id.btnUpdateBudget);
        Button btnDelete = findViewById(R.id.btnDeleteBudget);

        queue = Volley.newRequestQueue(this);
        recyclerSplits.setLayoutManager(new LinearLayoutManager(this));
        splitAdapter = new SplitAdapter(splits);
        recyclerSplits.setAdapter(splitAdapter);

        budgetId = getIntent().getLongExtra("budgetId", -1);
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        username = prefs.getString("username", null);

        if (budgetId == -1 || username == null) {
            Toast.makeText(this, "Invalid budget", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBudget(); // loads the budget

        btnUpdate.setOnClickListener(v -> showEditableSplits());
        btnDelete.setOnClickListener(v -> deleteBudget());
    }

    /**
     * Fetches the latest details of this budget from the server, including
     * owner, totals, creation date, and the list of splits. The UI is updated
     * with the response, and the {@link SplitAdapter} is notified of data changes.
     */
    // loads budgets details from the server
    private void loadBudget() {
        String url = BASE_URL + "/users/" + username + "/budgets/" + budgetId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String name = response.getString("name");
                        ownerUsername = response.getString("ownerUsername");
                        isOwner = ownerUsername.equalsIgnoreCase(username);
                        double totalShare = response.getDouble("totalShare");
                        double totalPaid = response.getDouble("totalPaid");
                        String createdAt = response.getString("createdAt");
                        tvBudgetName.setText(name);
                        tvOwner.setText("Owner: " + ownerUsername);
                        tvTotals.setText("Total Paid: $" + totalPaid + " | Total Share: $" + totalShare);
                        tvCreatedAt.setText("Created: " + createdAt);

                        JSONArray splitsArr = response.getJSONArray("splits");
                        splits.clear();
                        for (int i = 0; i < splitsArr.length(); i++) {
                            JSONObject s = splitsArr.getJSONObject(i);
                            splits.add(new Split(
                                    s.getString("username"),
                                    s.getDouble("shareAmount"),
                                    s.getDouble("paidAmount"),
                                    s.getDouble("balance")
                            ));
                        }
                        splitAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Failed to load budget details", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Username", username);
                return headers;
            }
        };
        queue.add(request);
    }

    /**
     * Switches the splits list into editable mode for the budget owner and
     * shows a confirmation dialog asking whether to save or discard the changes.
     * If the user chooses "Save", {@link #updateBudget()} is called; otherwise,
     * the original budget data is reloaded from the server.
     */
    // enables edit mode for budget splits and prompts user to save or cancel changes
    private void showEditableSplits() {
        if (!isOwner) {
            Toast.makeText(this, "Only the owner can edit this budget", Toast.LENGTH_SHORT).show();
            return;
        }

        editAdapter = new SplitEditAdapter(splits, username, isOwner);
        recyclerSplits.setAdapter(editAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Save Changes?")
                .setMessage("Would you like to save the updates to this budget?")
                .setPositiveButton("Save", (dialog, which) -> updateBudget())
                .setNegativeButton("Cancel", (dialog, which) -> loadBudget())
                .show();
    }

    /**
     * Builds a JSON payload from the updated split list and sends a PUT request
     * to the backend to update the budget. On success, a success message is shown
     * and the latest data is reloaded; on failure, an error toast is displayed.
     */
    // sends a put request to update the budget with edited split data
    private void updateBudget() {
        List<Split> updatedSplits = editAdapter.getUpdatedSplits();
        JSONArray arr = new JSONArray();

        for (Split s : updatedSplits) {
            try {
                JSONObject o = new JSONObject();
                o.put("username", s.getUsername());
                o.put("shareAmount", s.getShareAmount());
                o.put("paidAmount", s.getPaidAmount());
                arr.put(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject body = new JSONObject();
        try {
            body.put("splits", arr);
            body.put("name", tvBudgetName.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/users/" + ownerUsername + "/budgets/" + budgetId;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Toast.makeText(this, "Budget updated successfully!", Toast.LENGTH_SHORT).show();
                    loadBudget();
                },
                error -> Toast.makeText(this, "Failed to update budget", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Username", username);
                return headers;
            }
        };
        queue.add(req);
    }


    /**
     * Sends a DELETE request to the backend to remove the current budget.
     * If the deletion succeeds, the activity finishes and returns to the previous
     * screen; otherwise, an error toast is shown.
     */
    // sends a delete request to remove the current budget from the server
    private void deleteBudget() {
        String url = BASE_URL + "/users/" + ownerUsername + "/budgets/" + budgetId;

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Budget deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Failed to delete budget", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Username", username);
                return headers;
            }
        };

        queue.add(request);
    }
}