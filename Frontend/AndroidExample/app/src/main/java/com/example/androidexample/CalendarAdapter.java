package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * This class is responsible for creating, structuring and populating the calendar cell in the RecyclerView
 */
class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;

    private final ArrayList<String> taskDates;

    private LocalDate monthDate;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, LocalDate monthDate, ArrayList<String> taskDates) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.monthDate = monthDate;
        this.taskDates = taskDates;
    }

    @NonNull
    @Override
    // Overrides the calendar_cell.xml file by giving each cell a height of 1/6 (since there are 6 rows) of it's parent (which is the RecyclerView)
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    // Binds each cell in the grid to the holder (which consists of the UI for each day cell) and sets the text as the day number
    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = daysOfMonth.get(position);
        holder.dayOfMonth.setText(dayText);

        if (!dayText.equals("")) {
            int day = Integer.parseInt(dayText);

            // Build a LocalDate safely using the month/year we already know
            LocalDate cellDate = monthDate.withDayOfMonth(day);
            String formattedDate = cellDate.toString();

            if (taskDates.contains(formattedDate)) {
                holder.taskIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.taskIndicator.setVisibility(View.GONE);
            }
        } else {
            holder.taskIndicator.setVisibility(View.GONE);
        }
    }

    // returns the size of our ArrayList containing all the days
    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    // This interface sets up a way for the adapter to tell the activity when a calendar day is clicked.
    public interface OnItemListener
    {
        void onItemClick(int position, String dayText);
    }
}
