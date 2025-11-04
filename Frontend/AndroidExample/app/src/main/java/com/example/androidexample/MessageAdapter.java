package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.example.androidexample.TripMessageResponseDTO;

public class MessageAdapter extends ListAdapter<TripMessageResponseDTO, RecyclerView.ViewHolder> {
    private final long me;

    public MessageAdapter(long me) { super(DIFF); this.me = me; }

    @Override
    public int getItemViewType(int position) {
        TripMessageResponseDTO m = getItem(position);
        return (m.getSenderId() == me) ? R.layout.item_message_out : R.layout.item_message_in;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new RecyclerView.ViewHolder(v) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextView tv = holder.itemView.findViewById(R.id.text);
        TripMessageResponseDTO m = getItem(position);
        String edited = m.isEdited() ? " (edited)" : "";
        String deleted = m.isDeleted() ? " (deleted)" : "";
        tv.setText((m.getContent() == null ? "" : m.getContent()) + edited + deleted);
    }

    private static final DiffUtil.ItemCallback<TripMessageResponseDTO> DIFF = new DiffUtil.ItemCallback<TripMessageResponseDTO>() {
        @Override public boolean areItemsTheSame(@NonNull TripMessageResponseDTO a, @NonNull TripMessageResponseDTO b) { return a.getId() == b.getId(); }
        @Override public boolean areContentsTheSame(@NonNull TripMessageResponseDTO a, @NonNull TripMessageResponseDTO b) { return a.equals(b); }
    };
}

