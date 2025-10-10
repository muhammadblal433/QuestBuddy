package com.example.androidexample;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DayViewActivity extends AppCompatActivity {

    private String selectedDate;
    private LinearLayout timeColumn;
    private FrameLayout taskColumn;
    private int userId;

    // Keep track of event IDs mapped to task blocks
    private final Map<Integer, JSONObject> taskMap = new HashMap<>();

    private static final String TASKS_URL = "http://coms-3090-026.class.las.iastate.edu:8080/api/v4/calendar/events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);

        selectedDate = getIntent().getStringExtra("selectedDate");
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        TextView dateHeader = findViewById(R.id.dayDateText);
        dateHeader.setText(selectedDate);

        timeColumn = findViewById(R.id.timeColumn);
        taskColumn = findViewById(R.id.taskColumn);

        populateTimeColumn();
        fetchTasksForSelectedDay();

        Button addBtn = findViewById(R.id.addTaskBtn);
        Button returnBtn = findViewById(R.id.returnCalendar);

        returnBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DayViewActivity.this, CalendarActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });

        addBtn.setOnClickListener(v -> showAddTaskDialog());
    }

    private void populateTimeColumn() {
        for (int i = 0; i < 24; i++) {
            TextView timeText = new TextView(this);
            timeText.setText(String.format("%02d:00", i));
            timeText.setPadding(8, 30, 8, 30);
            timeText.setTextSize(14);
            timeColumn.addView(timeText);
        }
    }

    // --- GET all events for the user ---
    private void fetchTasksForSelectedDay() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, TASKS_URL, null,
                response -> {
                    LocalDate thisDay = parseDate(selectedDate);


                    ArrayList<JSONObject> tasksForDay = new ArrayList<>(taskMap.values());

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject task = response.getJSONObject(i);
                            String startAt = task.optString("startAt", "");
                            if (startAt.length() >= 10) {
                                LocalDate taskDate = LocalDate.parse(startAt.substring(0, 10));
                                if (taskDate.equals(thisDay)) {
                                    int id = task.getInt("id");
                                    taskMap.put(id, task);
                                    if (!tasksForDay.contains(task))
                                        tasksForDay.add(task);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    displayTasks(new ArrayList<>(taskMap.values()));
                },
                error -> Toast.makeText(this, "Failed to fetch tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };

        queue.add(request);
    }

    private void displayTasks(ArrayList<JSONObject> tasks) {
        taskColumn.removeAllViews();

        for (JSONObject task : tasks) {
            try {
                int id = task.getInt("id");
                String title = task.getString("title");
                String startAt = task.getString("startAt");
                String endAt = task.getString("endAt");

                OffsetDateTime startUTC = OffsetDateTime.parse(startAt);
                OffsetDateTime endUTC = OffsetDateTime.parse(endAt);

                addTaskBlock(id, title, startUTC.toLocalTime(), endUTC.toLocalTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAddTaskDialog() {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(32, 32, 32, 32);

        EditText titleInput = new EditText(this);
        titleInput.setHint("Task title");

        EditText startTimeInput = new EditText(this);
        startTimeInput.setHint("Start time (HH:mm)");
        startTimeInput.setFocusable(false);

        EditText endTimeInput = new EditText(this);
        endTimeInput.setHint("End time (HH:mm)");
        endTimeInput.setFocusable(false);

        dialogLayout.addView(titleInput);
        dialogLayout.addView(startTimeInput);
        dialogLayout.addView(endTimeInput);

        final LocalTime[] start = new LocalTime[1];
        final LocalTime[] end = new LocalTime[1];

        startTimeInput.setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(this, (view, hour, minute) -> {
                start[0] = LocalTime.of(hour, minute);
                startTimeInput.setText(start[0].format(DateTimeFormatter.ofPattern("HH:mm")));
            }, 12, 0, true);
            picker.show();
        });

        endTimeInput.setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(this, (view, hour, minute) -> {
                end[0] = LocalTime.of(hour, minute);
                endTimeInput.setText(end[0].format(DateTimeFormatter.ofPattern("HH:mm")));
            }, 13, 0, true);
            picker.show();
        });

        new android.app.AlertDialog.Builder(this)
                .setTitle("Add Task")
                .setView(dialogLayout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty() || start[0] == null || end[0] == null) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveTaskToServer(title, start[0], end[0]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addTaskBlock(int id, String title, LocalTime start, LocalTime end) {
        TextView block = new TextView(this);
        block.setText(title);
        block.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        block.setPadding(16, 16, 16, 16);
        block.setTextColor(getResources().getColor(android.R.color.white));
        block.setTextSize(14);
        block.setElevation(6);

        int startMinutes = start.getHour() * 60 + start.getMinute();
        int endMinutes = end.getHour() * 60 + end.getMinute();
        int durationMinutes = Math.max(15, endMinutes - startMinutes);

        int totalHeight = timeColumn.getHeight();
        if (totalHeight == 0) {
            timeColumn.post(() -> addTaskBlock(id, title, start, end));
            return;
        }

        float pxPerMinute = (float) (totalHeight + 75) / (24f * 60f);
        int topMargin = (int) (startMinutes * pxPerMinute);
        int height = (int) (durationMinutes * pxPerMinute);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
        );
        params.topMargin = topMargin;
        block.setLayoutParams(params);

        block.setOnClickListener(v -> showEditDeleteDialog(id));

        taskColumn.addView(block);
    }

    private void showEditDeleteDialog(int id) {
        JSONObject task = taskMap.get(id);
        if (task == null) return;

        String title = task.optString("title", "Untitled");
        String[] options = {"Edit", "Delete"};
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showEditTaskDialog(id, task);
                    else deleteTaskFromServer(id);
                })
                .show();
    }

    private void showEditTaskDialog(int id, JSONObject task) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        EditText titleInput = new EditText(this);
        titleInput.setText(task.optString("title", ""));
        layout.addView(titleInput);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = titleInput.getText().toString().trim();
                    if (!newTitle.isEmpty()) updateTaskOnServer(id, newTitle, task);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTaskOnServer(int id, String newTitle, JSONObject task) {
        try {
            JSONObject body = new JSONObject(task.toString());
            body.put("title", newTitle);

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, TASKS_URL + "/" + id, body,
                    response -> {
                        Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
                        fetchTasksForSelectedDay();
                    },
                    error -> Toast.makeText(this, "Error updating task", Toast.LENGTH_LONG).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("X-User-Id", String.valueOf(userId));
                    return headers;
                }
            };
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void deleteTaskFromServer(int id) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = TASKS_URL + "/" + id;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    // Works whether backend returns [] or 204 No Content
                    Toast.makeText(this, "Task deleted!", Toast.LENGTH_SHORT).show();
                    taskMap.remove(id);
                    taskColumn.removeAllViews();
                    fetchTasksForSelectedDay();
                },
                error -> {
                    String message = "Error deleting task";
                    if (error.networkResponse != null) {
                        message += " (Code: " + error.networkResponse.statusCode + ")";
                        try {
                            String body = new String(error.networkResponse.data, "UTF-8");
                            message += "\nResponse: " + body;
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-User-Id", String.valueOf(userId));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private void saveTaskToServer(String title, LocalTime start, LocalTime end) {
        LocalDate date = parseDate(selectedDate);
        if (date == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String startAt = date.atTime(start).format(formatter);
        String endAt = date.atTime(end).format(formatter);

        JSONObject taskJson = new JSONObject();
        try {
            taskJson.put("title", title);
            taskJson.put("description", "Daily sync");
            taskJson.put("startAt", startAt);
            taskJson.put("endAt", endAt);
            taskJson.put("location", "home");
            taskJson.put("allDay", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, TASKS_URL, taskJson,
                response -> {
                    try {
                        int id = response.getInt("id");
                        taskMap.put(id, response); // Store new event immediately
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
                    fetchTasksForSelectedDay();
                },
                error -> Toast.makeText(this, "Error saving task", Toast.LENGTH_LONG).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-User-Id", String.valueOf(userId));
                return headers;
            }
        };

        queue.add(request);
    }

    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}