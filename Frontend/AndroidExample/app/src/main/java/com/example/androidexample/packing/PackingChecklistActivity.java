package com.example.androidexample.packing;
import com.example.androidexample.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.HomeActivity;
import com.example.androidexample.LoginActivity;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PackingChecklistActivity extends AppCompatActivity {

    private RecyclerView recyclerPacking;
    private PackingAdapter adapter;
    private List<PackingItem> itemList = new ArrayList<>();
    private RequestQueue queue;
    private TextView tvAddHint;
    private int userId;
    //base url for api requests(mock server)
    private final String BASE_URL = "https://9d69c0d2-75cf-44b1-9f47-913ed20bc612.mock.pstmn.io/packing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_checklist);

        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // connect the layut views
        recyclerPacking = findViewById(R.id.recyclerPacking);
        recyclerPacking.setLayoutManager(new LinearLayoutManager(this));


        tvAddHint = findViewById(R.id.tvAddHint);
        queue = Volley.newRequestQueue(this);

        //setup the adapter for recycler view
        adapter = new PackingAdapter(this, itemList, this);
        recyclerPacking.setAdapter(adapter);

        // setup the buttons
        Button btnAddItem = findViewById(R.id.btnAddItem);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        //open dialog to add a new item
        btnAddItem.setOnClickListener(v -> showAddItemDialog());
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(PackingChecklistActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);}); // goes back to previous screen
    }

    //shows the dialog for the user to edit
    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        EditText etItemName = dialogView.findViewById(R.id.etItemName);

        new AlertDialog.Builder(this)
                .setTitle("Add New Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etItemName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addItem(name);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // sends post request to add new item in the mock server and it updates locally as well
    private void addItem(String itemName) {
        JSONObject itemJson = new JSONObject();
        try {
            itemJson.put("name", itemName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL,
                itemJson,
                response -> {
                    Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();

                    // Add item locally so it appears immediately
                    itemList.add(new PackingItem(itemList.size() + 1, itemName));
                    adapter.notifyDataSetChanged();
                    tvAddHint.setVisibility(View.GONE);
                },
                error -> Toast.makeText(this, "POST Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    // sends delete request to api and removes items from list
    public void deleteItem(long itemId) {

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                BASE_URL,
                null,
                response -> {
                    Toast.makeText(this, "Item deleted!", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < itemList.size(); i++) {
                        if (itemList.get(i).getId() == itemId) {
                            itemList.remove(i);
                            break;
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (itemList.isEmpty()) {
                        tvAddHint.setVisibility(View.VISIBLE);
                    }
                },
                error -> Toast.makeText(this, "DELETE Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}

