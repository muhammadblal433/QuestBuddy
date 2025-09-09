package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CounterActivity extends AppCompatActivity {

    private TextView numberTxt; // define number textview variable
    private Button increaseBtn; // define increase button variable
    private Button decreaseBtn; // define decrease button variable
    private Button backBtn;     // define back button variable

    private int counter = 0;    // counter variable

    public static final String EXTRA_START_COUNT = "start_count";
    public static final String EXTRA_FINAL_COUNT = "final_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        /* initialize UI elements */
        numberTxt = findViewById(R.id.number);
        increaseBtn = findViewById(R.id.counter_increase_btn);
        decreaseBtn = findViewById(R.id.counter_decrease_btn);
        backBtn = findViewById(R.id.counter_back_btn);

        /* get starting count from main activity, default is 0*/
        counter = getIntent().getIntExtra(EXTRA_START_COUNT, 0);
        numberTxt.setText(String.valueOf(counter));


        /* when increase btn is pressed, counter++, reset number textview */
        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberTxt.setText(String.valueOf(++counter));
            }
        });

        /* when decrease btn is pressed, counter--, reset number textview, this cannot go below 0 */
        decreaseBtn.setOnClickListener(view -> {
            if(counter > 0) counter--;
            numberTxt.setText(String.valueOf(counter));
        });

        /* when back btn is pressed, final counter value is returned and CounterActivity is closed */
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent data = new Intent();
               data.putExtra(EXTRA_FINAL_COUNT, counter);
               setResult(RESULT_OK, data);
               finish(); // we can now close CounterActivity
            }
        });

    }
}