package com.example.androidexample;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This class is responsible for
 */
public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    // displays the number on each cell
    public final TextView dayOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;
    public final View taskIndicator;

    // Stores calendar cell info in itemView and sets up how to deal with clicks.
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener)
    {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
        this.onItemListener = onItemListener;
        this.taskIndicator = itemView.findViewById(R.id.taskIndicator);
        itemView.setOnClickListener(this);
    }


    // Tells the activity which cell is clicked and what it contains
    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
    }
}
