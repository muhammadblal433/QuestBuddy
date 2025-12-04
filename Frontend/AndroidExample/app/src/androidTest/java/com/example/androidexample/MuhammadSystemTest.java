package com.example.androidexample;

import static org.junit.Assert.*;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.budget.CreateBudgetActivity;
import com.example.androidexample.notifications.NotificationsActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MuhammadSystemTest {

    /**
     * Launches NotificationsActivity with a fake userId and checks that
     * the RecyclerView is present and visible.
     */
    @Test
    public void openNotificationsScreen_showsRecyclerView() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsActivity.class
        );
        // any positive id is fine, the activity just needs a non -1 value
        intent.putExtra("userId", 51);

        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(intent)) {

            scenario.onActivity(activity -> {
                View recycler = activity.findViewById(R.id.recyclerNotifications);
                assertNotNull("RecyclerView should be present", recycler);
                assertEquals(View.VISIBLE, recycler.getVisibility());
                // also verify adapter attached
                assertNotNull("RecyclerView should have an adapter",
                        ((androidx.recyclerview.widget.RecyclerView) recycler).getAdapter());
            });
        }
    }

    /**
     * Invalid email should set an error on the email field and never hit the network.
     */
    @Test
    public void login_invalidEmail_showsError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {

            scenario.onActivity(activity -> {
                EditText email = activity.findViewById(R.id.etEmailLogin);
                EditText password = activity.findViewById(R.id.etPasswordLogin);
                Button loginButton = activity.findViewById(R.id.btnLogin);

                email.setText("not-an-email");
                password.setText("password123");
                loginButton.performClick();

                CharSequence error = email.getError();
                assertNotNull("Email field should show an error for invalid email", error);
                assertEquals("Enter a valid email", error.toString());
            });
        }
    }

    /**
     * Empty password should set "Password required" on the password field.
     */
    @Test
    public void login_emptyPassword_showsError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {

            scenario.onActivity(activity -> {
                EditText email = activity.findViewById(R.id.etEmailLogin);
                EditText password = activity.findViewById(R.id.etPasswordLogin);
                Button loginButton = activity.findViewById(R.id.btnLogin);

                email.setText("student@example.com");
                password.setText("");
                loginButton.performClick();

                CharSequence error = password.getError();
                assertNotNull("Password field should show an error when empty", error);
                assertEquals("Password required", error.toString());
            });
        }
    }

    /**
     * Creating a budget with an empty name should set "Budget name required".
     */
    @Test
    public void createBudget_withoutName_showsError() {
        try (ActivityScenario<CreateBudgetActivity> scenario =
                     ActivityScenario.launch(CreateBudgetActivity.class)) {

            scenario.onActivity(activity -> {
                EditText nameField = activity.findViewById(R.id.etBudgetName);
                Button createButton = activity.findViewById(R.id.btnCreateBudget);

                nameField.setText("");
                createButton.performClick();

                CharSequence error = nameField.getError();
                assertNotNull("Budget name field should show an error when empty", error);
                assertEquals("Budget name required", error.toString());
            });
        }
    }
}