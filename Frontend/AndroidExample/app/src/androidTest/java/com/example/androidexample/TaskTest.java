package com.example.androidexample;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tasks.Task;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TaskTest {

    @Test
    public void task_gettersReturnCorrectValues() {
        Task task = new Task(
                101,
                "Finish Project",
                "Complete the backend integration",
                "Pending",
                "2025-12-31"
        );

        assertEquals(101, task.getTaskId());
        assertEquals("Finish Project", task.getTitle());
        assertEquals("Complete the backend integration", task.getDescription());
        assertEquals("Pending", task.getStatus());
        assertEquals("2025-12-31", task.getDueDate());
    }
}