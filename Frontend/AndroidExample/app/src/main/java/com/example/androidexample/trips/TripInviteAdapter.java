package com.example.androidexample.trips;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.ArrayList;
import java.util.List;

public class TripInviteAdapter extends RecyclerView.Adapter<TripInviteAdapter.VH> {

    public interface Listener {
        void onAccept(TripInviteDTO invite);
        void onDecline(TripInviteDTO invite);
    }

    private final List<TripInviteDTO> data = new ArrayList<>();
    private final Listener listener;

    public TripInviteAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<TripInviteDTO> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_invite, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TripInviteDTO t = data.get(position);

        String name = t.tripName != null ? t.tripName : "(Unnamed trip)";
        String dest = t.destination != null ? t.destination : "—";
        String inviterName = t.inviterDisplayName != null && !t.inviterDisplayName.isEmpty()
                ? t.inviterDisplayName
                : t.inviterUsername;

        h.tvTripName.setText(name);
        h.tvDestination.setText("Destination: " + dest);
        h.tvInviter.setText("Invited by: " + (inviterName != null ? inviterName : "Unknown"));

        h.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(t);
        });

        h.btnDecline.setOnClickListener(v -> {
            if (listener != null) listener.onDecline(t);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTripName, tvDestination, tvInviter;
        Button btnAccept, btnDecline;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTripName   = itemView.findViewById(R.id.tvInviteTripName);
            tvDestination = itemView.findViewById(R.id.tvInviteDestination);
            tvInviter    = itemView.findViewById(R.id.tvInviter);
            btnAccept    = itemView.findViewById(R.id.btnAcceptInvite);
            btnDecline   = itemView.findViewById(R.id.btnDeclineInvite);
        }
    }
}
