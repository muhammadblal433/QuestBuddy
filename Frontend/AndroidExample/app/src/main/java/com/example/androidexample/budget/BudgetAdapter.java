package com.example.androidexample.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

// adapter for showing budgets in a recycler view
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    // listener for budget item clicks
    public interface OnBudgetClickListener { void onClick(Budget budget); }

    private List<Budget> budgets; // list of budgets
    private OnBudgetClickListener listener; // click listener

    // constructor to set data and listener
    public BudgetAdapter(List<Budget> budgets, OnBudgetClickListener listener) {
        this.budgets = budgets;
        this.listener = listener;
    }

    // create new view for each budget
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(v);
    }

    // bind data to view holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget b = budgets.get(position);
        holder.tvName.setText(b.getName());
        holder.tvOwner.setText("Owner: " + b.getOwnerUsername());
        holder.tvTotals.setText("Paid: $" + b.getTotalPaid() + " | Share: $" + b.getTotalShare());
        holder.tvCreated.setText("Created: " + b.getCreatedAt());
        holder.itemView.setOnClickListener(v -> listener.onClick(b));
    }

    // get number of budget items
    @Override
    public int getItemCount() { return budgets.size(); }

    // holds views for each budget item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvOwner, tvTotals, tvCreated;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBudgetName);
            tvOwner = itemView.findViewById(R.id.tvOwner);
            tvTotals = itemView.findViewById(R.id.tvTotals);
            tvCreated = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}
