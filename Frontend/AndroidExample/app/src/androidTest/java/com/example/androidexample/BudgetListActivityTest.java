package com.example.androidexample.budget;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.action.ViewActions.click;

@RunWith(AndroidJUnit4.class)
public class BudgetListActivityTest {

    @Before
    public void setupSession() {
        // Fake login session required by the Activity
        SharedPreferences prefs = ApplicationProvider
                .getApplicationContext()
                .getSharedPreferences("session", android.content.Context.MODE_PRIVATE);

        prefs.edit()
                .putString("username", "testUser")
                .putInt("userId", 1)
                .apply();
    }

    private void launch() {
        ActivityScenario.launch(
                new Intent(
                        ApplicationProvider.getApplicationContext(),
                        BudgetListActivity.class
                )
        );
    }

    @Test
    public void testUIVisible() {
        launch();

        onView(withId(R.id.recyclerBudgets)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAddBudget)).check(matches(isDisplayed()));
        onView(withId(R.id.btnReturnHome)).check(matches(isDisplayed()));
        onView(withId(R.id.btnUpgradePremium)).check(matches(isDisplayed()));
    }

    @Test
    public void testClickAddBudget() {
        launch();
        onView(withId(R.id.btnAddBudget)).perform(click());
    }

    @Test
    public void testClickReturnHome() {
        launch();
        onView(withId(R.id.btnReturnHome)).perform(click());
    }

    @Test
    public void testClickUpgradePremium() {
        launch();
        onView(withId(R.id.btnUpgradePremium)).perform(click());
    }
}
