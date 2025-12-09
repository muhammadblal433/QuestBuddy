package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tripplanner.AddEditTripEventActivity;
import com.example.androidexample.tripplanner.TripEventsActivity;
import com.example.androidexample.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TripEventsActivityTest {

    @Test
    public void tripEventsActivity_launchesSuccessfully() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class);
        intent.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 1L);
        intent.putExtra(TripEventsActivity.EXTRA_USER_ID, 2L);

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
        intent.putExtra(TripEventsActivity.EXTRA_USER_ID, 2L);

        try (ActivityScenario<TripEventsActivity> scenario =
                     ActivityScenario.launch(intent)) {

            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void tripEventsActivity_missingUserId_finishesImmediately() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class);
        intent.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 1L);

        try (ActivityScenario<TripEventsActivity> scenario =
                     ActivityScenario.launch(intent)) {

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

            onView(withId(R.id.recyclerEvents)).check(matches(isDisplayed()));
            onView(withId(R.id.fabAddEvent)).check(matches(isDisplayed()));
            onView(withId(R.id.btnReturn)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickingReturn_finishesActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class);
        intent.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 1L);
        intent.putExtra(TripEventsActivity.EXTRA_USER_ID, 2L);

        try (ActivityScenario<TripEventsActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.btnReturn)).perform(click());

            // Correct way to verify finish()
            scenario.onActivity(activity ->
                    assertTrue(activity.isFinishing())
            );
        }
    }

    @Test
    public void clickingFabAddEvent_opensAddEditTripEventActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class);
        intent.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 1L);
        intent.putExtra(TripEventsActivity.EXTRA_USER_ID, 2L);

        Intents.init();
        try (ActivityScenario<TripEventsActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.fabAddEvent)).perform(click());

            Intents.intended(hasComponent(AddEditTripEventActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }
}