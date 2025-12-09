package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import com.example.androidexample.tripplanner.TripEventsActivity;
import com.example.androidexample.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TripEventsActivityTest {

    @Test
    public void tripEventsActivity_launchesSuccessfully() {
        // Create intent with required extras
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class);
        intent.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 1L);
        intent.putExtra(TripEventsActivity.EXTRA_USER_ID, 2L);

        // Launch activity WITH the intent
        try (ActivityScenario<TripEventsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerEvents)).check(matches(isDisplayed()));
            onView(withId(R.id.fabAddEvent)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void tripEventsActivity_missingTripId_finishesImmediately() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class);
        // Only add userId, missing tripId
        intent.putExtra(TripEventsActivity.EXTRA_USER_ID, 2L);

        try (ActivityScenario<TripEventsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Activity should be destroyed because it finishes itself
            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void tripEventsActivity_missingUserId_finishesImmediately() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class);
        // Only add tripId, missing userId
        intent.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 1L);

        try (ActivityScenario<TripEventsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Activity should be destroyed because it finishes itself
            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void tripEventsActivity_allUIElementsDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class);
        intent.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 1L);
        intent.putExtra(TripEventsActivity.EXTRA_USER_ID, 2L);

        try (ActivityScenario<TripEventsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Check main UI elements are displayed
            onView(withId(R.id.recyclerEvents)).check(matches(isDisplayed()));
            onView(withId(R.id.fabAddEvent)).check(matches(isDisplayed()));
            onView(withId(R.id.btnReturn)).check(matches(isDisplayed()));
            // Note: progressEvents visibility depends on async API call state
            // So we don't check its visibility here
        }
    }
}