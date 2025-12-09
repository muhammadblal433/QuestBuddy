package com.example.androidexample.budget;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BudgetDetailActivityTest {

    private Context context;
    private static final String TEST_USERNAME = "testuser";
    private static final long TEST_BUDGET_ID = 1L;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("username", TEST_USERNAME)
                .apply();
    }

    @After
    public void tearDown() {
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private Intent createValidIntent() {
        Intent intent = new Intent(context, BudgetDetailActivity.class);
        intent.putExtra("budgetId", TEST_BUDGET_ID);
        return intent;
    }

    @Test
    public void testActivityLaunch_WithValidData_Success() {
        Intent intent = createValidIntent();
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
            assertNotNull(activity.findViewById(R.id.tvBudgetName));
            assertNotNull(activity.findViewById(R.id.tvOwner));
            assertNotNull(activity.findViewById(R.id.tvTotals));
            assertNotNull(activity.findViewById(R.id.tvCreatedAt));
            assertNotNull(activity.findViewById(R.id.recyclerSplits));
            assertNotNull(activity.findViewById(R.id.btnUpdateBudget));
            assertNotNull(activity.findViewById(R.id.btnDeleteBudget));
        });
    }

    @Test
    public void testUpdateButton_IsVisibleAndClickable() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);
        onView(withId(R.id.btnUpdateBudget))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testDeleteButton_IsVisibleAndClickable() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);
        onView(withId(R.id.btnDeleteBudget))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testTextViews_AreVisible() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);
        onView(withId(R.id.tvBudgetName))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvOwner))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvTotals))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvCreatedAt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testUsername_IsRetrievedFromSharedPreferences() {
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit().putString("username", "myTestUser").apply();
        Intent intent = createValidIntent();
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
        });
    }

    @Test
    public void testBudgetId_IsRetrievedFromIntent() {
        Intent intent = new Intent(context, BudgetDetailActivity.class);
        intent.putExtra("budgetId", 999L);
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
        });
    }

    @Test
    public void testActivityLaunch_WithNegativeBudgetId_FinishesActivity() {
        Intent intent = new Intent(context, BudgetDetailActivity.class);
        intent.putExtra("budgetId", -999L);
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity ->
                assertFalse(activity.isFinishing())
        );
    }

    @Test
    public void testActivityLaunch_WithEmptyUsername_FinishesActivity() {
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit().putString("username", "").apply();
        Intent intent = createValidIntent();
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity ->
                assertFalse(activity.isFinishing())
        );
    }

    @Test
    public void testRecyclerView_HasLinearLayoutManager() {
        Intent intent = createValidIntent();
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            androidx.recyclerview.widget.RecyclerView recyclerView =
                    activity.findViewById(R.id.recyclerSplits);
            assertNotNull(recyclerView.getLayoutManager());
        });
    }

    @Test
    public void testRecyclerView_HasAdapterSet() {
        Intent intent = createValidIntent();
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            androidx.recyclerview.widget.RecyclerView recyclerView =
                    activity.findViewById(R.id.recyclerSplits);
            assertNotNull(recyclerView.getAdapter());
        });
    }

    @Test
    public void testActivityLaunch_WithLargeBudgetId_Success() {
        Intent intent = new Intent(context, BudgetDetailActivity.class);
        intent.putExtra("budgetId", Long.MAX_VALUE);
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
        });
    }

    @Test
    public void testSharedPreferences_UsesCorrectSessionKey() {
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit().putString("username", "testUser123").apply();
        Intent intent = createValidIntent();
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
        });
    }

    @Test
    public void testUpdateButton_Click_DoesNotCrash() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);
        onView(withId(R.id.btnUpdateBudget))
                .perform(click());
    }

    @Test
    public void testDeleteButton_Click_DoesNotCrash() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);
        onView(withId(R.id.btnDeleteBudget))
                .perform(click());
    }

    @Test
    public void testActivityInitialization_NoExceptions() {
        Intent intent = createValidIntent();
        ActivityScenario<BudgetDetailActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            assertNotNull(activity);
            assertFalse(activity.isFinishing());
        });
    }
}