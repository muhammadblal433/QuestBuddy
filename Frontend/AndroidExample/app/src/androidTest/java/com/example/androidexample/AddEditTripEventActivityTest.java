package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import com.example.androidexample.tripplanner.AddEditTripEventActivity;
import com.example.androidexample.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AddEditTripEventActivityTest {

    @Test
    public void save_missingName_showsError() {
        Intent i = new Intent();
        i.putExtra(AddEditTripEventActivity.EXTRA_TRIP_ID, 1L);
        i.putExtra(AddEditTripEventActivity.EXTRA_USER_ID, 2L);

        try (ActivityScenario<AddEditTripEventActivity> scenario =
                     ActivityScenario.launch(AddEditTripEventActivity.class)) {

            onView(withId(R.id.edtStartsAt)).perform(replaceText("Start"));
            onView(withId(R.id.edtEndsAt)).perform(replaceText("End"));

            onView(withId(R.id.btnSave)).perform(click());

            onView(withId(R.id.edtName)).check(matches(hasErrorText("Required")));
        }
    }
}