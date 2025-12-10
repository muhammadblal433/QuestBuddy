package com.example.androidexample.trips;
import com.example.androidexample.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.example.androidexample.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends ListAdapter<TripMessageResponseDTO, MessageAdapter.VH> {

    public interface Listener{
        void onEdit(TripMessageResponseDTO msg);
        void onDelete(TripMessageResponseDTO msg);
        void onReact(TripMessageResponseDTO msg);
        void onUnreact(TripMessageResponseDTO msg, String emoji);
    }

    private Listener listener;

    public void setListener(Listener l) { this.listener = l; }

    //id of the current user
    private final long me;

    public MessageAdapter(long me) {
        super(DIFF);
        this.me = me;
    }

    // view holder
    static class VH extends RecyclerView.ViewHolder {
        TextView username;
        TextView text;
        TextView meta;
        TextView reactions;

        VH(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.text);
            meta = itemView.findViewById(R.id.meta);
            reactions = itemView.findViewById(R.id.reactions);
        }
    }

    @Override
    public int getItemViewType(int position) {
        TripMessageResponseDTO m = getItem(position);
        return (m.getSenderId() == me)
                ? R.layout.item_message_out
                : R.layout.item_message_in;
    }

    // inflate row layout
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new VH(v);
    }

    // bind data to ui
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TripMessageResponseDTO m = getItem(position);

        // Set username - show "Me" for current user, otherwise show their username
        if (h.username != null) {
            if (m.getSenderId() == me) {
                h.username.setText("Me");
            } else {
                String username = m.getSenderUsername();
                h.username.setText(username != null && !username.isEmpty() ? username : "Unknown");
            }
        }

        // Set message content - show "(message deleted)" if deleted
        String content =
                (m.isDeleted())
                        ? "(message deleted)"
                        : (m.getContent() == null ? "" : m.getContent());

        h.text.setText(content);

        // Format timestamp and add "edited" indicator if message was edited
        String time = formatTimestamp(m.getSentAt());
        if (m.isEdited()) time += " • edited";
        h.meta.setText(time);

        // Handle reactions display
        if (m.getReactions() != null && !m.getReactions().isEmpty()) {
            android.util.Log.d("MessageAdapter", "Message has reactions: " + m.getReactions());
            android.util.Log.d("MessageAdapter", "My reactions: " + m.getMyReactions());

            // Build reactions string - show user's reactions in brackets
            StringBuilder r = new StringBuilder();
            for (String emoji : m.getReactions().keySet()) {
                int count = m.getReactions().get(emoji);
                boolean mine = m.getMyReactions() != null && m.getMyReactions().contains(emoji);

                // Wrap user's own reactions in brackets to highlight them
                if (mine) {
                    r.append("[").append(emoji).append(" ").append(count).append("]  ");
                } else {
                    r.append(emoji).append(" ").append(count).append("   ");
                }
            }

            // Show reactions text view and set the text
            h.reactions.setVisibility(View.VISIBLE);
            h.reactions.setText(r.toString().trim());
            android.util.Log.d("MessageAdapter", "Setting reactions text: " + r.toString().trim());

            // Handle clicking on reactions to remove them
            h.reactions.setOnClickListener(v -> {
                android.util.Log.d("MessageAdapter", "Reactions TextView clicked!");
                android.util.Log.d("MessageAdapter", "Listener is null? " + (listener == null));
                android.util.Log.d("MessageAdapter", "MyReactions: " + m.getMyReactions());

                // Only show remove menu if user has reactions on this message
                if (listener == null || m.getMyReactions() == null || m.getMyReactions().isEmpty()) {
                    android.util.Log.d("MessageAdapter", "Bailing out - no listener or no reactions");
                    return;
                }

                // Create popup menu with option to remove each of user's reactions
                android.util.Log.d("MessageAdapter", "Creating popup menu...");
                PopupMenu menu = new PopupMenu(v.getContext(), v);
                for (String emoji : m.getMyReactions()) {
                    android.util.Log.d("MessageAdapter", "Adding menu item: Remove " + emoji);
                    menu.getMenu().add("Remove " + emoji);
                }

                // Handle menu item selection
                menu.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    String emoji = title.replace("Remove ", "");
                    android.util.Log.d("MessageAdapter", "Menu item clicked: " + title + " -> emoji: " + emoji);
                    listener.onUnreact(m, emoji);
                    return true;
                });

                android.util.Log.d("MessageAdapter", "Showing popup menu");
                menu.show();
            });
        } else {
            // No reactions - hide the reactions view
            android.util.Log.d("MessageAdapter", "No reactions on this message");
            h.reactions.setVisibility(View.GONE);
        }

        // Handle long press on message to show edit/delete/react menu
        // Handle long press on message to show edit/delete/react menu
        View messageContainer = h.itemView.findViewById(R.id.message_container);
        if (messageContainer != null) {
            messageContainer.setOnLongClickListener(v -> {
                if (listener == null) return true;

                PopupMenu menu = new PopupMenu(v.getContext(), v);

                // Only allow editing/deleting your own messages if not deleted
                if (!m.isDeleted()) {
                    if (m.getSenderId() == me) menu.getMenu().add("Edit");
                    if (m.getSenderId() == me) menu.getMenu().add("Delete");
                }

                // Anyone can react to any message
                menu.getMenu().add("React");

                // Handle menu item selection
                menu.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    switch (title) {
                        case "Edit":
                            listener.onEdit(m);
                            break;
                        case "Delete":
                            listener.onDelete(m);
                            break;
                        case "React":
                            listener.onReact(m);
                            break;
                    }
                    return true;
                });

                menu.show();
                return true;
            });
        }
    }
    // convert iso timestamp to hh:mm
    private String formatTimestamp(String iso) {
        if (iso == null) return "";
        try {
            SimpleDateFormat parser =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            Date d = parser.parse(iso);

            SimpleDateFormat out =
                    new SimpleDateFormat("hh:mm a", Locale.US);
            return out.format(d);
        } catch (ParseException e) {
            return "";
        }
    }

    // diffutil for list changes
    private static final DiffUtil.ItemCallback<TripMessageResponseDTO> DIFF =
            new DiffUtil.ItemCallback<TripMessageResponseDTO>() {
                @Override
                public boolean areItemsTheSame(@NonNull TripMessageResponseDTO a, @NonNull TripMessageResponseDTO b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull TripMessageResponseDTO a, @NonNull TripMessageResponseDTO b) {
                    return a.getContent().equals(b.getContent())
                            && a.isEdited() == b.isEdited()
                            && a.isDeleted() == b.isDeleted()
                            && ((a.getReactions() == null && b.getReactions() == null)
                            || (a.getReactions() != null && a.getReactions().equals(b.getReactions())));
                }
            };
}


