package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private Button backBtn, forwardBtn;
    private int userId;


    private static final String TASKS_URL = "http://coms-3090-026.class.las.iastate.edu:8080/api/v4/calendar/events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_interface);
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        TextView navHome = findViewById(R.id.nav_home);


        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });


        initWidgets();
        // initializes to the current month
        selectedDate = LocalDate.now();
        fetchTasksFromServer();
    }

    // initializes the views for calendarRecycler, the monthYear Text, the back and forward buttons
    // Also changes the month based on if the forward or backward button is pressed
    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        backBtn = findViewById(R.id.back_btn);
        forwardBtn = findViewById(R.id.forward_btn);

        backBtn.setOnClickListener(v -> {
            selectedDate = selectedDate.minusMonths(1);
            fetchTasksFromServer();
        });

        forwardBtn.setOnClickListener(v -> {
            selectedDate = selectedDate.plusMonths(1);
            fetchTasksFromServer();
        });
    }

    private void fetchTasksFromServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, TASKS_URL, null,
                response -> {
                    ArrayList<String> taskDates = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject taskObject = response.getJSONObject(i);
                            String startAt = taskObject.optString("startAt", "");

                            if (startAt.length() >= 10) {
                                // Extract just the date part, e.g., 2025-10-06
                                String datePart = startAt.substring(0, 10);
                                taskDates.add(datePart);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    setMonthView(taskDates);
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

        queue.add(jsonArrayRequest);
    }

    // Sets the header text and sets up the grid of cells using CalendarAdapter and also sets up the RecyclerView grid
    private void setMonthView(ArrayList<String> taskDates) {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        String monthYear = monthYearFromDate(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, selectedDate, taskDates);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    // This basically decides how many blanks there are each month and also where they occur in each month
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    // formats how the heading should look like
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }


    // when a cell is clicked, it displays a message saying which date is selected
    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            String selectedFullDate = dayText + " " + monthYearFromDate(selectedDate);
            Intent intent = new Intent(this, DayViewActivity.class);
            intent.putExtra("selectedDate", selectedFullDate);
            intent.putExtra("userId", userId);
            startActivity(intent);
        }
    }
}
