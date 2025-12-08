package com.example.androidexample.budget;
import com.example.androidexample.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.example.androidexample.payments.PremiumActivity;


// shows all budgets for the logged-in user
public class BudgetListActivity extends AppCompatActivity {

    private RecyclerView recyclerBudgets;
    private BudgetAdapter adapter;
    private List<Budget> budgetList = new ArrayList<>();
    private RequestQueue queue;
    private String username;
    private static final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080/api/v11";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        username = prefs.getString("username", null);
        int userId = prefs.getInt("userId", -1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_list);

        recyclerBudgets = findViewById(R.id.recyclerBudgets);
        Button btnAdd = findViewById(R.id.btnAddBudget);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetListActivity.this, CreateBudgetActivity.class);
            startActivity(intent);
        });

        recyclerBudgets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BudgetAdapter(budgetList, this::onBudgetClick);
        recyclerBudgets.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);

        if (username == null) {
            Toast.makeText(this, "No user logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBudgets(); // load budgets from server

        // returns to home page
        Button btnReturnHome = findViewById(R.id.btnReturnHome);
        btnReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetListActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });

        // Upgrade to Premium button
        Button btnUpgradePremium = findViewById(R.id.btnUpgradePremium);
        btnUpgradePremium.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetListActivity.this, PremiumActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    // loads all budgets for the current user
    private void loadBudgets() {
        String url = BASE_URL + "/users/" + username + "/budgets";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    budgetList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            budgetList.add(new Budget(
                                    obj.getLong("id"),
                                    obj.getString("name"),
                                    obj.getString("ownerUsername"),
                                    obj.getDouble("totalShare"),
                                    obj.getDouble("totalPaid"),
                                    obj.getString("createdAt")
                            ));
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to load budgets", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Username", username);
                return headers;
            }
        };
        queue.add(request);
    }


    // opens budget details when clicked
    private void onBudgetClick(Budget budget) {
        Intent intent = new Intent(BudgetListActivity.this, BudgetDetailActivity.class);
        intent.putExtra("budgetId", budget.getId());
        startActivity(intent);
    }
}
