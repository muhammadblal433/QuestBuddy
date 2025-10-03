package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Declare UI Components
    private TextView messageText, tvCounter;
    private Button btnToggle, btnReset;
    // Variables to track toggle state and counter
    private boolean isHelloWorld = true;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link UI elements to Java variables
        messageText = findViewById(R.id.main_msg_txt);
        tvCounter = findViewById(R.id.tvCounter);
        btnToggle = findViewById(R.id.btnToggle);
        btnReset = findViewById(R.id.btnReset);

        // initial screen setup
        messageText.setText("Hello World");
        messageText.setBackground(null); // no yellow background initially

        // Toggle button click logic code
        btnToggle.setOnClickListener(v -> {
            // Update Counter
            counter++;
            if (isHelloWorld) {
                // Yellow Background
                messageText.setText("Hello World - Yellow BG");
                messageText.setBackgroundResource(R.drawable.bg_hello);
            } else {
                // Green Background
                messageText.setText("Hello World - Green BG");
                messageText.setBackgroundResource(R.drawable.bg_alt);
            }

            // This allows the app to change between green and yellow states
            isHelloWorld = !isHelloWorld;

            // Update Counter Text
            tvCounter.setText("Count: " + counter);
        });

        // Reset button click logic code
        btnReset.setOnClickListener(v -> {
            counter = 0; // Reset the counter
            isHelloWorld = true; // Reset the toggle state as well
            messageText.setText("Hello World");// Default to original text
            messageText.setBackground(null); // Remove any background that's already there
            tvCounter.setText("Count: 0"); // Reset the counter display to 0
        });
    }
}
