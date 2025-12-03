package com.example.androidexample.trips;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.HomeActivity;
import com.example.androidexample.R;
import com.example.androidexample.tripplanner.TripEventsActivity;

import java.util.List;

public class TripListActivity extends AppCompatActivity {

    private Button btnViewInvites;
    private Button btnReturnHome;
    private RecyclerView recycler;
    private View tvEmpty;
    private Button btnAdd;
    private TripAdapter adapter;
    private boolean isLoading = false;

    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Trips");
        setSupportActionBar(toolbar);

        recycler = findViewById(R.id.recyclerTrips);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAdd = findViewById(R.id.btnAdd);
        btnViewInvites = findViewById(R.id.btnViewInvites);
        btnReturnHome  = findViewById(R.id.btnReturnHome);

        userId = getIntent().getIntExtra("userId", -1);

        adapter = new TripAdapter(new TripAdapter.Listener() {
            @Override
            public void onChat(TripDTO t) {
                Intent i = new Intent(TripListActivity.this, TripChatActivity.class);
                i.putExtra(TripEventsActivity.EXTRA_TRIP_ID, (long) t.id);
                i.putExtra(TripEventsActivity.EXTRA_USER_ID, (long) userId);
                i.putExtra("tripName", t.name);
                startActivity(i);
            }
            @Override
            public void onPlanner(TripDTO t) {
                Intent i = new Intent(
                        TripListActivity.this,
                        com.example.androidexample.tripplanner.TripEventsActivity.class
                );

                i.putExtra(TripEventsActivity.EXTRA_TRIP_ID, (long) t.id);
                i.putExtra(TripEventsActivity.EXTRA_USER_ID, (long) userId);


                startActivity(i);
            }

            @Override public void onEdit(TripDTO t) {
                showUpsertDialog(/*isCreate=*/false, t);
            }
            @Override public void onDelete(TripDTO t) {
                confirmDelete(t);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recycler.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showUpsertDialog(true, null));

        btnViewInvites.setOnClickListener(v -> {
            Intent i = new Intent(TripListActivity.this, TripInvitesActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
        });

        btnReturnHome.setOnClickListener(v -> {
            Intent i = new Intent(TripListActivity.this, HomeActivity.class);
            i.putExtra("userId", userId);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        loadData();
    }

    private void loadData() {
        if (isLoading) return;
        isLoading = true;

        TripAPI.fetchTrips(this, userId, new TripAPI.ListCallback() {
            @Override public void onSuccess(List<TripDTO> trips) {
                isLoading = false;
                adapter.submit(trips);
                tvEmpty.setVisibility((trips == null || trips.isEmpty()) ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(String message) {
                isLoading = false;
                if (adapter.getItemCount() == 0) tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(TripListActivity.this, "Failed to load trips: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // --------- Create/Update dialog ----------
    private void showUpsertDialog(boolean isCreate, @Nullable TripDTO existing) {
        // Build a tiny form inline (no extra XML needed)
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        form.setPadding(pad, pad, pad, 0);

        EditText etName = new EditText(this);
        etName.setHint("Name");
        etName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        form.addView(etName);

        EditText etDestination = new EditText(this);
        etDestination.setHint("Destination");
        form.addView(etDestination);

        EditText etStartDate = new EditText(this);
        etStartDate.setHint("Start Date (YYYY-MM-DD)");
        form.addView(etStartDate);

        EditText etEndDate = new EditText(this);
        etEndDate.setHint("End Date (YYYY-MM-DD)");
        form.addView(etEndDate);

        if (!isCreate && existing != null) {
            if (existing.name != null) etName.setText(existing.name);
            if (existing.destination != null) etDestination.setText(existing.destination);
            if (existing.startDate != null) etStartDate.setText(existing.startDate);
            if (existing.endDate != null) etEndDate.setText(existing.endDate);
        }

        new AlertDialog.Builder(this)
                .setTitle(isCreate ? "Create Trip" : "Edit Trip")
                .setView(form)
                .setPositiveButton(isCreate ? "Create" : "Save", (d, w) -> {
                    TripDTO t = new TripDTO();
                    t.name = etName.getText().toString().trim();
                    t.destination = etDestination.getText().toString().trim();
                    t.startDate = etStartDate.getText().toString().trim();
                    t.endDate = etEndDate.getText().toString().trim();

                    if (isCreate) {
                        TripAPI.createTrip(TripListActivity.this, userId, t, new TripAPI.OneCallback() {
                            @Override public void onSuccess(TripDTO trip) {
                                Toast.makeText(TripListActivity.this, "Created", Toast.LENGTH_SHORT).show();
                                loadData();
                            }
                            @Override public void onError(String message) {
                                Toast.makeText(TripListActivity.this, "Create failed: " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        TripAPI.updateTrip(TripListActivity.this, userId, existing.id, t, new TripAPI.OneCallback()
                        {
                            @Override public void onSuccess(TripDTO trip) {
                                Toast.makeText(TripListActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                loadData();
                            }
                            @Override public void onError(String message) {
                                Toast.makeText(TripListActivity.this, "Update failed: " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --------- Delete confirm ----------
    private void confirmDelete(TripDTO t) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Trip?")
                .setMessage("Are you sure you want to delete \"" + (t.name != null ? t.name : "this trip") + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    TripAPI.deleteTrip(this, userId, t.id, new TripAPI.VoidCallback() {
                        @Override public void onSuccess() {
                            Toast.makeText(TripListActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            loadData();
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(TripListActivity.this, "Delete failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
