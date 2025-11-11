package com.example.androidexample.budget;

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
import com.example.androidexample.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_list);

        recyclerBudgets = findViewById(R.id.recyclerBudgets);
        Button btnAdd = findViewById(R.id.btnAddBudget);

        recyclerBudgets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BudgetAdapter(budgetList, this::onBudgetClick);
        recyclerBudgets.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        username = prefs.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "No user logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBudgets(); // load budgets from server

        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(BudgetListActivity.this, CreateBudgetActivity.class);
            startActivity(i); // open create budget page
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
