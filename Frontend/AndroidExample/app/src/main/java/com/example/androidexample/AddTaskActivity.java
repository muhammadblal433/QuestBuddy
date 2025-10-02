package com.example.androidexample;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AddTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        TextView dateDisplay = findViewById(R.id.date);
        String selectedDate = getIntent().getStringExtra("selectedDate");

        dateDisplay.setText("Date: " + selectedDate);
    }
}
