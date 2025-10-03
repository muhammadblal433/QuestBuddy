package com.example.androidexample;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AddTaskActivity extends AppCompatActivity {

    private String selectedDate; // Object for the selected date
    private EditText taskInput; // Object for the EdiText that holds task's input
    private Button saveTaskBtn; // Object for the save button

    private static final String MOCK_URL = "https://ec3fa688-a6b1-4c54-a258-487e5dd20160.mock.pstmn.io";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        TextView dateDisplay = findViewById(R.id.date);
        selectedDate = getIntent().getStringExtra("selectedDate");
        taskInput = findViewById(R.id.taskInput);
        saveTaskBtn = findViewById(R.id.saveTaskButton);

        dateDisplay.setText("Date: " + selectedDate);
        saveTaskBtn.setOnClickListener(v -> saveTasks());


    }

    private void saveTasks(){
        String task = taskInput.getText().toString().trim();
        if (task.isEmpty()) {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject taskJson = new JSONObject();
        try {
            taskJson.put("date", selectedDate);
            taskJson.put("task", task);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, MOCK_URL, taskJson,
                response -> {
                    Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}
