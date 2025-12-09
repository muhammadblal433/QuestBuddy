package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tasks.TaskManagerActivity;
import com.example.androidexample.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TaskManagerActivityTest {

    @Test
    public void taskManagerActivity_launchesAndShowsRecyclerView() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                TaskManagerActivity.class
        );
        intent.putExtra("userId", 5); // any valid ID is fine

        try (ActivityScenario<TaskManagerActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
            onView(withId(R.id.btnAddTask)).check(matches(isDisplayed()));
            onView(withId(R.id.btnHome)).check(matches(isDisplayed()));
        }
    }
}