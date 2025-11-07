package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidexample.R;
import java.util.ArrayList;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.VH> {

    public interface Listener {
        void onOpen(TripDTO t);
        void onEdit(TripDTO t);
        void onDelete(TripDTO t);
    }

    private final List<TripDTO> data = new ArrayList<>();
    private final Listener listener;

    public TripAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<TripDTO> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public TripDTO getAt(int pos) { return data.get(pos); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TripDTO t = data.get(pos);
        h.tvName.setText(t.name != null ? t.name : "(Unnamed Trip)");
        h.tvDestination.setText(t.destination != null ? t.destination : "—");
        String start = t.startDate != null ? t.startDate : "?";
        String end   = t.endDate   != null ? t.endDate   : "?";
        h.tvDateRange.setText(start + " \u2192 " + end);
        String startLoc = t.startLocationName != null ? t.startLocationName : "Unknown";
        h.tvStartLocation.setText("Start: " + startLoc);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onOpen(t); });
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(t); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(t); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDestination, tvDateRange, tvStartLocation;
        AppCompatImageButton btnEdit, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvStartLocation = itemView.findViewById(R.id.tvStartLocation);
            btnEdit = itemView.findViewById(R.id.btnEditName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
