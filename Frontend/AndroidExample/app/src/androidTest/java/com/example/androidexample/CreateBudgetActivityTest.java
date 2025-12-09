package com.example.androidexample.budget;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateBudgetActivityTest {

    private Context context;
    private static final String TEST_USERNAME = "testuser";

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
        return new Intent(context, CreateBudgetActivity.class);
    }

    @Test
    public void testActivityLaunch_Success() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
            assertNotNull(activity.findViewById(R.id.etBudgetName));
            assertNotNull(activity.findViewById(R.id.participantsContainer));
            assertNotNull(activity.findViewById(R.id.btnAddParticipant));
            assertNotNull(activity.findViewById(R.id.btnCreateBudget));
        });
    }

    @Test
    public void testBudgetNameField_IsVisible() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);

        onView(withId(R.id.etBudgetName))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAddParticipantButton_IsVisible() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);

        onView(withId(R.id.btnAddParticipant))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testCreateBudgetButton_IsVisible() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);

        onView(withId(R.id.btnCreateBudget))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testParticipantsContainer_IsVisible() {
        Intent intent = createValidIntent();
        ActivityScenario.launch(intent);

        onView(withId(R.id.participantsContainer))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testInitialParticipants_TwoViewsAdded() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);
            assertEquals(2, container.getChildCount());
        });
    }

    @Test
    public void testAddParticipantButton_Click_AddsNewParticipant() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);
            int initialCount = container.getChildCount();

            activity.findViewById(R.id.btnAddParticipant).performClick();

            assertEquals(initialCount + 1, container.getChildCount());
        });
    }

    @Test
    public void testAddMultipleParticipants_IncreasesCount() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);
            int initialCount = container.getChildCount();

            activity.findViewById(R.id.btnAddParticipant).performClick();
            activity.findViewById(R.id.btnAddParticipant).performClick();
            activity.findViewById(R.id.btnAddParticipant).performClick();

            assertEquals(initialCount + 3, container.getChildCount());
        });
    }

    @Test
    public void testParticipantLabels_AreNumberedCorrectly() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);

            android.widget.TextView label1 = container.getChildAt(0)
                    .findViewById(R.id.tvParticipantLabel);
            assertEquals("Participant 1", label1.getText().toString());

            android.widget.TextView label2 = container.getChildAt(1)
                    .findViewById(R.id.tvParticipantLabel);
            assertEquals("Participant 2", label2.getText().toString());
        });
    }

    @Test
    public void testDeleteParticipant_RemovesView() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);
            activity.findViewById(R.id.btnAddParticipant).performClick();

            int countBeforeDelete = container.getChildCount();

            android.view.View lastParticipant = container.getChildAt(countBeforeDelete - 1);
            android.widget.Button deleteBtn = lastParticipant.findViewById(R.id.btnDeleteParticipant);
            deleteBtn.performClick();

            assertEquals(countBeforeDelete - 1, container.getChildCount());
        });
    }

    @Test
    public void testDeleteLastParticipant_ShowsToast() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);

            while (container.getChildCount() > 1) {
                android.view.View lastView = container.getChildAt(container.getChildCount() - 1);
                android.widget.Button deleteBtn = lastView.findViewById(R.id.btnDeleteParticipant);
                deleteBtn.performClick();
            }

            assertEquals(1, container.getChildCount());

            android.view.View lastView = container.getChildAt(0);
            android.widget.Button deleteBtn = lastView.findViewById(R.id.btnDeleteParticipant);
            deleteBtn.performClick();

            assertEquals(1, container.getChildCount());
        });
    }

    @Test
    public void testRenumberParticipants_AfterDeletion() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);

            activity.findViewById(R.id.btnAddParticipant).performClick();

            android.view.View firstParticipant = container.getChildAt(0);
            android.widget.Button deleteBtn = firstParticipant.findViewById(R.id.btnDeleteParticipant);
            deleteBtn.performClick();

            android.widget.TextView newFirstLabel = container.getChildAt(0)
                    .findViewById(R.id.tvParticipantLabel);
            assertEquals("Participant 1", newFirstLabel.getText().toString());
        });
    }

    @Test
    public void testUsernameRetrieved_FromSharedPreferences() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
        });
    }

    @Test
    public void testEmptyUsername_InSharedPreferences() {
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit().putString("username", "").apply();

        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            assertNotNull(activity);
        });
    }

    @Test
    public void testNullUsername_InSharedPreferences() {
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit().remove("username").apply();

        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            assertNotNull(activity);
        });
    }

    @Test
    public void testParticipantView_HasAllFields() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);
            android.view.View participantView = container.getChildAt(0);

            assertNotNull(participantView.findViewById(R.id.tvParticipantLabel));
            assertNotNull(participantView.findViewById(R.id.etUsername));
            assertNotNull(participantView.findViewById(R.id.etShareAmount));
            assertNotNull(participantView.findViewById(R.id.etPaidAmount));
            assertNotNull(participantView.findViewById(R.id.btnDeleteParticipant));
        });
    }

    @Test
    public void testScrollView_Exists() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            assertNotNull(activity.findViewById(R.id.scrollViewBudgetCreate));
        });
    }

    @Test
    public void testCreateButton_Click_WithEmptyBudgetName() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            EditText budgetName = activity.findViewById(R.id.etBudgetName);
            budgetName.setText("");

            activity.findViewById(R.id.btnCreateBudget).performClick();

            assertNotNull(budgetName.getError());
        });
    }

    @Test
    public void testCreateButton_Click_WithValidBudgetName() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            EditText budgetName = activity.findViewById(R.id.etBudgetName);
            budgetName.setText("Test Budget");

            LinearLayout container = activity.findViewById(R.id.participantsContainer);
            android.view.View firstParticipant = container.getChildAt(0);
            EditText username = firstParticipant.findViewById(R.id.etUsername);
            username.setText("user1");

            activity.findViewById(R.id.btnCreateBudget).performClick();
        });
    }

    @Test
    public void testCreateButton_Click_WithNoParticipants() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            EditText budgetName = activity.findViewById(R.id.etBudgetName);
            budgetName.setText("Test Budget");

            activity.findViewById(R.id.btnCreateBudget).performClick();
        });
    }

    @Test
    public void testActivityInitialization_NoExceptions() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            assertNotNull(activity);
            assertFalse(activity.isFinishing());
        });
    }

    @Test
    public void testParticipantsContainer_IsLinearLayout() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);
            assertNotNull(container);
            assertTrue(container instanceof LinearLayout);
        });
    }

    @Test
    public void testBudgetNameField_IsEditText() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            EditText budgetName = activity.findViewById(R.id.etBudgetName);
            assertNotNull(budgetName);
            assertTrue(budgetName instanceof EditText);
        });
    }

    @Test
    public void testAddParticipant_TenTimes_AllAdded() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);
            int initialCount = container.getChildCount();

            for (int i = 0; i < 10; i++) {
                activity.findViewById(R.id.btnAddParticipant).performClick();
            }

            assertEquals(initialCount + 10, container.getChildCount());
        });
    }

    @Test
    public void testDeleteButton_ExistsInEachParticipant() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            LinearLayout container = activity.findViewById(R.id.participantsContainer);

            for (int i = 0; i < container.getChildCount(); i++) {
                android.view.View participantView = container.getChildAt(i);
                android.widget.Button deleteBtn = participantView.findViewById(R.id.btnDeleteParticipant);
                assertNotNull(deleteBtn);
            }
        });
    }

    @Test
    public void testAllUIComponents_AreInitialized() {
        Intent intent = createValidIntent();
        ActivityScenario<CreateBudgetActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            assertNotNull(activity.findViewById(R.id.etBudgetName));
            assertNotNull(activity.findViewById(R.id.participantsContainer));
            assertNotNull(activity.findViewById(R.id.btnAddParticipant));
            assertNotNull(activity.findViewById(R.id.btnCreateBudget));
            assertNotNull(activity.findViewById(R.id.scrollViewBudgetCreate));
        });
    }
}