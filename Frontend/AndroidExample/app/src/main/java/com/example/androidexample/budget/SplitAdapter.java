package com.example.androidexample.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

// adapter for showing each participant's split info
public class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.ViewHolder> {

    private List<Split> splits; // list of splits
    public SplitAdapter(List<Split> splits) { this.splits = splits; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_split, parent, false);
        return new ViewHolder(v);
    }

    // binds data to each item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Split s = splits.get(position);
        holder.tvUsername.setText("User: " + s.getUsername());
        holder.tvShare.setText("Share: $" + s.getShareAmount());
        holder.tvPaid.setText("Paid: $" + s.getPaidAmount());
        holder.tvBalance.setText("Balance: $" + s.getBalance());
    }

    // returns number of items
    @Override
    public int getItemCount() { return splits.size(); }

    // holds all text views for one item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvShare, tvPaid, tvBalance;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvShare = itemView.findViewById(R.id.tvShare);
            tvPaid = itemView.findViewById(R.id.tvPaid);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}
