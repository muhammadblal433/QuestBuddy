package com.example.androidexample;

import static org.junit.Assert.assertEquals;

import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.example.androidexample.packing.PackingAdapter;
import com.example.androidexample.packing.PackingItem;

import org.junit.Test;

import java.util.Arrays;

public class PackingAdapterTest {

    @Test
    public void adapter_bindsWithoutCrashing_andCountsCorrectly() {
        PackingItem item = new PackingItem(1, "Toothbrush");

        PackingAdapter adapter = new PackingAdapter(
                ApplicationProvider.getApplicationContext(),
                Arrays.asList(item),
                null // activity not needed for this unit-style test
        );

        FrameLayout parent = new FrameLayout(ApplicationProvider.getApplicationContext());
        PackingAdapter.PackingViewHolder vh = adapter.onCreateViewHolder(parent, 0);

        // Just ensure binding runs without throwing exceptions
        adapter.onBindViewHolder(vh, 0);

        // Check item count
        assertEquals(1, adapter.getItemCount());
    }
}