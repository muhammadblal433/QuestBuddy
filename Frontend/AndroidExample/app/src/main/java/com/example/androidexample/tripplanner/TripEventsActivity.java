package com.example.androidexample.tripplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.androidexample.R;
import com.example.androidexample.tripplanner.TripEvent;
import com.example.androidexample.tripplanner.TripEventPage;
import com.example.androidexample.tripplanner.TripEventApi;


public class TripEventsActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "tripId";
    public static final String EXTRA_USER_ID = "userId";

    private long tripId;
    private long userId;

    private TripEventApi api;
    private TripEventAdapter adapter;
    private ProgressBar progressBar;

    private int currentPage = 0;
    private int pageSize = 50;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_events);

        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1L);
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1L);

        if (tripId <= 0 || userId <= 0) {
            Toast.makeText(this, "Missing tripId or userId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        api = new TripEventApi(this);

        RecyclerView recycler = findViewById(R.id.recyclerEvents);
        progressBar = findViewById(R.id.progressEvents);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddEvent);

        adapter = new TripEventAdapter(new TripEventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TripEvent event) {
                Intent i = new Intent(TripEventsActivity.this, AddEditTripEventActivity.class);
                i.putExtra(AddEditTripEventActivity.EXTRA_TRIP_ID, tripId);
                i.putExtra(AddEditTripEventActivity.EXTRA_USER_ID, userId);
                i.putExtra(AddEditTripEventActivity.EXTRA_EVENT, event);
                startActivity(i);
            }

            @Override
            public void onItemLongClick(TripEvent event) {
                deleteEvent(event);
            }

            @Override
            public void onDeleteClick(TripEvent event) {
                deleteEvent(event);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent i = new Intent(TripEventsActivity.this, AddEditTripEventActivity.class);
            i.putExtra(AddEditTripEventActivity.EXTRA_TRIP_ID, tripId);
            i.putExtra(AddEditTripEventActivity.EXTRA_USER_ID, userId);
            startActivity(i);
        });

        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);

        api.listEvents(
                userId,
                tripId,
                null,
                null,
                currentPage,
                pageSize,
                new TripEventApi.PageCallback() {
                    @Override
                    public void onSuccess(TripEventPage page) {
                        progressBar.setVisibility(View.GONE);
                        adapter.setItems(page.content);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(TripEventsActivity.this, "Error: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void deleteEvent(TripEvent event) {
        api.deleteEvent(
                userId,
                tripId,
                event.id,
                new TripEventApi.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(TripEventsActivity.this,
                                "Event deleted", Toast.LENGTH_SHORT).show();
                        loadEvents();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(TripEventsActivity.this,
                                "Delete failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}
