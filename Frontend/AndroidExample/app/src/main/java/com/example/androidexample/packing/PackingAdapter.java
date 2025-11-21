package com.example.androidexample.packing;
import com.example.androidexample.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PackingAdapter extends RecyclerView.Adapter<PackingAdapter.PackingViewHolder> {

    private Context context;
    private List<PackingItem> itemList;
    private PackingChecklistActivity activity;

    public PackingAdapter(Context context, List<PackingItem> itemList, PackingChecklistActivity activity) {
        this.context = context;
        this.itemList = itemList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_packing, parent, false);
        return new PackingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackingViewHolder holder, int position) {
        PackingItem item = itemList.get(position);
        holder.tvItemName.setText(item.getName());

        // Delete button functionality
        holder.btnDelete.setOnClickListener(v -> {
            activity.deleteItem(item.getId());
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class PackingViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName;
        Button btnDelete;

        public PackingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
