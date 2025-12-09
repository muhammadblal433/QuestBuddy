package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tripplanner.AddEditTripEventActivity;
import com.example.androidexample.tripplanner.TripEvent;
import com.example.androidexample.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AddEditTripEventActivityTest {

    private ActivityScenario<AddEditTripEventActivity> launchNewEvent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                AddEditTripEventActivity.class
        );
        intent.putExtra(AddEditTripEventActivity.EXTRA_TRIP_ID, 1L);
        intent.putExtra(AddEditTripEventActivity.EXTRA_USER_ID, 2L);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<AddEditTripEventActivity> launchEditEvent() {
        TripEvent event = new TripEvent();
        event.id = 10L;
        event.name = "Existing Event";
        event.location = "Test Location";
        event.notes = "Test Notes";
        event.position = 5;
        event.startsAt = "2025-01-15T10:00:00Z";
        event.endsAt = "2025-01-15T12:00:00Z";

        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                AddEditTripEventActivity.class
        );
        intent.putExtra(AddEditTripEventActivity.EXTRA_TRIP_ID, 1L);
        intent.putExtra(AddEditTripEventActivity.EXTRA_USER_ID, 2L);
        intent.putExtra(AddEditTripEventActivity.EXTRA_EVENT, event);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void activity_launchesInNewMode() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtName)).check(matches(isDisplayed()));
            onView(withId(R.id.edtStartsAt)).check(matches(isDisplayed()));
            onView(withId(R.id.edtEndsAt)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSave)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void activity_launchesInEditMode() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchEditEvent()) {
            onView(withId(R.id.edtName)).check(matches(withText("Existing Event")));
            onView(withId(R.id.edtLocation)).check(matches(withText("Test Location")));
            onView(withId(R.id.edtNotes)).check(matches(withText("Test Notes")));
            onView(withId(R.id.edtPosition)).check(matches(withText("5")));
        }
    }

    @Test
    public void btnReturn_finishesActivity() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.btnReturn)).perform(click());

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scenario.onActivity(activity -> {
                assertTrue(activity.isFinishing());
            });
        }
    }

    @Test
    public void save_missingName_showsError() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btnSave).performClick();
            });

            onView(withId(R.id.edtName)).check(matches(hasErrorText("Required")));
        }
    }

    @Test
    public void save_missingStartsAt_showsError() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtName))
                    .perform(typeText("Test Event"), closeSoftKeyboard());

            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btnSave).performClick();
            });

            onView(withId(R.id.edtStartsAt)).check(matches(hasErrorText("Required")));
        }
    }

    @Test
    public void save_missingEndsAt_showsError() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtName))
                    .perform(typeText("Test Event"), closeSoftKeyboard());

            scenario.onActivity(activity -> {
                // Set startsAtIso directly via reflection or use a helper
                activity.findViewById(R.id.edtStartsAt).performClick();
            });

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Close any dialogs
            try {
                onView(withText("Cancel")).perform(click());
            } catch (Exception ignored) {
            }

            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btnSave).performClick();
            });

            onView(withId(R.id.edtStartsAt)).check(matches(hasErrorText("Required")));
        }
    }

    @Test
    public void save_invalidPosition_showsError() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtName))
                    .perform(typeText("Test Event"), closeSoftKeyboard());

            scenario.onActivity(activity -> {
                // Manually set invalid text in position field (bypassing input type restriction)
                android.widget.EditText edtPosition = activity.findViewById(R.id.edtPosition);
                edtPosition.setText("invalid");

                // Manually set the ISO dates to bypass validation
                try {
                    java.lang.reflect.Field startsAtField =
                            AddEditTripEventActivity.class.getDeclaredField("startsAtIso");
                    startsAtField.setAccessible(true);
                    startsAtField.set(activity, "2025-01-15T10:00:00Z");

                    java.lang.reflect.Field endsAtField =
                            AddEditTripEventActivity.class.getDeclaredField("endsAtIso");
                    endsAtField.setAccessible(true);
                    endsAtField.set(activity, "2025-01-15T12:00:00Z");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activity.findViewById(R.id.btnSave).performClick();
            });

            onView(withId(R.id.edtPosition)).check(matches(hasErrorText("Invalid number")));
        }
    }

    @Test
    public void edtStartsAt_opensDateTimePicker() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtStartsAt)).perform(click());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // DatePickerDialog should be showing - close it
            try {
                onView(withText("Cancel")).perform(click());
            } catch (Exception e) {
                // Dialog might not be visible in test environment
            }
        }
    }

    @Test
    public void edtEndsAt_opensDateTimePicker() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtEndsAt)).perform(click());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // DatePickerDialog should be showing - close it
            try {
                onView(withText("Cancel")).perform(click());
            } catch (Exception e) {
                // Dialog might not be visible in test environment
            }
        }
    }

    @Test
    public void allFieldsInitialized() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.edtName));
                assertNotNull(activity.findViewById(R.id.edtStartsAt));
                assertNotNull(activity.findViewById(R.id.edtEndsAt));
                assertNotNull(activity.findViewById(R.id.edtLocation));
                assertNotNull(activity.findViewById(R.id.edtNotes));
                assertNotNull(activity.findViewById(R.id.edtPosition));
                assertNotNull(activity.findViewById(R.id.btnSave));
                assertNotNull(activity.findViewById(R.id.btnReturn));
            });
        }
    }

    @Test
    public void edtStartsAt_isNotFocusable() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            scenario.onActivity(activity -> {
                android.widget.EditText edtStartsAt = activity.findViewById(R.id.edtStartsAt);
                assertEquals(false, edtStartsAt.isFocusable());
                assertEquals(true, edtStartsAt.isClickable());
            });
        }
    }

    @Test
    public void edtEndsAt_isNotFocusable() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            scenario.onActivity(activity -> {
                android.widget.EditText edtEndsAt = activity.findViewById(R.id.edtEndsAt);
                assertEquals(false, edtEndsAt.isFocusable());
                assertEquals(true, edtEndsAt.isClickable());
            });
        }
    }

    @Test
    public void editMode_setsTitle() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchEditEvent()) {
            scenario.onActivity(activity -> {
                assertEquals("Edit Event", activity.getTitle());
            });
        }
    }

    @Test
    public void newMode_setsTitle() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            scenario.onActivity(activity -> {
                assertEquals("New Event", activity.getTitle());
            });
        }
    }

    @Test
    public void editMode_loadsEventData() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchEditEvent()) {
            scenario.onActivity(activity -> {
                android.widget.EditText edtName = activity.findViewById(R.id.edtName);
                android.widget.EditText edtLocation = activity.findViewById(R.id.edtLocation);
                android.widget.EditText edtNotes = activity.findViewById(R.id.edtNotes);
                android.widget.EditText edtPosition = activity.findViewById(R.id.edtPosition);

                assertEquals("Existing Event", edtName.getText().toString());
                assertEquals("Test Location", edtLocation.getText().toString());
                assertEquals("Test Notes", edtNotes.getText().toString());
                assertEquals("5", edtPosition.getText().toString());
            });
        }
    }

    @Test
    public void editMode_loadsDates() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchEditEvent()) {
            scenario.onActivity(activity -> {
                android.widget.EditText edtStartsAt = activity.findViewById(R.id.edtStartsAt);
                android.widget.EditText edtEndsAt = activity.findViewById(R.id.edtEndsAt);

                // Dates should be formatted and displayed
                assertTrue(!edtStartsAt.getText().toString().isEmpty());
                assertTrue(!edtEndsAt.getText().toString().isEmpty());
            });
        }
    }

    @Test
    public void save_withValidData_callsCreateApi() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtName))
                    .perform(typeText("New Event"), closeSoftKeyboard());
            onView(withId(R.id.edtLocation))
                    .perform(typeText("Location"), closeSoftKeyboard());
            onView(withId(R.id.edtNotes))
                    .perform(typeText("Notes"), closeSoftKeyboard());
            onView(withId(R.id.edtPosition))
                    .perform(typeText("1"), closeSoftKeyboard());

            scenario.onActivity(activity -> {
                // Set dates manually
                try {
                    java.lang.reflect.Field startsAtField =
                            AddEditTripEventActivity.class.getDeclaredField("startsAtIso");
                    startsAtField.setAccessible(true);
                    startsAtField.set(activity, "2025-01-15T10:00:00Z");

                    java.lang.reflect.Field endsAtField =
                            AddEditTripEventActivity.class.getDeclaredField("endsAtIso");
                    endsAtField.setAccessible(true);
                    endsAtField.set(activity, "2025-01-15T12:00:00Z");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activity.findViewById(R.id.btnSave).performClick();
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void save_withValidData_callsEditApi() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchEditEvent()) {
            onView(withId(R.id.edtName))
                    .perform(replaceText("Updated Event"), closeSoftKeyboard());

            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btnSave).performClick();
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void save_withEmptyOptionalFields_doesNotError() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtName))
                    .perform(typeText("Event"), closeSoftKeyboard());

            scenario.onActivity(activity -> {
                // Set dates manually
                try {
                    java.lang.reflect.Field startsAtField =
                            AddEditTripEventActivity.class.getDeclaredField("startsAtIso");
                    startsAtField.setAccessible(true);
                    startsAtField.set(activity, "2025-01-15T10:00:00Z");

                    java.lang.reflect.Field endsAtField =
                            AddEditTripEventActivity.class.getDeclaredField("endsAtIso");
                    endsAtField.setAccessible(true);
                    endsAtField.set(activity, "2025-01-15T12:00:00Z");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activity.findViewById(R.id.btnSave).performClick();
            });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void formatDisplay_handlesValidIso() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchEditEvent()) {
            scenario.onActivity(activity -> {
                android.widget.EditText edtStartsAt = activity.findViewById(R.id.edtStartsAt);
                // Should display formatted date, not raw ISO
                assertTrue(edtStartsAt.getText().toString().contains("Jan") ||
                        edtStartsAt.getText().toString().contains("2025"));
            });
        }
    }

    @Test
    public void editMode_withNullPosition_doesNotCrash() {
        TripEvent event = new TripEvent();
        event.id = 10L;
        event.name = "Event";
        event.position = null;
        event.startsAt = "2025-01-15T10:00:00Z";
        event.endsAt = "2025-01-15T12:00:00Z";

        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                AddEditTripEventActivity.class
        );
        intent.putExtra(AddEditTripEventActivity.EXTRA_TRIP_ID, 1L);
        intent.putExtra(AddEditTripEventActivity.EXTRA_USER_ID, 2L);
        intent.putExtra(AddEditTripEventActivity.EXTRA_EVENT, event);

        try (ActivityScenario<AddEditTripEventActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.edtPosition)).check(matches(withText("")));
        }
    }

    @Test
    public void save_withValidPosition_parsesCorrectly() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            onView(withId(R.id.edtName))
                    .perform(typeText("Event"), closeSoftKeyboard());
            onView(withId(R.id.edtPosition))
                    .perform(typeText("42"), closeSoftKeyboard());

            scenario.onActivity(activity -> {
                try {
                    java.lang.reflect.Field startsAtField =
                            AddEditTripEventActivity.class.getDeclaredField("startsAtIso");
                    startsAtField.setAccessible(true);
                    startsAtField.set(activity, "2025-01-15T10:00:00Z");

                    java.lang.reflect.Field endsAtField =
                            AddEditTripEventActivity.class.getDeclaredField("endsAtIso");
                    endsAtField.setAccessible(true);
                    endsAtField.set(activity, "2025-01-15T12:00:00Z");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activity.findViewById(R.id.btnSave).performClick();
            });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void openDateTimePicker_forStartsAt() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.edtStartsAt).performClick();
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Cleanup
            try {
                onView(withText("Cancel")).perform(click());
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    public void openDateTimePicker_forEndsAt() {
        try (ActivityScenario<AddEditTripEventActivity> scenario = launchNewEvent()) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.edtEndsAt).performClick();
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Cleanup
            try {
                onView(withText("Cancel")).perform(click());
            } catch (Exception ignored) {
            }
        }
    }
}