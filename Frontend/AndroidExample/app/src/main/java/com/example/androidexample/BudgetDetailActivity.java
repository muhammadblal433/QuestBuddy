package com.example.androidexample;

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

// shows details for one budget
public class BudgetDetailActivity extends AppCompatActivity {

    private TextView tvBudgetName, tvOwner, tvTotals, tvCreatedAt;
    private RecyclerView recyclerSplits;
    private RequestQueue queue;
    private SplitAdapter splitAdapter;
    private List<Split> splits = new ArrayList<>();
    private long budgetId;
    private String username;
    private static final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080/api/v11";

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

        btnUpdate.setOnClickListener(v -> Toast.makeText(this, "Update feature coming soon", Toast.LENGTH_SHORT).show());
        btnDelete.setOnClickListener(v -> deleteBudget());
    }

    // loads budgets details from the server
    private void loadBudget() {
        String url = BASE_URL + "/users/" + username + "/budgets/" + budgetId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String name = response.getString("name");
                        String ownerUsername = response.getString("ownerUsername");
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

                    } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Failed to load budget details", Toast.LENGTH_SHORT).show()
        );
        queue.add(request); // add to queue
    }

    //deletes budgets from the server
    private void deleteBudget() {
        String url = BASE_URL + "/users/" + username + "/budgets/" + budgetId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    Toast.makeText(this, "Budget deleted", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Failed to delete budget", Toast.LENGTH_SHORT).show()
        );
        queue.add(request); // add to queue
    }
}

