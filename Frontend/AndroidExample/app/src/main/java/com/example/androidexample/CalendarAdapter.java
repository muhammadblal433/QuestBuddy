package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * This class is responsible for creating, structuring and populating the calendar cell in the RecyclerView
 */
class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener)
    {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
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
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        holder.dayOfMonth.setText(daysOfMonth.get(position));
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
