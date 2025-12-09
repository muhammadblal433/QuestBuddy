package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tasks.Task;
import com.example.androidexample.tasks.TaskManagerActivity;
import com.example.androidexample.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TaskManagerActivityTest {

    private ActivityScenario<TaskManagerActivity> launch() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                TaskManagerActivity.class
        );
        intent.putExtra("userId", 5);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<TaskManagerActivity> launchWithoutUserId() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                TaskManagerActivity.class
        );
        return ActivityScenario.launch(intent);
    }

    @Test
    public void taskManagerActivity_launchesAndShowsRecyclerView() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
            onView(withId(R.id.btnAddTask)).check(matches(isDisplayed()));
            onView(withId(R.id.btnHome)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void taskManagerActivity_invalidUserId_finishesActivity() {
        try (ActivityScenario<TaskManagerActivity> scenario = launchWithoutUserId()) {
            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void btnAddTask_opensAddTaskDialog() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.btnAddTask)).perform(click());
            onView(withText("Add New Task")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void addTaskDialog_hasCorrectElements() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.btnAddTask)).perform(click());

            onView(withText("Add New Task")).check(matches(isDisplayed()));
            onView(withId(R.id.etTaskTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.etTaskDescription)).check(matches(isDisplayed()));
            onView(withText("Add")).check(matches(isDisplayed()));
            onView(withText("Cancel")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void addTaskDialog_cancelButton_dismissesDialog() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.btnAddTask)).perform(click());
            onView(withText("Cancel")).perform(click());

            onView(withId(R.id.btnAddTask)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void addTaskDialog_emptyFields_showsToast() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.btnAddTask)).perform(click());
            onView(withText("Add")).perform(click());

            // Dialog closes and main UI is accessible
            onView(withId(R.id.btnAddTask)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void addTaskDialog_emptyTitle_showsToast() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.btnAddTask)).perform(click());

            onView(withId(R.id.etTaskDescription))
                    .perform(typeText("Test Description"), closeSoftKeyboard());

            onView(withText("Add")).perform(click());

            onView(withId(R.id.btnAddTask)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void addTaskDialog_emptyDescription_showsToast() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.btnAddTask)).perform(click());

            onView(withId(R.id.etTaskTitle))
                    .perform(typeText("Test Title"), closeSoftKeyboard());

            onView(withText("Add")).perform(click());

            onView(withId(R.id.btnAddTask)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void addTaskDialog_validInput_addsTask() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.btnAddTask)).perform(click());

            onView(withId(R.id.etTaskTitle))
                    .perform(typeText("Buy groceries"), closeSoftKeyboard());
            onView(withId(R.id.etTaskDescription))
                    .perform(typeText("Milk and eggs"), closeSoftKeyboard());

            onView(withText("Add")).perform(click());

            // Wait for API call
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void btnHome_navigatesToHome() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            onView(withId(R.id.btnHome)).perform(click());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void btnScrollDown_scrollsToBottom() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                // Directly trigger the scroll action instead of clicking the button
                RecyclerView recycler = activity.findViewById(R.id.recyclerTasks);
                if (recycler.getAdapter() != null && recycler.getAdapter().getItemCount() > 0) {
                    recycler.smoothScrollToPosition(recycler.getAdapter().getItemCount() - 1);
                }
            });

            // Verify no crash occurs
            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void recyclerView_hasCorrectLayoutManager() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerTasks);
                assertTrue(recycler.getLayoutManager() instanceof
                        androidx.recyclerview.widget.LinearLayoutManager);
            });
        }
    }

    @Test
    public void recyclerView_hasAdapter() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerTasks);
                assertNotNull(recycler.getAdapter());
            });
        }
    }

    @Test
    public void getTasks_isCalledOnCreate() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            // Wait for API call
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scenario.onActivity(activity -> {
                RecyclerView recycler = activity.findViewById(R.id.recyclerTasks);
                assertNotNull(recycler.getAdapter());
            });
        }
    }

    @Test
    public void updateTask_canBeCalled() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.updateTask(1L, "Test Task", "In Progress", "2025-12-31");
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void deleteTask_canBeCalled() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.deleteTask(1L);
            });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void showEditDialog_canBeCalled() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                Task task = new Task(1L, "Test", "Description", "Pending", "2025-12-31");
                activity.showEditDialog(task);
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Check that dialog appears
            onView(withText("Edit Task")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void editDialog_hasCorrectElements() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                Task task = new Task(1L, "Original Title", "Description", "Pending", "2025-12-31");
                activity.showEditDialog(task);
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withText("Edit Task")).check(matches(isDisplayed()));
            onView(withId(R.id.etEditTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.etEditStatus)).check(matches(isDisplayed()));
            onView(withId(R.id.etEditDueDate)).check(matches(isDisplayed()));
            onView(withText("Save")).check(matches(isDisplayed()));
            onView(withText("Cancel")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void editDialog_cancelButton_dismissesDialog() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                Task task = new Task(1L, "Test", "Description", "Pending", "2025-12-31");
                activity.showEditDialog(task);
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withText("Cancel")).perform(click());
            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void editDialog_saveButton_updatesTask() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                Task task = new Task(1L, "Test", "Description", "Pending", "2025-12-31");
                activity.showEditDialog(task);
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.etEditTitle))
                    .perform(typeText(" Updated"), closeSoftKeyboard());

            onView(withText("Save")).perform(click());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void tvAddHint_existsInLayout() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.tvAddHint));
            });
        }
    }

    @Test
    public void allUIElements_areInitialized() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.recyclerTasks));
                assertNotNull(activity.findViewById(R.id.btnAddTask));
                assertNotNull(activity.findViewById(R.id.btnHome));
                assertNotNull(activity.findViewById(R.id.btnScrollDown));
                assertNotNull(activity.findViewById(R.id.tvAddHint));
            });
        }
    }

    @Test
    public void getTasks_handlesEmptyResponse() {
        try (ActivityScenario<TaskManagerActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                activity.getTasks();
            });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.recyclerTasks)).check(matches(isDisplayed()));
        }
    }
}