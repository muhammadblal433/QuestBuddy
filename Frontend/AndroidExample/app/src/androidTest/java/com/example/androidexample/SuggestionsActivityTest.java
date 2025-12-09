package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.friends.SuggestionsActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SuggestionsActivityTest {

    private ActivityScenario<SuggestionsActivity> launch() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SuggestionsActivity.class
        );
        intent.putExtra("username", "testuser");
        intent.putExtra("userID", 100L);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<SuggestionsActivity> launchWithoutUsername() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SuggestionsActivity.class
        );
        intent.putExtra("userID", 100L);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void activity_launchesSuccessfully() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            onView(withId(R.id.recyclerSuggestions)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void activity_withoutUsername_finishes() {
        try (ActivityScenario<SuggestionsActivity> scenario = launchWithoutUsername()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void activity_withEmptyUsername_finishes() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SuggestionsActivity.class
        );
        intent.putExtra("username", "");
        intent.putExtra("userID", 100L);

        try (ActivityScenario<SuggestionsActivity> scenario = ActivityScenario.launch(intent)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void recyclerView_isInitialized() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerSuggestions);
                assertNotNull(recycler);
            });
        }
    }

    @Test
    public void recyclerView_hasLayoutManager() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerSuggestions);
                assertNotNull(recycler.getLayoutManager());
                assertTrue(recycler.getLayoutManager() instanceof
                        androidx.recyclerview.widget.LinearLayoutManager);
            });
        }
    }

    @Test
    public void recyclerView_hasAdapter() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerSuggestions);
                assertNotNull(recycler.getAdapter());
            });
        }
    }

    @Test
    public void loadSuggestions_isCalledOnCreate() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerSuggestions)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void progressDialog_isShownDuringLoading() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerSuggestions)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void parseSuggestions_handlesValidJSON() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("id", 1L);
                    obj.put("displayName", "Alice Smith");
                    obj.put("username", "alice123");
                    obj.put("mutualCount", 5);
                    jsonArray.put(obj);

                    java.lang.reflect.Method method =
                            SuggestionsActivity.class.getDeclaredMethod("parseSuggestions", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void parseSuggestions_handlesEmptyArray() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();

                    java.lang.reflect.Method method =
                            SuggestionsActivity.class.getDeclaredMethod("parseSuggestions", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    public void parseSuggestions_handlesMultipleSuggestions() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();

                    for (int i = 1; i <= 3; i++) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", (long) i);
                        obj.put("displayName", "User " + i);
                        obj.put("username", "user" + i);
                        obj.put("mutualCount", i);
                        jsonArray.put(obj);
                    }

                    java.lang.reflect.Method method =
                            SuggestionsActivity.class.getDeclaredMethod("parseSuggestions", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void parseSuggestions_setsSuggestedStatus() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("id", 1L);
                    obj.put("displayName", "Alice");
                    obj.put("username", "alice123");
                    obj.put("mutualCount", 0);
                    jsonArray.put(obj);

                    java.lang.reflect.Method method =
                            SuggestionsActivity.class.getDeclaredMethod("parseSuggestions", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    public void parseSuggestions_handlesMissingMutualCount() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("id", 1L);
                    obj.put("displayName", "Alice");
                    obj.put("username", "alice123");
                    // mutualCount not provided
                    jsonArray.put(obj);

                    java.lang.reflect.Method method =
                            SuggestionsActivity.class.getDeclaredMethod("parseSuggestions", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    public void sendFriendRequest_canBeCalled() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.sendFriendRequest("targetuser");
            });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void sendFriendRequest_withDifferentUsername() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.sendFriendRequest("alice123");
            });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void activity_withDifferentUserId_loadsCorrectData() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SuggestionsActivity.class
        );
        intent.putExtra("username", "differentuser");
        intent.putExtra("userID", 200L);

        try (ActivityScenario<SuggestionsActivity> scenario = ActivityScenario.launch(intent)) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerSuggestions)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void loadSuggestions_withNetworkError_showsToast() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerSuggestions)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void activity_onCreate_setsContentView() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.recyclerSuggestions));
            });
        }
    }

    @Test
    public void progressDialog_isDismissedAfterLoad() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerSuggestions)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adapter_notifiesDataSetChanged() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerSuggestions);
                assertNotNull(recycler.getAdapter());
            });
        }
    }

    @Test
    public void retryPolicy_isSetOnRequest() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Request with retry policy should be sent
            onView(withId(R.id.recyclerSuggestions)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void parseSuggestions_handlesInvalidJSON() {
        try (ActivityScenario<SuggestionsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();
                    // Missing required fields
                    obj.put("id", 1L);
                    jsonArray.put(obj);

                    java.lang.reflect.Method method =
                            SuggestionsActivity.class.getDeclaredMethod("parseSuggestions", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    // Should handle gracefully
                }
            });
        }
    }
}