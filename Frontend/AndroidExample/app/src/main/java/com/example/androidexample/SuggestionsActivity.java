package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsActivity extends AppCompatActivity {
    private RecyclerView recyclerSuggestions;
    private FriendAdapter adapter;
    private List<Friend> suggestions;
    private String currentUsername;

    private Long currentUserId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        currentUsername = getIntent().getStringExtra("username");
        currentUserId = getIntent().getLongExtra("userID", -1);
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Error: No username provided!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerSuggestions = findViewById(R.id.recyclerSuggestions);
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));
        suggestions = new ArrayList<>();
        adapter = new FriendAdapter(this, suggestions, currentUsername, currentUserId);
        recyclerSuggestions.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading suggestions...");
        progressDialog.setCancelable(false);

        loadSuggestions();
    }

    private void loadSuggestions() {
        progressDialog.show();
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v8/users/"
                + currentUsername + "/friends/suggestions?limit=10";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressDialog.dismiss();
                    suggestions.clear();
                    parseSuggestions(response);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to load suggestions: " + error.toString(), Toast.LENGTH_SHORT).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }

    private void parseSuggestions(JSONArray response) {
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Friend f = new Friend();
                f.setId(obj.getLong("id"));
                f.setDisplayName(obj.getString("displayName"));
                f.setUsername(obj.getString("username"));
                f.setMutualCount(obj.optInt("mutualCount", 0));
                f.setStatus("SUGGESTED");
                suggestions.add(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 👇 Called when you press “Add Friend”
    public void sendFriendRequest(String targetUsername) {
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v8/users/"
                + currentUsername + "/friends/requests/" + targetUsername;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> Toast.makeText(this, "Request sent to " + targetUsername, Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Failed to send request: " + error.toString(), Toast.LENGTH_SHORT).show()
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }
}

