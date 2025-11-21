package com.example.androidexample.notifications;
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

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private final Context context;
    private final List<NotificationModel> notifications;
    private final OnNotificationActionListener listener;

    // interface for handling mark as read and delete actions
    public interface OnNotificationActionListener {
        void onMarkAsRead(NotificationModel n);
        void onDelete(NotificationModel n);
    }

    // initializes adapter with context, list, and listener
    public NotificationsAdapter(Context context, List<NotificationModel> notifications, OnNotificationActionListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    // inflates the layout for each notification item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    // binds notification data to the view elements
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel n = notifications.get(position);

        holder.tvTitle.setText(n.getTitle());
        holder.tvMessage.setText(n.getMessage());
        holder.tvType.setText("Type: " + n.getType());
        holder.tvCreatedAt.setText("Created: " + n.getCreatedAt());

        if (n.isRead()) {
            holder.tvStatus.setText("Status: Read");
            holder.tvStatus.setTextColor(0xFF4CAF50);
        } else {
            holder.tvStatus.setText("Status: Unread");
            holder.tvStatus.setTextColor(0xFFF44336);
        }

        holder.btnMarkRead.setOnClickListener(v -> {
            if (!n.isRead()) {
                listener.onMarkAsRead(n);
            }
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(n));
    }

    // returns the number of notifications in the list
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // holds references to the ui components for each notification item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvType, tvCreatedAt, tvStatus;
        Button btnMarkRead, btnDelete;

        // links ui elements from xml layout to java variables
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvType = itemView.findViewById(R.id.tvType);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMarkRead = itemView.findViewById(R.id.btnMarkRead);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
