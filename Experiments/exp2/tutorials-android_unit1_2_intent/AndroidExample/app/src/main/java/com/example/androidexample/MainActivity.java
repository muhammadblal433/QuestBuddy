package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private TextView messageText;     // define message textview variable
    private Button counterButton;     // define counter button variable
    private TextView tvLastResult;
    private ActivityResultLauncher<Intent> counterLauncher; // I inserted an API to receive the data back

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);             // link to Main activity XML

        /* initialize UI elements */
        messageText = findViewById(R.id.main_msg_txt);      // link to message textview in the Main activity XML
        counterButton = findViewById(R.id.main_counter_btn);// link to counter button in the Main activity XML
        tvLastResult = findViewById(R.id.tvLastResult);

        counterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK && result.getData() != null){
                        int finalCount = result.getData()
                                .getIntExtra(CounterActivity.EXTRA_FINAL_COUNT, 0);
                        tvLastResult.setText("Last Result: " + finalCount);
                    }
                }
        );

        /* click listener on counter button pressed */
        counterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* When we click on the counter button press, we create intent to open CounterActivity, start with default value of 0, and use the API to get the result back*/
                Intent intent = new Intent(MainActivity.this, CounterActivity.class);
                intent.putExtra(CounterActivity.EXTRA_START_COUNT, 0);
                counterLauncher.launch(intent);
            }
        });
    }
}