package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tripplanner.TripEventsActivity;
import com.example.androidexample.trips.TripChatActivity;
import com.example.androidexample.trips.TripInvitesActivity;
import com.example.androidexample.trips.TripListActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AniroopSystemTest {

    private ActivityScenario<HomeActivity> launchHomeWithUser() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                HomeActivity.class
        );
        intent.putExtra("userId", 52);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<TripListActivity> launchTripList() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                TripListActivity.class
        );
        intent.putExtra("userId", 1);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<TripChatActivity> launchTripChat() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                TripChatActivity.class
        );
        intent.putExtra("userId", 1L);
        intent.putExtra("tripId", 123L);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<TripEventsActivity> launchTripPlanner() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                TripEventsActivity.class
        );
        intent.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 123L);
        intent.putExtra(TripEventsActivity.EXTRA_USER_ID, 1L);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<TripInvitesActivity> launchTripInvites() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                TripInvitesActivity.class
        );
        intent.putExtra("userId", 1);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void loginScreen_showsFieldsAndButton() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.etEmailLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.etPasswordLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void homeScreen_showsDrawerAndUsername() {
        launchHomeWithUser();

        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
    }

    @Test
    public void tripsScreen_showsRecyclerAndButtons() {
        launchTripList();

        onView(withId(R.id.recyclerTrips)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAdd)).check(matches(isDisplayed()));
        onView(withId(R.id.btnViewInvites)).check(matches(isDisplayed()));
        onView(withId(R.id.btnReturnHome)).check(matches(isDisplayed()));
    }

    @Test
    public void invitesScreen_showsList() {
        launchTripInvites();

        onView(withId(R.id.recyclerInvites)).check(matches(isDisplayed()));
    }


    @Test
    public void tripChat_canTypeAndSendMessage() {
        launchTripChat();

        onView(withId(R.id.recycler)).check(matches(isDisplayed()));
        onView(withId(R.id.input)).check(matches(isDisplayed()));
        onView(withId(R.id.send)).check(matches(isDisplayed()));

        onView(withId(R.id.input))
                .perform(typeText("Hello from Espresso!"), closeSoftKeyboard());
        onView(withId(R.id.send)).perform(click());
    }

    @Test
    public void tripPlanner_showsEventsAndFab() {
        launchTripPlanner();

        onView(withId(R.id.recyclerEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.fabAddEvent)).check(matches(isDisplayed()));
    }
}
