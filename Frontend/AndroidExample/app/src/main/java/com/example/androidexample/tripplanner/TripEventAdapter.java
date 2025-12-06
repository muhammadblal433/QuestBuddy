package com.example.androidexample.tripplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.example.androidexample.tripplanner.TripEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TripEventAdapter extends RecyclerView.Adapter<TripEventAdapter.EventViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TripEvent event);

        void onItemLongClick(TripEvent event);
        void onDeleteClick(TripEvent event);
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
        ImageButton btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtEventName);
            txtTime = itemView.findViewById(R.id.txtEventTime);
            txtLocation = itemView.findViewById(R.id.txtEventLocation);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(TripEvent event, OnItemClickListener listener) {
            txtName.setText(event.name != null ? event.name : "(no name)");
            txtTime.setText(formatInstant(event.startsAt));
            txtLocation.setText(event.location != null ? event.location : "");

            itemView.setOnClickListener(v -> listener.onItemClick(event));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(event);
                return true;
            });

            btnDelete.setOnClickListener(v -> listener.onDeleteClick(event));
        }

        private String formatInstant(String iso) {
            if (iso == null || iso.isEmpty()) return "";
            try {
                Instant instant = Instant.parse(iso);
                ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("MMM dd, yyyy • h:mm a");

                return zdt.format(formatter);
            } catch (Exception e) {
                return iso; // fallback to raw if parsing fails
            }
        }
    }
}