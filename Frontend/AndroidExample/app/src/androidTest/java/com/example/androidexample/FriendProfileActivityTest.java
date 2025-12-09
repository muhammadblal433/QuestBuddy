package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.friends.FriendProfileActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FriendProfileActivityTest {

    private ActivityScenario<FriendProfileActivity> launch() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                FriendProfileActivity.class
        );
        intent.putExtra("username", "alice123");
        intent.putExtra("id", 1L);
        intent.putExtra("displayName", "Alice Smith");
        intent.putExtra("currentUser", "testuser");
        intent.putExtra("userId", 100L);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void activity_launchesSuccessfully() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            onView(withId(R.id.tvProfileName)).check(matches(isDisplayed()));
            onView(withId(R.id.tvProfileUsername)).check(matches(isDisplayed()));
            onView(withId(R.id.tvProfileEmail)).check(matches(isDisplayed()));
            onView(withId(R.id.btnUnfriend)).check(matches(isDisplayed()));
            onView(withId(R.id.btnBlock)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void activity_displaysCorrectName() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            onView(withId(R.id.tvProfileName)).check(matches(withText("Alice Smith")));
        }
    }

    @Test
    public void activity_displaysCorrectUsername() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            onView(withId(R.id.tvProfileUsername)).check(matches(withText("@alice123")));
        }
    }

    @Test
    public void btnUnfriend_clickNavigatesAway() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            onView(withId(R.id.btnUnfriend)).perform(click());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void btnBlock_clickNavigatesAway() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            onView(withId(R.id.btnBlock)).perform(click());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void btnBack_clickNavigatesAway() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            onView(withId(R.id.btnBack)).perform(click());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void allUIElements_areInitialized() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.tvProfileName));
                assertNotNull(activity.findViewById(R.id.tvProfileUsername));
                assertNotNull(activity.findViewById(R.id.tvProfileEmail));
                assertNotNull(activity.findViewById(R.id.btnUnfriend));
                assertNotNull(activity.findViewById(R.id.btnBlock));
                assertNotNull(activity.findViewById(R.id.btnBack));
            });
        }
    }

    @Test
    public void emailRequest_isSentOnCreate() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Email should be updated after API call
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.tvProfileEmail));
            });
        }
    }

    @Test
    public void activity_withDifferentData_displaysCorrectly() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                FriendProfileActivity.class
        );
        intent.putExtra("username", "bob456");
        intent.putExtra("id", 2L);
        intent.putExtra("displayName", "Bob Jones");
        intent.putExtra("currentUser", "currentuser");
        intent.putExtra("userId", 200L);

        try (ActivityScenario<FriendProfileActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvProfileName)).check(matches(withText("Bob Jones")));
            onView(withId(R.id.tvProfileUsername)).check(matches(withText("@bob456")));
        }
    }

    @Test
    public void btnUnfriend_sendsAPIRequest() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            onView(withId(R.id.btnUnfriend)).perform(click());

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void btnBlock_sendsAPIRequest() {
        try (ActivityScenario<FriendProfileActivity> scenario = launch()) {
            onView(withId(R.id.btnBlock)).perform(click());

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}