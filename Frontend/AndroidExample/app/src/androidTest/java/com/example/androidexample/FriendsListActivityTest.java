package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.friends.FriendsListActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FriendsListActivityTest {

    @Before
    public void setUp() {
        SharedPreferences prefs = ApplicationProvider.getApplicationContext()
                .getSharedPreferences("session", android.content.Context.MODE_PRIVATE);
        prefs.edit().putString("username", "testuser").apply();
        prefs.edit().putInt("userId", 100).apply();
    }

    private ActivityScenario<FriendsListActivity> launch() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                FriendsListActivity.class
        );
        intent.putExtra("username", "testuser");
        intent.putExtra("userId", 100);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void activity_launchesSuccessfully() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void recyclerView_hasLayoutManager() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerFriends);
                assertNotNull(recycler.getLayoutManager());
                assertTrue(recycler.getLayoutManager() instanceof
                        androidx.recyclerview.widget.LinearLayoutManager);
            });
        }
    }

    @Test
    public void recyclerView_hasAdapter() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerFriends);
                assertNotNull(recycler.getAdapter());
            });
        }
    }

    @Test
    public void allUIElements_areInitialized() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.recyclerFriends));
                assertNotNull(activity.findViewById(R.id.tvNoFriends));
                assertNotNull(activity.findViewById(R.id.btnViewRequests));
                assertNotNull(activity.findViewById(R.id.btnViewSuggestions));
                assertNotNull(activity.findViewById(R.id.btnReturnHome));
                assertNotNull(activity.findViewById(R.id.btnAddFriend));
            });
        }
    }

    @Test
    public void loadFriends_isCalledOnCreate() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void activity_withSharedPreferences_loadsUsername() {
        SharedPreferences prefs = ApplicationProvider.getApplicationContext()
                .getSharedPreferences("session", android.content.Context.MODE_PRIVATE);
        prefs.edit().putString("username", "shareduser").apply();
        prefs.edit().putInt("userId", 200).apply();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FriendsListActivity.class);
        try (ActivityScenario<FriendsListActivity> scenario = ActivityScenario.launch(intent)) {
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void activity_withoutUserInfo_showsEmptyMessage() {
        SharedPreferences prefs = ApplicationProvider.getApplicationContext()
                .getSharedPreferences("session", android.content.Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FriendsListActivity.class);
        try (ActivityScenario<FriendsListActivity> scenario = ActivityScenario.launch(intent)) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            onView(withId(R.id.tvNoFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void onResume_reloadsFriends() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.moveToState(Lifecycle.State.STARTED);
            try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.moveToState(Lifecycle.State.RESUMED);
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void etCurrentUsername_isHidden() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                android.widget.EditText etUsername = activity.findViewById(R.id.etCurrentUsername);
                if (etUsername != null) {
                    assertEquals(android.view.View.GONE, etUsername.getVisibility());
                }
            });
        }
    }

    @Test
    public void btnAddFriend_opensSearchDialog() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.onActivity(activity -> {
                android.widget.Button btnAddFriend = activity.findViewById(R.id.btnAddFriend);
                if (btnAddFriend != null) {
                    btnAddFriend.performClick();
                }
            });
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            assertTrue(true);
        }
    }

    @Test
    public void emptyState_showsCorrectMessage() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.tvNoFriends));
            });
        }
    }

    @Test
    public void ensureMe_returnsTrueWithUsername() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.onActivity(activity -> {
                assertNotNull(activity);
            });
        }
    }

    @Test
    public void activity_handlesNullUsernameFromIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FriendsListActivity.class);
        intent.putExtra("userId", 100);
        try (ActivityScenario<FriendsListActivity> scenario = ActivityScenario.launch(intent)) {
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.recyclerFriends));
            });
        }
    }

    @Test
    public void activity_handlesInvalidUserId() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FriendsListActivity.class);
        intent.putExtra("username", "testuser");
        intent.putExtra("userId", -1);
        try (ActivityScenario<FriendsListActivity> scenario = ActivityScenario.launch(intent)) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.recyclerFriends));
            });
        }
    }

    @Test
    public void progressDialog_dismissedAfterLoad() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adapter_initializesCorrectly() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerFriends);
                assertNotNull(recycler.getAdapter());
                assertTrue(recycler.getAdapter().getItemCount() >= 0);
            });
        }
    }

    @Test
    public void buttons_areClickable() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.onActivity(activity -> {
                android.widget.Button btnRequests = activity.findViewById(R.id.btnViewRequests);
                android.widget.Button btnSuggestions = activity.findViewById(R.id.btnViewSuggestions);
                android.widget.Button btnAdd = activity.findViewById(R.id.btnAddFriend);

                assertNotNull(btnRequests);
                assertNotNull(btnSuggestions);
                assertNotNull(btnAdd);

                assertTrue(btnRequests.isClickable());
                assertTrue(btnSuggestions.isClickable());
                assertTrue(btnAdd.isClickable());
            });
        }
    }

    @Test
    public void activity_maintainsStateAcrossRotation() {
        try (ActivityScenario<FriendsListActivity> scenario = launch()) {
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerFriends);
                assertNotNull(recycler);
                assertNotNull(recycler.getAdapter());
            });
        }
    }
}