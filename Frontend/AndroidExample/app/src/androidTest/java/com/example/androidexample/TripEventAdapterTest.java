package com.example.androidexample;

import static org.junit.Assert.assertEquals;

import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tripplanner.TripEvent;
import com.example.androidexample.tripplanner.TripEventAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class TripEventAdapterTest {

    @Test
    public void adapter_bindsEventWithoutCrash() {

        TripEvent event = new TripEvent();
        event.name = "Dinner";
        event.startsAt = "2025-01-01T18:00:00Z";
        event.location = "City Center";

        TripEventAdapter adapter = new TripEventAdapter(new TripEventAdapter.OnItemClickListener() {
            @Override public void onItemClick(TripEvent e) {}
            @Override public void onItemLongClick(TripEvent e) {}
            @Override public void onDeleteClick(TripEvent e) {}
        });

        adapter.setItems(Collections.singletonList(event));

        FrameLayout parent =
                new FrameLayout(ApplicationProvider.getApplicationContext());

        // Directly call onCreateViewHolder – ignore returned ViewHolder
        adapter.onCreateViewHolder(parent, 0);

        // Directly call onBindViewHolder using a dummy position (0)
        // This ensures the binding logic runs without crashing
        adapter.onBindViewHolder(adapter.onCreateViewHolder(parent, 0), 0);

        // Validate count
        assertEquals(1, adapter.getItemCount());
    }
}