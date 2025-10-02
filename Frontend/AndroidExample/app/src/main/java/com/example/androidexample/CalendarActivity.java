package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class CalendarActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private Button backBtn, forwardBtn;
    private static final String TASKS_URL = "https://ec3fa688-a6b1-4c54-a258-487e5dd20160.mock.pstmn.io";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_interface);

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
                            String date = taskObject.getString("date");
                            taskDates.add(date);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Now that we have task dates, load the calendar view
                    setMonthView(taskDates);
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);
    }

    // Sets the header text and sets up the grid of cells using CalendarAdapter and also sets up the RecyclerView grid
    private void setMonthView(ArrayList<String> taskDates) {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        String monthYear = monthYearFromDate(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, monthYear, taskDates);
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


    // when a cell is clicked, it displays a message saying which date is selected (needs to be changed for our task)
    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            String selectedFullDate = dayText + " " + monthYearFromDate(selectedDate);
            Intent intent = new Intent(this, AddTaskActivity.class);
            intent.putExtra("selectedDate", selectedFullDate);
            startActivity(intent);
        }
    }
}
