package com.example.androidexample.tripplanner;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidexample.R;
import com.example.androidexample.tripplanner.TripEvent;
import com.example.androidexample.tripplanner.TripEventApi;

public class AddEditTripEventActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "tripId";
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_EVENT = "tripEvent";

    private long tripId;
    private long userId;

    private TripEvent existingEvent;

    private TripEventApi api;

    private EditText edtName;
    private EditText edtStartsAt;
    private EditText edtEndsAt;
    private EditText edtLocation;
    private EditText edtNotes;
    private EditText edtPosition;
    private Button btnSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_trip_event);

        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1L);
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1L);
        existingEvent = (TripEvent) getIntent().getSerializableExtra(EXTRA_EVENT);

        api = new TripEventApi(this);

        edtName = findViewById(R.id.edtName);
        edtStartsAt = findViewById(R.id.edtStartsAt);
        edtEndsAt = findViewById(R.id.edtEndsAt);
        edtLocation = findViewById(R.id.edtLocation);
        edtNotes = findViewById(R.id.edtNotes);
        edtPosition = findViewById(R.id.edtPosition);
        btnSave = findViewById(R.id.btnSave);

        if (existingEvent != null) {
            setTitle("Edit Event");
            edtName.setText(existingEvent.name);
            edtStartsAt.setText(existingEvent.startsAt);
            edtEndsAt.setText(existingEvent.endsAt);
            edtLocation.setText(existingEvent.location);
            edtNotes.setText(existingEvent.notes);
            if (existingEvent.position != null) {
                edtPosition.setText(String.valueOf(existingEvent.position));
            }
        } else {
            setTitle("New Event");
        }

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String name = edtName.getText().toString().trim();
        String startsAt = edtStartsAt.getText().toString().trim();
        String endsAt = edtEndsAt.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();
        String positionStr = edtPosition.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(startsAt)) {
            edtStartsAt.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(endsAt)) {
            edtEndsAt.setError("Required");
            return;
        }

        Integer position = null;
        if (!TextUtils.isEmpty(positionStr)) {
            try {
                position = Integer.parseInt(positionStr);
            } catch (NumberFormatException e) {
                edtPosition.setError("Invalid number");
                return;
            }
        }

        if (existingEvent == null) {
            api.createEvent(
                    userId,
                    tripId,
                    name,
                    startsAt,
                    endsAt,
                    TextUtils.isEmpty(location) ? null : location,
                    TextUtils.isEmpty(notes) ? null : notes,
                    position,
                    new TripEventApi.EventCallback() {
                        @Override
                        public void onSuccess(TripEvent event) {
                            Toast.makeText(AddEditTripEventActivity.this,
                                    "Event created", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(AddEditTripEventActivity.this,
                                    "Create failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            api.editEvent(
                    userId,
                    tripId,
                    existingEvent.id,
                    name,
                    startsAt,
                    endsAt,
                    TextUtils.isEmpty(location) ? null : location,
                    TextUtils.isEmpty(notes) ? null : notes,
                    position,
                    new TripEventApi.EventCallback() {
                        @Override
                        public void onSuccess(TripEvent event) {
                            Toast.makeText(AddEditTripEventActivity.this,
                                    "Event updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(AddEditTripEventActivity.this,
                                    "Update failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }
}
