package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerTasks;  //displays the list of tasks
    private TaskAdapter adapter; //connects data to the RecyclerView
    private List<Task> taskList = new ArrayList<>();  //stores alll the tasks
    private EditText etTitle, etDescription;  // inputs field for new task
    private RequestQueue queue; // handles the network requests(Volley)


    private final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080/api/v3/tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_manager);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        Button btnAdd = findViewById(R.id.btnAddTask);

        recyclerTasks = findViewById(R.id.recyclerTasks);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));

        queue = Volley.newRequestQueue(this);
        adapter = new TaskAdapter(this, taskList, this);
        recyclerTasks.setAdapter(adapter);

        FloatingActionButton btnScrollDown = findViewById(R.id.btnScrollDown);
        btnScrollDown.setOnClickListener(v -> {
            if (adapter.getItemCount() > 0) {
                recyclerTasks.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        });

        btnAdd.setOnClickListener(v -> addTask());
        getTasks();
    }

    // GET all tasks
    public void getTasks() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, BASE_URL, null,
                response -> {
                    taskList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            JSONObject userObj = obj.getJSONObject("user");

                            taskList.add(new Task(
                                    obj.getLong("taskId"),
                                    obj.getString("title"),
                                    obj.optString("description", "No Description"),
                                    obj.getString("status"),
                                    obj.optString("dueDate", "N/A")
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "GET Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
        queue.add(request);
    }

    // POST - Add new task
    private void addTask() {
        JSONObject taskJson = new JSONObject();
        try {
            taskJson.put("userId", 22);
            taskJson.put("title", etTitle.getText().toString());
            taskJson.put("description", etDescription.getText().toString());
            taskJson.put("status", "Pending");
            taskJson.put("dueDate", "2025-10-15");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL, taskJson,
                response -> {
                    Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
                    getTasks();
                },
                error -> Toast.makeText(this, "POST Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
        queue.add(request);
    }

    // PUT - Update task status
    public void updateTask(long taskId, String title, String status, String dueDate) {
        String url = BASE_URL + "/" + taskId;

        JSONObject updateJson = new JSONObject();
        try {
            updateJson.put("title", title);
            updateJson.put("status", status);
            updateJson.put("dueDate", dueDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, updateJson,
                response -> {
                    Toast.makeText(this, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                    getTasks(); // Refresh the list after updating
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error updating task: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(request);
    }


    // DELETE - Remove task
    public void deleteTask(long taskId) {
        String url = BASE_URL + "/" + taskId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    Toast.makeText(this, "Task deleted!", Toast.LENGTH_SHORT).show();
                    getTasks();
                },
                error -> Toast.makeText(this, "DELETE Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
        queue.add(request);
    }

    public void showEditDialog(Task task) {
        // Inflate the custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_task, null);

        EditText etTitle = dialogView.findViewById(R.id.etEditTitle);
        EditText etStatus = dialogView.findViewById(R.id.etEditStatus);
        EditText etDueDate = dialogView.findViewById(R.id.etEditDueDate);

        // Pre-fill with current values
        etTitle.setText(task.getTitle());
        etStatus.setText(task.getStatus());
        etDueDate.setText(task.getDueDate());

        // Build the dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newStatus = etStatus.getText().toString().trim();
                    String newDueDate = etDueDate.getText().toString().trim();

                    updateTask(task.getTaskId(), newTitle, newStatus, newDueDate);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}

