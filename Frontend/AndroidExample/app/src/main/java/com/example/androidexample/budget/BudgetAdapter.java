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

/**
 * RecyclerView adapter responsible for displaying a list of {@link Budget} items.
 * Each item row shows the budget's name, owner, totals, and creation date,
 * and notifies a listener when the user clicks on a budget.
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    /**
     * Callback interface for handling clicks on a budget item.
     */
    // listener for budget item clicks
    public interface OnBudgetClickListener {
        /**
         * Invoked when a budget item in the list is clicked.
         *
         * @param budget the {@link Budget} associated with the clicked item
         */
        void onClick(Budget budget);
    }

    private List<Budget> budgets; // list of budgets
    private OnBudgetClickListener listener; // click listener

    private String currentUsername; // username of the current user

    /**
     * Creates a new {@code BudgetAdapter} with the given list of budgets and click listener.
     *
     * @param budgets  list of budgets to display in the RecyclerView
     * @param listener callback that will be invoked when a budget is clicked
     */
    // constructor to set data and listener
    public BudgetAdapter(List<Budget> budgets, OnBudgetClickListener listener) {
        this.budgets = budgets;
        this.listener = listener;
        this.currentUsername = currentUsername;
    }

    /**
     * Inflates the item layout and returns a new {@link ViewHolder} for a budget row.
     *
     * @param parent   parent view group into which the new view will be added
     * @param viewType type of the new view (unused in this adapter)
     * @return a new {@link ViewHolder} containing the inflated view
     */
    // create new view for each budget
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Binds the budget data for the item at the given position to the provided {@link ViewHolder}.
     * This sets the name, owner label (showing "You" if the current user owns it),
     * totals, and creation date, and attaches a click listener.
     *
     * @param holder   {@link ViewHolder} whose views should be updated
     * @param position position of the item within the adapter's data set
     */
    // bind data to view holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget b = budgets.get(position);
        holder.tvName.setText(b.getName());

        if(b.getOwnerUsername().equalsIgnoreCase(currentUsername))
            holder.tvOwner.setText("Owner: You");
        else
            holder.tvOwner.setText("Owner: " + b.getOwnerUsername());

        holder.tvTotals.setText("Paid: $" + b.getTotalPaid() + " | Share: $" + b.getTotalShare());
        holder.tvCreated.setText("Created: " + b.getCreatedAt());
        holder.itemView.setOnClickListener(v -> listener.onClick(b));
    }

    /**
     * Returns the total number of budgets managed by this adapter.
     *
     * @return the number of budget items
     */
    // get number of budget items
    @Override
    public int getItemCount() { return budgets.size(); }

    /**
     * ViewHolder that holds the views for a single budget row.
     * It exposes TextViews for the budget name, owner, totals, and creation date.
     */
    // holds views for each budget item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvOwner, tvTotals, tvCreated;

        /**
         * Constructs a new {@code ViewHolder} for the provided itemView.
         * The constructor caches references to the TextViews used to display the budget data.
         *
         * @param itemView root view for the budget row
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBudgetName);
            tvOwner = itemView.findViewById(R.id.tvOwner);
            tvTotals = itemView.findViewById(R.id.tvTotals);
            tvCreated = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}