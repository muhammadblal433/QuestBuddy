package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.DirectMessageDTO;
import com.example.androidexample.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RecyclerView adapter for a 1:1 chat using simple bubble layouts:
 *  - res/layout/message_in.xml  (incoming)
 *  - res/layout/message_out.xml (outgoing)
 *
 * Uses DirectMessageDTO for data binding.
 * Network actions (edit/delete/react/read) are called directly from the adapter
 * and include X-User-Id via getHeaders override (no global Volley singleton).
 */
public class DirectMessageAdapter extends RecyclerView.Adapter<DirectMessageAdapter.VH> {

    public interface Callbacks {
        long me();             // current user id (long)
        long peerId();         // friend id (long)
        void onNeedMore(long beforeId);
        void onError(String msg);
        void onMarkedRead(long messageId);
    }

    private static final int SELF  = 1;
    private static final int OTHER = 2;

    private final Context ctx;
    private final Callbacks cb;
    private final List<DirectMessageDTO> data = new ArrayList<>();

    public DirectMessageAdapter(Context ctx, Callbacks cb) {
        this.ctx = ctx;
        this.cb  = cb;
    }

    // ---------------- dataset ops ----------------
    public void prepend(List<DirectMessageDTO> older) {
        if (older == null || older.isEmpty()) return;
        data.addAll(0, older);
        notifyItemRangeInserted(0, older.size());
    }

    public void append(DirectMessageDTO m) {
        if (m == null) return;
        data.add(m);
        notifyItemInserted(data.size() - 1);
    }

    public long smallestId() {
        if (data.isEmpty()) return Long.MAX_VALUE;
        long min = Long.MAX_VALUE;
        for (DirectMessageDTO m : data) min = Math.min(min, m.id);
        return min;
    }

    // ---------------- RecyclerView ----------------
    @Override public int getItemViewType(int position) {
        return data.get(position).senderId == cb.me() ? SELF : OTHER;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == SELF) ? R.layout.item_message_out : R.layout.item_message_in;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        DirectMessageDTO m = data.get(pos);

        // Simplified bubble text
        if (m.edited && m.text == null) {
            h.text.setText("(message edited)");
        } else if (m.text == null || m.text.isEmpty()) {
            h.text.setText(m.edited ? "(message edited)" : "(no content)");
        } else {
            h.text.setText(m.text);
        }

        // Long-press actions
        h.itemView.setOnLongClickListener(v -> {
            showMessageActions(m);
            return true;
        });

        // Mark read when the last visible incoming message is rendered
        if (pos == getItemCount() - 1 && m.senderId == cb.peerId()) {
            markRead(m.id);
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView text;
        VH(@NonNull View item) {
            super(item);
            text = item.findViewById(R.id.text);
        }
    }

    // ---------------- UI dialogs ----------------
    private void showMessageActions(DirectMessageDTO m) {
        boolean isSelf = m.senderId == cb.me();
        List<String> actions = new ArrayList<>();
        if (isSelf) actions.add("Edit");
        if (isSelf) actions.add("Delete");
        actions.add("React");

        new AlertDialog.Builder(ctx)
                .setItems(actions.toArray(new String[0]), (d, which) -> {
                    String choice = actions.get(which);
                    if ("Edit".equals(choice))      editMessageDialog(m);
                    else if ("Delete".equals(choice)) deleteMessage(m.id);
                    else if ("React".equals(choice))  reactDialog(m.id);
                })
                .show();
    }

    private void editMessageDialog(DirectMessageDTO m) {
        final EditText input = new EditText(ctx);
        input.setText(m.text != null ? m.text : "");
        new AlertDialog.Builder(ctx)
                .setTitle("Edit message")
                .setView(input)
                .setPositiveButton("Save", (dd, w) -> {
                    String newText = input.getText().toString().trim();
                    if (TextUtils.isEmpty(newText)) {
                        Toast.makeText(ctx, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    editMessage(m.id, newText);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reactDialog(long messageId) {
        final EditText input = new EditText(ctx);
        input.setHint("Emoji, e.g. 😀 👍 ❤️");
        new AlertDialog.Builder(ctx)
                .setTitle("React with emoji")
                .setView(input)
                .setPositiveButton("React", (dd, w) -> {
                    String emoji = input.getText().toString().trim();
                    if (!emoji.isEmpty()) toggleReaction(messageId, emoji);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------- Networking helpers ----------------
    private Map<String,String> authHeader() {
        return new java.util.HashMap<String,String>() {{
            put("X-User-Id", String.valueOf(cb.me()));
        }};
    }

    private String baseV10() {
        // http://.../api/v10/direct/{peerId}
        return "http://coms-3090-026.class.las.iastate.edu:8080/api/v10/direct/" + cb.peerId();
    }

    /**
     * PUT /{peerId}/messages/{messageId}
     * NOTE: Server edit DTO expects "content". We send "content" even though
     * the local DTO uses field name "text".
     */
    private void editMessage(long messageId, String newContent) {
        String url = baseV10() + "/messages/" + messageId;
        JSONObject body = new JSONObject();
        try { body.put("content", newContent); } catch (JSONException ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT, url, body,
                res -> { /* Optionally refresh message list or update locally */ },
                err -> cb.onError("Edit failed")
        ) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(ctx).add(req);
    }

    /**
     * DELETE /{peerId}/messages/{messageId}
     */
    private void deleteMessage(long messageId) {
        String url = baseV10() + "/messages/" + messageId;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.DELETE, url, null,
                res -> { /* Optionally remove item locally or refetch */ },
                err -> cb.onError("Delete failed")
        ) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(ctx).add(req);
    }

    /**
     * POST /{peerId}/messages/{messageId}/reactions?emoji=...
     */
    private void toggleReaction(long messageId, String emoji) {
        String url = baseV10() + "/messages/" + messageId + "/reactions?emoji=" + android.net.Uri.encode(emoji);
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST, url, null,
                res -> { /* Server returns counts; you can refetch a single message if desired */ },
                err -> cb.onError("Reaction failed")
        ) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(ctx).add(req);
    }

    /**
     * POST /{peerId}/messages/{messageId}/read
     */
    private void markRead(long messageId) {
        String url = baseV10() + "/messages/" + messageId + "/read";
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST, url, null,
                res -> cb.onMarkedRead(messageId),
                err -> { /* ignore read errors silently */ }
        ) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(ctx).add(req);
    }
}