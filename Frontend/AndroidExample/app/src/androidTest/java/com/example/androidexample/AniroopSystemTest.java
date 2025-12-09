package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.friends.FriendsListActivity;
import com.example.androidexample.friends.FriendProfileActivity;
import com.example.androidexample.friends.RequestsActivity;
import com.example.androidexample.friends.SuggestionsActivity;
import com.example.androidexample.messages.DirectChatActivity;
import com.example.androidexample.notifications.NotificationsActivity;
import com.example.androidexample.packing.PackingChecklistActivity;
import com.example.androidexample.tasks.TaskManagerActivity;
import com.example.androidexample.budget.BudgetListActivity;
import com.example.androidexample.budget.BudgetDetailActivity;
import com.example.androidexample.payments.PremiumActivity;
import com.example.androidexample.trips.TripListActivity;
import com.example.androidexample.trips.TripChatActivity;
import com.example.androidexample.tripplanner.TripEventsActivity;
import com.example.androidexample.trips.TripInvitesActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AniroopSystemTest {

    private final long TEST_USER_ID = 1L;
    private final long TEST_TRIP_ID = 1L;
    private final String TEST_USERNAME = "testUser";

    // Helps the UI settle after async network calls
    private void pause() {
        try { Thread.sleep(600); } catch (Exception ignored) {}
    }

    // ---------------- FRIENDS ----------------

    @Test
    public void friendsList_buttonsClick() {
        ActivityScenario.launch(FriendsListActivity.class);
        pause();

        onView(withId(R.id.btnViewRequests)).perform(click());
        pause();
        onView(withId(R.id.btnViewSuggestions)).perform(click());
        pause();
        onView(withId(R.id.btnAddFriend)).perform(click());
    }

    @Test
    public void suggestions_UI() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), SuggestionsActivity.class);
        i.putExtra("username", TEST_USERNAME);
        i.putExtra("userId", TEST_USER_ID);

        ActivityScenario.launch(i);
        pause();

        onView(withId(R.id.recyclerSuggestions)).check(matches(isDisplayed()));
    }

    // ---------------- DIRECT CHAT ----------------

    @Test
    public void directChat_UI() {
        ActivityScenario.launch(DirectChatActivity.class);
        pause();

        onView(withId(R.id.rvMessages)).check(matches(isDisplayed()));
        onView(withId(R.id.etMessage)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSend)).check(matches(isDisplayed()));
    }

    @Test
    public void directChat_sendMessage() {
        ActivityScenario.launch(DirectChatActivity.class);
        pause();

        onView(withId(R.id.etMessage)).perform(typeText("Hello"), closeSoftKeyboard());
        onView(withId(R.id.btnSend)).perform(click());
    }

    // ---------------- NOTIFICATIONS ----------------

    @Test
    public void notifications_UI() {
        setFakeSession();

        Intent i = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsActivity.class
        );

        i.putExtra("userId", 1);
        i.putExtra("username", "testUser");

        ActivityScenario.launch(i);

        onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAll)).check(matches(isDisplayed()));
        onView(withId(R.id.btnRead)).check(matches(isDisplayed()));
    }

    // ---------------- PACKING ----------------

    @Test
    public void packingChecklist_UI() {
        setFakeSession(); // REQUIRED for all screens that use authenticated API calls

        Intent i = new Intent(
                ApplicationProvider.getApplicationContext(),
                PackingChecklistActivity.class
        );

        // PackingChecklistActivity requires this
        i.putExtra("userId", 1);

        ActivityScenario.launch(i);
        pause();

        onView(withId(R.id.btnAddItem)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerPacking)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackHome)).check(matches(isDisplayed()));
    }

    // ---------------- TASK MANAGER ----------------

    @Test
    public void taskManager_UI() {
        setFakeSession();  // REQUIRED for authenticated API calls

        Intent i = new Intent(
                ApplicationProvider.getApplicationContext(),
                TaskManagerActivity.class
        );
        i.putExtra("userId", 1);  // REQUIRED or activity will auto-redirect to Login

        ActivityScenario.launch(i);
        pause();

        onView(withId(R.id.btnAddTask)).check(matches(isDisplayed()));
        onView(withId(R.id.btnHome)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
    }

    @Test
    public void taskManager_add() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                TaskManagerActivity.class
        );
        intent.putExtra("userId", 1);

        ActivityScenario.launch(intent);

        try { Thread.sleep(500); } catch (Exception ignored) {}

        onView(withId(R.id.btnAddTask)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAddTask)).perform(click());
    }

    private void setFakeSession() {
        Context ctx = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit().putInt("userId", 1).apply();
    }


    // ---------------- PREMIUM ----------------

    public void premium_UI() {
        setFakeSession();
        ActivityScenario.launch(PremiumActivity.class);

        onView(withId(R.id.btnUpgradePremium)).check(matches(isDisplayed()));
        onView(withId(R.id.btnReturnHome)).check(matches(isDisplayed()));
    }

    // ---------------- TRIP LIST ----------------

    @Test
    public void tripList_UI() {
        ActivityScenario.launch(TripListActivity.class);
        pause();

        onView(withId(R.id.recyclerTrips)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAdd)).check(matches(isDisplayed()));
        onView(withId(R.id.btnViewInvites)).check(matches(isDisplayed()));
    }

    // ---------------- TRIP CHAT ----------------

    @Test
    public void tripChat_UI() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TripChatActivity.class);
        i.putExtra("tripId", TEST_TRIP_ID);
        i.putExtra("userId", TEST_USER_ID);

        ActivityScenario.launch(i);
        pause();

        onView(withId(R.id.recycler)).check(matches(isDisplayed()));
        onView(withId(R.id.input)).check(matches(isDisplayed()));
        onView(withId(R.id.send)).check(matches(isDisplayed()));
    }

    @Test
    public void tripChat_send() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TripChatActivity.class);
        i.putExtra("tripId", TEST_TRIP_ID);
        i.putExtra("userId", TEST_USER_ID);

        ActivityScenario.launch(i);
        pause();

        onView(withId(R.id.input)).perform(typeText("Hi"), closeSoftKeyboard());
        onView(withId(R.id.send)).perform(click());
    }

    // ---------------- TRIP EVENTS ----------------

    @Test
    public void tripEvents_UI() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TripEventsActivity.class);
        i.putExtra(TripEventsActivity.EXTRA_TRIP_ID, TEST_TRIP_ID);
        i.putExtra(TripEventsActivity.EXTRA_USER_ID, TEST_USER_ID);

        ActivityScenario.launch(i);
        pause();

        onView(withId(R.id.recyclerEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.fabAddEvent)).check(matches(isDisplayed()));
    }
}
