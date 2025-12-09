package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.payments.PremiumActivity;
import com.example.androidexample.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PremiumActivityTest {

    private ActivityScenario<PremiumActivity> launch() {

        SharedPreferences prefs =
                ApplicationProvider.getApplicationContext()
                        .getSharedPreferences("session", 0);

        prefs.edit().putInt("userId", 100).apply();

        return ActivityScenario.launch(PremiumActivity.class);
    }

    @Test
    public void premiumScreen_showsUI() {
        launch();

        onView(withId(R.id.tvPremiumTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvPremiumFeatures)).check(matches(isDisplayed()));
        onView(withId(R.id.btnUpgradePremium)).check(matches(isDisplayed()));
        onView(withId(R.id.btnReturnHome)).check(matches(isDisplayed()));
    }

    @Test
    public void upgradeButton_isClickable() {
        launch();
        onView(withId(R.id.btnUpgradePremium)).perform(click());
    }

    @Test
    public void returnHomeButton_isClickable() {
        launch();
        onView(withId(R.id.btnReturnHome)).perform(click());
    }
}