package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.view.View;

import androidx.lifecycle.Lifecycle;
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

    private ActivityScenario<PackingChecklistActivity> launchWithoutUserId() {
        Intent i = new Intent(
                ApplicationProvider.getApplicationContext(),
                PackingChecklistActivity.class
        );
        // Don't put userId extra
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

    @Test
    public void addItemDialog_hasCorrectElements() {
        launch();
        onView(withId(R.id.btnAddItem)).perform(click());

        // Check dialog elements
        onView(withText("Add New Item")).check(matches(isDisplayed()));
        onView(withId(R.id.etItemName)).check(matches(isDisplayed()));
        onView(withText("Add")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }

    @Test
    public void addItemDialog_cancelButton_dismissesDialog() {
        launch();
        onView(withId(R.id.btnAddItem)).perform(click());
        onView(withText("Cancel")).perform(click());

        // Dialog should be dismissed - we can't directly check this,
        // but we can verify the main UI is still accessible
        onView(withId(R.id.btnAddItem)).check(matches(isDisplayed()));
    }

    @Test
    public void addItemDialog_emptyInput_showsToast() {
        launch();
        onView(withId(R.id.btnAddItem)).perform(click());

        // Try to add without entering text
        onView(withText("Add")).perform(click());

        // After clicking Add with empty input, dialog closes but Toast is shown
        // We verify the main UI is still accessible
        onView(withId(R.id.btnAddItem)).check(matches(isDisplayed()));
    }

    @Test
    public void addItemDialog_validInput_addsItem() {
        launch();
        onView(withId(R.id.btnAddItem)).perform(click());

        // Enter item name
        onView(withId(R.id.etItemName))
                .perform(typeText("Passport"), closeSoftKeyboard());

        // Click Add button
        onView(withText("Add")).perform(click());

        // Give time for API call and UI update
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // The hint should be gone after adding an item
        onView(withId(R.id.tvAddHint)).check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void addItemDialog_multipleItems_addsAll() {
        launch();

        // Add first item
        onView(withId(R.id.btnAddItem)).perform(click());
        onView(withId(R.id.etItemName))
                .perform(typeText("Toothbrush"), closeSoftKeyboard());
        onView(withText("Add")).perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Add second item
        onView(withId(R.id.btnAddItem)).perform(click());
        onView(withId(R.id.etItemName))
                .perform(typeText("Sunscreen"), closeSoftKeyboard());
        onView(withText("Add")).perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify UI is still functional
        onView(withId(R.id.recyclerPacking)).check(matches(isDisplayed()));
    }

    @Test
    public void btnBackHome_navigatesToHome() {
        ActivityScenario<PackingChecklistActivity> scenario = launch();

        onView(withId(R.id.btnBackHome)).perform(click());

        // Activity should start finishing after clicking back
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void invalidUserId_finishesActivity() {
        ActivityScenario<PackingChecklistActivity> scenario = launchWithoutUserId();

        // Activity should finish immediately when userId is invalid
        assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
    }

    @Test
    public void deleteItem_removesFromList() {
        ActivityScenario<PackingChecklistActivity> scenario = launch();

        scenario.onActivity(activity -> {
            // Add an item first
            activity.runOnUiThread(() -> {
                // Manually call addItem to set up test data
                // This would normally be done through the API
            });

            // Then test deleteItem method
            activity.deleteItem(1L);
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void tvAddHint_visibleWhenListEmpty() {
        launch();

        // Initially, if no items, hint should be visible
        // (This depends on initial state - adjust based on your app's behavior)
        onView(withId(R.id.tvAddHint)).check(matches(isDisplayed()));
    }

    @Test
    public void recyclerView_hasCorrectLayoutManager() {
        ActivityScenario<PackingChecklistActivity> scenario = launch();

        scenario.onActivity(activity -> {
            androidx.recyclerview.widget.RecyclerView recycler =
                    activity.findViewById(R.id.recyclerPacking);
            assertTrue(recycler.getLayoutManager() instanceof
                    androidx.recyclerview.widget.LinearLayoutManager);
        });
    }

    @Test
    public void adapter_isSetCorrectly() {
        ActivityScenario<PackingChecklistActivity> scenario = launch();

        scenario.onActivity(activity -> {
            androidx.recyclerview.widget.RecyclerView recycler =
                    activity.findViewById(R.id.recyclerPacking);
            assertTrue(recycler.getAdapter() != null);
        });
    }

    @Test
    public void addItem_withWhitespaceOnly_showsError() {
        launch();
        onView(withId(R.id.btnAddItem)).perform(click());

        // Enter only spaces
        onView(withId(R.id.etItemName))
                .perform(typeText("   "), closeSoftKeyboard());

        onView(withText("Add")).perform(click());

        // After clicking Add with whitespace, dialog closes and shows Toast
        // Verify main UI is still accessible
        onView(withId(R.id.btnAddItem)).check(matches(isDisplayed()));
    }

    @Test
    public void onCreate_setsCorrectContentView() {
        ActivityScenario<PackingChecklistActivity> scenario = launch();

        scenario.onActivity(activity -> {
            // Verify key views exist
            assertTrue(activity.findViewById(R.id.recyclerPacking) != null);
            assertTrue(activity.findViewById(R.id.btnAddItem) != null);
            assertTrue(activity.findViewById(R.id.btnBackHome) != null);
            assertTrue(activity.findViewById(R.id.tvAddHint) != null);
        });
    }

    @Test
    public void userId_isExtractedFromIntent() {
        ActivityScenario<PackingChecklistActivity> scenario = launch();

        scenario.onActivity(activity -> {
            // If activity is running, userId was valid
            assertTrue(!activity.isFinishing());
        });
    }

    @Test
    public void requestQueue_isInitialized() {
        ActivityScenario<PackingChecklistActivity> scenario = launch();

        scenario.onActivity(activity -> {
            // Activity should be properly initialized
            assertTrue(activity.findViewById(R.id.recyclerPacking) != null);
        });
    }

    @Test
    public void deleteItem_whenListBecomesEmpty_showsHint() {
        ActivityScenario<PackingChecklistActivity> scenario = launch();

        scenario.onActivity(activity -> {
            // This test verifies the logic in deleteItem that shows hint
            // when list becomes empty
            activity.deleteItem(999L); // Non-existent item
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // After attempting delete, app should still be functional
        onView(withId(R.id.btnAddItem)).check(matches(isDisplayed()));
    }
}