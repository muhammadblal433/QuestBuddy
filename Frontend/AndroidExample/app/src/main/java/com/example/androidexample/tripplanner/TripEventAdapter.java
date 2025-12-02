package com.example.androidexample.tripplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.example.androidexample.tripplanner.TripEvent;

import java.util.ArrayList;
import java.util.List;

public class TripEventAdapter extends RecyclerView.Adapter<TripEventAdapter.EventViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TripEvent event);
        void onItemLongClick(TripEvent event);
    }

    private final List<TripEvent> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public TripEventAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TripEvent> events) {
        items.clear();
        if (events != null) {
            items.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        TripEvent event = items.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtTime;
        TextView txtLocation;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtEventName);
            txtTime = itemView.findViewById(R.id.txtEventTime);
            txtLocation = itemView.findViewById(R.id.txtEventLocation);
        }

        void bind(TripEvent event, OnItemClickListener listener) {
            txtName.setText(event.name != null ? event.name : "(no name)");
            txtTime.setText(event.startsAt != null ? event.startsAt : "");
            txtLocation.setText(event.location != null ? event.location : "");

            itemView.setOnClickListener(v -> listener.onItemClick(event));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(event);
                return true;
            });
        }
    }
}