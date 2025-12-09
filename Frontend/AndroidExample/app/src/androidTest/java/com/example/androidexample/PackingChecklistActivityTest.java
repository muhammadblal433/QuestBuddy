package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.packing.PackingChecklistActivity;
import com.example.androidexample.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PackingChecklistActivityTest {

    private ActivityScenario<PackingChecklistActivity> launch() {
        Intent i = new Intent(
                ApplicationProvider.getApplicationContext(),
                PackingChecklistActivity.class
        );
        i.putExtra("userId", 50);
        return ActivityScenario.launch(i);
    }

    @Test
    public void packingChecklist_showsUI() {
        launch();

        onView(withId(R.id.recyclerPacking)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAddItem)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackHome)).check(matches(isDisplayed()));
    }

    @Test
    public void pressing_addItem_opensDialog() {
        launch();
        onView(withId(R.id.btnAddItem)).perform(click());
        onView(withText("Add New Item")).check(matches(isDisplayed()));
    }
}