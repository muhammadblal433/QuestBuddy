package com.example.androidexample;

import static org.junit.Assert.assertEquals;

import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.example.androidexample.tasks.Task;
import com.example.androidexample.tasks.TaskAdapter;

import org.junit.Test;

import java.util.Arrays;

public class TaskAdapterTest {

    @Test
    public void adapter_bindsDataWithoutCrashing_andCountsCorrectly() {
        Task task = new Task(
                1,
                "Study",
                "Work on algorithms",
                "Pending",
                "2025-01-01"
        );

        TaskAdapter adapter = new TaskAdapter(
                ApplicationProvider.getApplicationContext(),
                Arrays.asList(task),
                null // activity not required for binding test
        );

        FrameLayout parent = new FrameLayout(ApplicationProvider.getApplicationContext());
        TaskAdapter.TaskViewHolder vh = adapter.onCreateViewHolder(parent, 0);

        // run binding — this covers the core logic
        adapter.onBindViewHolder(vh, 0);

        assertEquals(1, adapter.getItemCount());
    }
}