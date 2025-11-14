package com.example.androidexample.messages;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
        default void onWantsLatestRefresh() {}
    }

    private static final int SELF  = 1;
    private static final int OTHER = 2;

    private final Context ctx;
    private final Callbacks cb;
    private final List<DirectMessageDTO> data = new ArrayList<>();

    public DirectMessageAdapter(Context ctx, Callbacks cb) {
        this.ctx = ctx;
        this.cb = cb;
        setHasStableIds(true);
    }

    @Override public long getItemId(int position) {
        return data.get(position).id; // <-- unique DB id
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

        // Text
        if (m.deleted) {
            h.text.setText("(message deleted)");
        } else if (m.text == null || m.text.isEmpty()) {
            h.text.setText(m.edited ? "(message edited)" : "(no content)");
        } else {
            h.text.setText(m.text);
        }

        // Meta
        String time = (m.createdAtEpochMs > 0)
                ? java.time.format.DateTimeFormatter.ofPattern("h:mm a")
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(m.createdAtEpochMs))
                : "";
        h.meta.setText(m.deleted ? time + "  (deleted)"
                : (m.edited ? time + "  (edited)" : time));

        // Reactions (unchanged for non-deleted)
        if (m.deleted) {
            h.reactions.setVisibility(View.GONE);
            h.reactions.setText("");
        } else {
            String rx = m.reactionsDisplay();
            h.reactions.setVisibility(rx.isEmpty() ? View.GONE : View.VISIBLE);
            h.reactions.setText(rx);
        }

        // Long-press
        h.itemView.setOnLongClickListener(m.deleted ? null : v -> { showMessageActions(m); return true; });

        // Mark read for newest incoming
        if (pos == getItemCount() - 1 && m.senderId == cb.peerId()) {
            markRead(m.id);
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView text;
        TextView meta;
        TextView reactions;
        VH(@NonNull View item) {
            super(item);
            text = item.findViewById(R.id.text);
            meta = item.findViewById(R.id.meta);
            reactions = item.findViewById(R.id.reactions);
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
     */
    private void editMessage(long messageId, String newContent) {
        int idx = indexOf(messageId);
        if (idx < 0) { cb.onError("Message not found"); return; }

        DirectMessageDTO row = data.get(idx);
        String oldText = row.text;
        Integer currentVersion = row.version != null ? row.version : 0;

        // locally updates text only
        applyLocalEdit(messageId, newContent);

        String url = baseV10() + "/messages/" + messageId;
        JSONObject body = new JSONObject();
        try {
            body.put("content", newContent.trim());
            body.put("version", currentVersion);
        } catch (JSONException ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT, url, body,
                res -> {
                    DirectMessageDTO updated = DirectMessageDTO.fromJson(res);
                    data.set(idx, updated);
                    notifyItemChanged(idx);
                },
                err -> {
                    int code = err.networkResponse != null ? err.networkResponse.statusCode : -1;
                    // revert optimistic text
                    row.text = oldText;
                    notifyItemChanged(idx);

                    if (code == 409 || code == 412) {
                        cb.onError("Someone edited this message. Reloaded latest.");
                        // Optional: refetch page or single row here
                        // fetchMessages(null, 50, false);
                    } else {
                        cb.onError("Edit failed" + (code>0?" ("+code+")":""));
                    }
                }
        ) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        req.setShouldCache(false);
        Volley.newRequestQueue(ctx).add(req);
    }

    /**
     * DELETE /{peerId}/messages/{messageId}
     */
    private void deleteMessage(long messageId) {
        String url = baseV10() + "/messages/" + messageId;

        int beforeIndex = indexOf(messageId);
        DirectMessageDTO backup = (beforeIndex >= 0) ? data.get(beforeIndex) : null;
        markDeleted(messageId);

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                res -> {
                    Toast.makeText(ctx, "Message Deleted!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    cb.onError("Delete failed");
                    if (backup != null) {
                        data.add(beforeIndex, backup);
                        notifyItemInserted(beforeIndex);
                    }
                }
        ) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };

        Volley.newRequestQueue(ctx).add(req);
    }

    /**
     * POST /{peerId}/messages/{messageId}/reactions?emoji=...
     */
    private void toggleReaction(long messageId, String key) {
        int idx = indexOf(messageId); if (idx < 0) return;
        DirectMessageDTO row = data.get(idx);
        if (row.deleted) { cb.onError("Can't react to a deleted message"); return; }

        String url = baseV10() + "/messages/" + messageId
                + "/reactions?emoji=" + android.net.Uri.encode(key == null ? "" : key);

        // optimistic +1 using the EXACT key you sent to the server
        applyLocalReaction(messageId, key, /*add*/ true);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST, url, null,
                res -> {
                    // Trust server's reaction object (keys as stored in DB)
                    DirectMessageDTO m = data.get(idx);
                    m.reactions = res;
                    notifyItemChanged(idx);
                    cb.onWantsLatestRefresh();
                },
                err -> {
                    // revert optimistic change
                    applyLocalReaction(messageId, key, /*add*/ false);
                    int code = err.networkResponse != null ? err.networkResponse.statusCode : -1;
                    String body = (err.networkResponse != null && err.networkResponse.data != null)
                            ? new String(err.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8) : null;
                    android.util.Log.e("REACTION", "POST failed code=" + code + " body=" + body);
                    cb.onError("Reaction failed" + (code > 0 ? " (" + code + ")" : ""));
                }
        ) {
            @Override public Map<String, String> getHeaders() {
                Map<String,String> h = authHeader();
                h.put("Accept", "application/json");
                return h;
            }
            @Override
            protected com.android.volley.Response<org.json.JSONObject> parseNetworkResponse(
                    com.android.volley.NetworkResponse response) {
                if (response == null || response.data == null || response.data.length == 0) {
                    return com.android.volley.Response.success(
                            new org.json.JSONObject(),
                            com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                }
                return super.parseNetworkResponse(response);
            }
        };

        req.setShouldCache(false);
        Volley.newRequestQueue(ctx).add(req);
    }

    private void unreact(long messageId, String emoji) {
        int idx = indexOf(messageId);
        if (idx < 0) return;
        DirectMessageDTO row = data.get(idx);
        if (row.deleted) { cb.onError("Can't react to a deleted message"); return; }

        String url = baseV10() + "/messages/" + messageId
                + "/reactions/" + android.net.Uri.encode(emoji == null ? "" : emoji);

        // optimistic -1
        applyLocalReaction(messageId, emoji, /*add*/ false);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.DELETE, url, /*body*/ null,
                res -> {
                    DirectMessageDTO m = data.get(idx);
                    m.reactions = res;  // authoritative counts
                    notifyItemChanged(idx);
                },
                err -> {
                    // revert optimistic change
                    applyLocalReaction(messageId, emoji, /*add*/ true);

                    int code = err.networkResponse != null ? err.networkResponse.statusCode : -1;
                    String body = (err.networkResponse != null && err.networkResponse.data != null)
                            ? new String(err.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8) : null;
                    android.util.Log.e("REACTION", "DELETE failed code=" + code + " body=" + body);
                    cb.onError("Unreact failed" + (code > 0 ? " (" + code + ")" : ""));
                }
        ) {
            @Override public Map<String, String> getHeaders() {
                Map<String,String> h = authHeader();
                h.put("Accept", "application/json");
                return h;
            }
            @Override
            protected com.android.volley.Response<org.json.JSONObject> parseNetworkResponse(
                    com.android.volley.NetworkResponse response) {
                if (response == null || response.data == null || response.data.length == 0) {
                    return com.android.volley.Response.success(
                            new org.json.JSONObject(),
                            com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                }
                return super.parseNetworkResponse(response);
            }
        };

        req.setShouldCache(false);
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

    /** Find adapter index by message id */
    private int indexOf(long messageId) {
        for (int i = 0; i < data.size(); i++) if (data.get(i).id == messageId) return i;
        return -1;
    }

    private void applyLocalEdit(long messageId, String newText) {
        int i = indexOf(messageId);
        if (i < 0) return;
        DirectMessageDTO m = data.get(i);
        m.text = newText;
        m.edited = true;
        notifyItemChanged(i);
    }

    private void removeById(long messageId) {
        int i = indexOf(messageId);
        if (i < 0) {
            android.util.Log.w("DM", "removeById: not found id=" + messageId);
            return;
        }
        data.remove(i);
        notifyItemRemoved(i);
    }

    private void applyLocalReaction(long messageId, String emoji, boolean add) {
        int i = indexOf(messageId); if (i < 0) return;
        DirectMessageDTO m = data.get(i);
        if (m.reactions == null) m.reactions = new org.json.JSONObject();
        int prev = m.reactions.optInt(emoji, 0);
        int next = Math.max(prev + (add ? 1 : -1), 0);
        try { if (next == 0) m.reactions.remove(emoji); else m.reactions.put(emoji, next); } catch (Exception ignored) {}
        notifyItemChanged(i);
    }

    public boolean containsId(long id) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).id == id) return true;
        }
        return false;
    }

    // Add near other dataset ops
    public int indexOfId(long messageId) { return indexOf(messageId); }

    public void replaceAt(int index, DirectMessageDTO m) {
        data.set(index, m);
        notifyItemChanged(index);
    }

    public void upsert(DirectMessageDTO m) {
        if (m == null) return;
        int i = indexOf(m.id);
        if (i >= 0) { data.set(i, m); notifyItemChanged(i); }
        else { append(m); }
    }

    // make this one public so activity can call it on remote deletes
    public void removeByIdPublic(long messageId) { removeById(messageId); }

    public void markDeleted(long messageId) {
        int i = indexOf(messageId);
        if (i < 0) return;
        DirectMessageDTO m = data.get(i);
        m.deleted = true;
        m.text = "(message deleted)";
        m.reactions = null;
        notifyItemChanged(i);
    }

    public void setReactions(long messageId, org.json.JSONObject reactions) {
        int i = indexOf(messageId);
        if (i < 0) return;
        DirectMessageDTO m = data.get(i);
        m.reactions = reactions;
        notifyItemChanged(i);
    }
}