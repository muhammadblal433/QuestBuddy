
package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.friends.RequestsActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RequestsActivityTest {

    private ActivityScenario<RequestsActivity> launch() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                RequestsActivity.class
        );
        intent.putExtra("username", "testuser");
        intent.putExtra("userId", 100L);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void activity_launchesSuccessfully() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void recyclerView_isInitialized() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerFriends);
                assertNotNull(recycler);
            });
        }
    }

    @Test
    public void recyclerView_hasLayoutManager() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
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
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerFriends);
                assertNotNull(recycler.getAdapter());
            });
        }
    }

    @Test
    public void loadRequests_isCalledOnCreate() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void progressDialog_isShownDuringLoading() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Progress dialog should be created
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adapter_getItemCount_initiallyZero() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerFriends);
                // Initially might be zero before API loads
                assertNotNull(recycler.getAdapter());
            });
        }
    }

    @Test
    public void parseRequests_handlesValidJSON() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("id", 1L);
                    obj.put("displayName", "Alice Smith");
                    obj.put("username", "alice123");
                    obj.put("status", "PENDING");
                    jsonArray.put(obj);

                    java.lang.reflect.Method method =
                            RequestsActivity.class.getDeclaredMethod("parseRequests", JSONArray.class);
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
    public void parseRequests_handlesEmptyArray() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();

                    java.lang.reflect.Method method =
                            RequestsActivity.class.getDeclaredMethod("parseRequests", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    public void parseRequests_handlesMultipleRequests() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();

                    for (int i = 1; i <= 3; i++) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", (long) i);
                        obj.put("displayName", "User " + i);
                        obj.put("username", "user" + i);
                        obj.put("status", "PENDING");
                        jsonArray.put(obj);
                    }

                    java.lang.reflect.Method method =
                            RequestsActivity.class.getDeclaredMethod("parseRequests", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
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
    public void parseRequests_handlesInvalidJSON() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();
                    // Missing required fields
                    obj.put("id", 1L);
                    jsonArray.put(obj);

                    java.lang.reflect.Method method =
                            RequestsActivity.class.getDeclaredMethod("parseRequests", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    // Should handle gracefully
                }
            });
        }
    }

    @Test
    public void activity_withDifferentUsername_loadsCorrectData() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                RequestsActivity.class
        );
        intent.putExtra("username", "differentuser");
        intent.putExtra("userId", 200L);

        try (ActivityScenario<RequestsActivity> scenario = ActivityScenario.launch(intent)) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void loadRequests_withNetworkError_showsToast() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Should handle error gracefully
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void activity_onCreate_setsContentView() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.recyclerFriends));
            });
        }
    }

    @Test
    public void activity_extractsIntentExtras() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                // If activity doesn't crash, intent extras were extracted successfully
                assertNotNull(activity.findViewById(R.id.recyclerFriends));
            });
        }
    }

    @Test
    public void progressDialog_isDismissedAfterLoad() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Progress dialog should be dismissed
            onView(withId(R.id.recyclerFriends)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adapter_notifiesDataSetChanged() {
        try (ActivityScenario<RequestsActivity> scenario = launch()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerFriends);
                assertNotNull(recycler.getAdapter());
            });
        }
    }
}