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
        TextView text;
        TextView meta;
        TextView reactions;

        VH(@NonNull View itemView) {
            super(itemView);
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

        String content =
                (m.isDeleted())
                        ? "(message deleted)"
                        : (m.getContent() == null ? "" : m.getContent());

        h.text.setText(content);

        // timestamp + edited flag
        String time = formatTimestamp(m.getSentAt());
        if (m.isEdited()) time += " • edited";

        // reactions string
        h.meta.setText(time);

        if (m.getReactions() != null && !m.getReactions().isEmpty()) {
            StringBuilder r = new StringBuilder();
            for (String emoji : m.getReactions().keySet()) {
                int count = m.getReactions().get(emoji);
                boolean mine = m.getMyReactions() != null && m.getMyReactions().contains(emoji);

                if (mine) {
                    r.append("[").append(emoji).append(" ").append(count).append("]  ");
                } else {
                    r.append(emoji).append(" ").append(count).append("   ");
                }
            }

            h.reactions.setVisibility(View.VISIBLE);
            h.reactions.setText(r.toString().trim());
            h.reactions.setOnClickListener(v -> {
                if (listener != null && m.getMyReactions() != null && !m.getMyReactions().isEmpty()) {
                    // Unreact first emoji
                    String emoji = m.getMyReactions().iterator().next();
                    listener.onUnreact(m, emoji);
                }
            });
        } else {
            h.reactions.setVisibility(View.GONE);
        }
        h.itemView.setOnLongClickListener(v -> {
            if (listener == null) return true;

            PopupMenu menu = new PopupMenu(v.getContext(), v);

            // Do not allow editing deleted messages
            if (!m.isDeleted()) {
                if (m.getSenderId() == me) menu.getMenu().add("Edit");
                if (m.getSenderId() == me) menu.getMenu().add("Delete");
            }

            // Always allow react
            menu.getMenu().add("React");

            menu.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                switch (title) {
                    case "Edit": listener.onEdit(m); break;
                    case "Delete": listener.onDelete(m); break;
                    case "React": listener.onReact(m); break;
                }
                return true;
            });

            menu.show();
            return true;
        });
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


