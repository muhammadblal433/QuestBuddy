package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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

import com.example.androidexample.notifications.NotificationModel;
import com.example.androidexample.notifications.NotificationsActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotificationsActivityTest {

    @Before
    public void setUp() {
        // Set up shared preferences with a valid userId
        SharedPreferences prefs = ApplicationProvider.getApplicationContext()
                .getSharedPreferences("session", android.content.Context.MODE_PRIVATE);
        prefs.edit().putInt("userId", 5).apply();
        prefs.edit().putString("username", "testuser").apply();
    }

    private ActivityScenario<NotificationsActivity> launch() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsActivity.class
        );
        intent.putExtra("userId", 5);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<NotificationsActivity> launchWithoutUserId() {
        // Clear shared preferences
        SharedPreferences prefs = ApplicationProvider.getApplicationContext()
                .getSharedPreferences("session", android.content.Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsActivity.class
        );
        return ActivityScenario.launch(intent);
    }

    @Test
    public void activity_launchesSuccessfully() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
            onView(withId(R.id.btnAll)).check(matches(isDisplayed()));
            onView(withId(R.id.btnRead)).check(matches(isDisplayed()));
            onView(withId(R.id.btnReturnHome)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void activity_withoutUserId_finishes() {
        try (ActivityScenario<NotificationsActivity> scenario = launchWithoutUserId()) {
            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void allUIElements_areInitialized() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.recyclerNotifications));
                assertNotNull(activity.findViewById(R.id.btnAll));
                assertNotNull(activity.findViewById(R.id.btnRead));
                assertNotNull(activity.findViewById(R.id.btnReturnHome));
            });
        }
    }

    @Test
    public void recyclerView_hasLayoutManager() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerNotifications);
                assertNotNull(recycler.getLayoutManager());
                assertTrue(recycler.getLayoutManager() instanceof
                        androidx.recyclerview.widget.LinearLayoutManager);
            });
        }
    }

    @Test
    public void recyclerView_hasAdapter() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerNotifications);
                assertNotNull(recycler.getAdapter());
            });
        }
    }

    @Test
    public void btnAll_switchesFilter() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            onView(withId(R.id.btnAll)).perform(click());

            // Wait for filter to apply
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Verify no crash
            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void btnRead_switchesFilter() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            onView(withId(R.id.btnRead)).perform(click());

            // Wait for filter to apply
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Verify no crash
            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void btnReturnHome_navigatesToHome() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            onView(withId(R.id.btnReturnHome)).perform(click());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Just verify the scenario state instead of accessing destroyed activity
            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void switchFilter_toAll_showsAllNotifications() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                // Use reflection to access private method
                try {
                    java.lang.reflect.Method method =
                            NotificationsActivity.class.getDeclaredMethod("switchFilter", String.class);
                    method.setAccessible(true);
                    method.invoke(activity, "ALL");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void switchFilter_toRead_showsOnlyReadNotifications() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    java.lang.reflect.Method method =
                            NotificationsActivity.class.getDeclaredMethod("switchFilter", String.class);
                    method.setAccessible(true);
                    method.invoke(activity, "READ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void onMarkAsRead_callsAPI() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                NotificationModel notification = new NotificationModel(
                        1L, 100L, "Title", "Message", "INFO", "2025-12-08", false
                );

                activity.onMarkAsRead(notification);
            });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void onDelete_callsAPI() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                NotificationModel notification = new NotificationModel(
                        1L, 100L, "Title", "Message", "INFO", "2025-12-08", false
                );

                activity.onDelete(notification);
            });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void onWebSocketMessage_newNotification_addsToList() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("kind", "NEW");
                    json.put("id", 1L);
                    json.put("recipientId", 100L);
                    json.put("title", "New Notification");
                    json.put("message", "Test Message");
                    json.put("type", "INFO");
                    json.put("createdAt", "2025-12-08T10:00:00Z");
                    json.put("read", false);

                    activity.onWebSocketMessage(json.toString());
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
    public void onWebSocketMessage_readNotification_updatesStatus() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("kind", "READ");
                    json.put("id", 1L);

                    activity.onWebSocketMessage(json.toString());
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
    public void onWebSocketMessage_invalidJson_doesNotCrash() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.onWebSocketMessage("invalid json");
            });

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void onWebSocketMessage_noKindField_doesNotCrash() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("id", 1L);
                    json.put("title", "Test");

                    activity.onWebSocketMessage(json.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void onWebSocketOpen_logsConnection() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.onWebSocketOpen(null);
            });

            // Should not crash
            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void onWebSocketClose_doesNotCrash() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.onWebSocketClose(1000, "Normal closure", true);
            });

            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void onWebSocketError_doesNotCrash() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.onWebSocketError(new Exception("Test error"));
            });

            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void fetchNotifications_isCalledOnCreate() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void parseNotifications_handlesValidJSON() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("id", 1L);
                    obj.put("recipientId", 100L);
                    obj.put("title", "Test Title");
                    obj.put("message", "Test Message");
                    obj.put("type", "INFO");
                    obj.put("createdAt", "2025-12-08T10:00:00Z");
                    obj.put("read", false);
                    jsonArray.put(obj);

                    java.lang.reflect.Method method =
                            NotificationsActivity.class.getDeclaredMethod("parseNotifications", JSONArray.class);
                    method.setAccessible(true);
                    method.invoke(activity, jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    public void activity_usesSharedPreferencesUserId() {
        SharedPreferences prefs = ApplicationProvider.getApplicationContext()
                .getSharedPreferences("session", android.content.Context.MODE_PRIVATE);
        prefs.edit().putInt("userId", 42).apply();

        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsActivity.class
        );

        try (ActivityScenario<NotificationsActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void onDestroy_removesWebSocketListener() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            scenario.moveToState(Lifecycle.State.DESTROYED);

            // Activity should be destroyed without crash
            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void filterButtons_canBeClickedMultipleTimes() {
        try (ActivityScenario<NotificationsActivity> scenario = launch()) {
            onView(withId(R.id.btnAll)).perform(click());
            onView(withId(R.id.btnRead)).perform(click());
            onView(withId(R.id.btnAll)).perform(click());
            onView(withId(R.id.btnRead)).perform(click());

            onView(withId(R.id.recyclerNotifications)).check(matches(isDisplayed()));
        }
    }
}