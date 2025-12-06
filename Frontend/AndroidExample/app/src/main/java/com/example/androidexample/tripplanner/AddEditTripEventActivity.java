package com.example.androidexample.tripplanner;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;

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

    private String startsAtIso = null;
    private String endsAtIso = null;

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

        edtStartsAt.setFocusable(false);
        edtStartsAt.setClickable(true);

        edtEndsAt.setFocusable(false);
        edtEndsAt.setClickable(true);

        if (existingEvent != null) {
            setTitle("Edit Event");

            edtName.setText(existingEvent.name);
            edtLocation.setText(existingEvent.location);
            edtNotes.setText(existingEvent.notes);

            if (existingEvent.position != null) {
                edtPosition.setText(String.valueOf(existingEvent.position));
            }

            if (existingEvent.startsAt != null) {
                startsAtIso = existingEvent.startsAt;
                edtStartsAt.setText(formatDisplay(existingEvent.startsAt));
            }
            if (existingEvent.endsAt != null) {
                endsAtIso = existingEvent.endsAt;
                edtEndsAt.setText(formatDisplay(existingEvent.endsAt));
            }

        } else {
            setTitle("New Event");
        }

        edtStartsAt.setOnClickListener(v -> openDateTimePicker(true));
        edtEndsAt.setOnClickListener(v -> openDateTimePicker(false));

        btnSave.setOnClickListener(v -> save());
    }

    private void openDateTimePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);

                    TimePickerDialog timePicker = new TimePickerDialog(
                            this,
                            (tp, hour, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                // Set display text
                                String display = new SimpleDateFormat(
                                        "MMM dd, yyyy • h:mm a",
                                        Locale.getDefault()
                                ).format(calendar.getTime());

                                // Convert to ISO
                                Instant instant = calendar.toInstant();
                                String iso = instant.toString();

                                if (isStart) {
                                    edtStartsAt.setText(display);
                                    startsAtIso = iso;
                                } else {
                                    edtEndsAt.setText(display);
                                    endsAtIso = iso;
                                }

                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                    );

                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    private String formatDisplay(String iso) {
        try {
            Instant instant = Instant.parse(iso);
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            return zdt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy • h:mm a"));
        } catch (Exception e) {
            return iso;
        }
    }

    private void save() {
        String name = edtName.getText().toString().trim();
        String startsAt = startsAtIso;
        String endsAt = endsAtIso;
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
