package com.example.androidexample.friends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;
import com.example.androidexample.R;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity {
    private RecyclerView recyclerRequests;
    private FriendAdapter adapter;
    private List<Friend> requests;
    private String currentUsername;
    private long currentUserID;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        currentUsername = getIntent().getStringExtra("username");
        currentUserID = getIntent().getLongExtra("userId", -1);
        recyclerRequests = findViewById(R.id.recyclerFriends);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));
        requests = new ArrayList<>();
        adapter = new FriendAdapter(this, requests, currentUsername, currentUserID);
        recyclerRequests.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading incoming requests...");
        progressDialog.setCancelable(false);

        loadRequests();
    }

    private void loadRequests() {
        progressDialog.show();
        String url = "http://coms-3090-026.class.las.iastate.edu:8080/api/v8/users/"
                + currentUsername + "/friends/requests/incoming";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressDialog.dismiss();
                    requests.clear();
                    parseRequests(response);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to load requests: " + error.toString(), Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void parseRequests(JSONArray response) {
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Friend f = new Friend();
                f.setId(obj.getLong("id"));
                f.setDisplayName(obj.getString("displayName"));
                f.setUsername(obj.getString("username"));
                f.setStatus(obj.getString("status"));
                requests.add(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
