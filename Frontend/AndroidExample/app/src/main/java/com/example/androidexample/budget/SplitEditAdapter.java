package com.example.androidexample.budget;


import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.androidexample.R;


public class SplitEditAdapter extends RecyclerView.Adapter<SplitEditAdapter.ViewHolder> {


    private List<Split> splits;
    private String currentUser;
    private boolean isOwner;


    public SplitEditAdapter(List<Split> splits, String currentUser, boolean isOwner) {
        this.splits = splits;
        this.currentUser = currentUser;
        this.isOwner = isOwner;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_split_editable, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Split s = splits.get(position);


        holder.tvUsername.setText("User: " + s.getUsername());
        holder.etShare.setText(String.valueOf(s.getShareAmount()));
        holder.etPaid.setText(String.valueOf(s.getPaidAmount()));


        boolean canEdit = isOwner || s.getUsername().equalsIgnoreCase(currentUser);
        holder.etShare.setEnabled(canEdit);
        holder.etPaid.setEnabled(canEdit);


        holder.etShare.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable e) {}


            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                try {
                    double val = Double.parseDouble(c.toString());
                    s.setShareAmount(val);
                } catch (Exception ex) {
                    s.setShareAmount(0);
                }
            }
        });


        holder.etPaid.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable e) {}


            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                try {
                    double val = Double.parseDouble(c.toString());
                    s.setPaidAmount(val);
                } catch (Exception ex) {
                    s.setPaidAmount(0);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return splits.size();
    }


    public List<Split> getUpdatedSplits() {
        return splits;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        EditText etShare, etPaid;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            etShare = itemView.findViewById(R.id.etShareAmount);
            etPaid = itemView.findViewById(R.id.etPaidAmount);
        }
    }
}

