package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
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
public class MuhammadSystemTest {

    private ActivityScenario<HomeActivity> launchHome() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        i.putExtra("userId", 52);
        return ActivityScenario.launch(i);
    }

    private ActivityScenario<TripListActivity> launchTripList() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TripListActivity.class);
        i.putExtra("userId", 1);
        return ActivityScenario.launch(i);
    }

    private ActivityScenario<TripChatActivity> launchChat() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TripChatActivity.class);
        i.putExtra("userId", 1L);
        i.putExtra("tripId", 123L);
        return ActivityScenario.launch(i);
    }

    private ActivityScenario<TripInvitesActivity> launchInvites() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TripInvitesActivity.class);
        i.putExtra("userId", 1);
        return ActivityScenario.launch(i);
    }

    private ActivityScenario<TripEventsActivity> launchPlanner() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TripEventsActivity.class);
        i.putExtra(TripEventsActivity.EXTRA_TRIP_ID, 123L);
        i.putExtra(TripEventsActivity.EXTRA_USER_ID, 1L);
        return ActivityScenario.launch(i);
    }

    // Login Tests
    @Test
    public void login_showsFields() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.etEmailLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.etPasswordLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void login_typing() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.etEmailLogin))
                .perform(typeText("test@test.com"), closeSoftKeyboard());

        onView(withId(R.id.etPasswordLogin))
                .perform(typeText("password123"), closeSoftKeyboard());

        onView(withId(R.id.btnLogin)).perform(click());
    }

    // Home Screen Tests
    @Test
    public void home_showsUsernameAndDrawer() {
        launchHome();

        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
    }

    // Trip List Tests
    @Test
    public void tripList_showsUI() {
        launchTripList();

        onView(withId(R.id.recyclerTrips)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAdd)).check(matches(isDisplayed()));
        onView(withId(R.id.btnViewInvites)).check(matches(isDisplayed()));
        onView(withId(R.id.btnReturnHome)).check(matches(isDisplayed()));
    }

    @Test
    public void tripList_openInvites() {
        launchTripList();
        onView(withId(R.id.btnViewInvites)).perform(click());
        onView(withId(R.id.recyclerInvites)).check(matches(isDisplayed()));
    }

    // Invites
    @Test
    public void invites_showList() {
        launchInvites();
        onView(withId(R.id.recyclerInvites)).check(matches(isDisplayed()));
    }

    // Chat
    @Test
    public void chat_sendMessage() {
        launchChat();

        onView(withId(R.id.input))
                .perform(typeText("Hello!"), closeSoftKeyboard());

        onView(withId(R.id.send)).perform(click());
    }

    // Planner
    @Test
    public void planner_showsUI() {
        launchPlanner();

        onView(withId(R.id.recyclerEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.fabAddEvent)).check(matches(isDisplayed()));
    }
}