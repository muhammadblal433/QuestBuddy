package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.R;
import com.example.androidexample.DirectMessageDTO;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DirectMessageAdapter extends RecyclerView.Adapter<DirectMessageAdapter.VH> {


    public interface Callbacks {
        long me();
        long peerId();
        void onNeedMore(long beforeId);
        void onError(String msg);
        void onMarkedRead(long messageId);
    }


    private static final int SELF = 1;
    private static final int OTHER = 2;


    private final Context ctx;
    private final Callbacks cb;
    private final List<DirectMessageDTO> data = new ArrayList<>();


    public DirectMessageAdapter(Context ctx, Callbacks cb) {
        this.ctx = ctx;
        this.cb = cb;
    }


    public void prepend(List<DirectMessageDTO> older) {
        data.addAll(0, older);
        notifyItemRangeInserted(0, older.size());
    }


    public void append(DirectMessageDTO m) {
        data.add(m);
        notifyItemInserted(data.size()-1);
    }

    public long smallestId() {
        if (data.isEmpty()) return Long.MAX_VALUE;
        long min = Long.MAX_VALUE;
        for (DirectMessageDTO m : data) min = Math.min(min, m.id);
        return min;
    }


    @Override public int getItemViewType(int position) {
        return data.get(position).senderId == cb.me() ? SELF : OTHER;
    }


    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == SELF ? R.layout.item_message_self : R.layout.item_message_other;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }


    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        DirectMessageDTO m = data.get(pos);
        h.tvText.setText(m.text);
        CharSequence when = DateUtils.getRelativeTimeSpanString(m.createdAtEpochMs);
        h.tvMeta.setText((m.edited? "edited • " : "") + when);


        if (m.reactions != null && m.reactions.length() > 0) {
            h.tvReactions.setVisibility(View.VISIBLE);
            h.tvReactions.setText(renderReactions(m.reactions));
        } else {
            h.tvReactions.setVisibility(View.GONE);
        }


        h.itemView.setOnLongClickListener(v -> {
            showMessageActions(m);
            return true;
        });


// mark read if last item from peer
        if (pos == getItemCount()-1 && m.senderId == cb.peerId()) {
            markRead(m.id);
        }
    }


    @Override public int getItemCount() { return data.size(); }


    static class VH extends RecyclerView.ViewHolder {
        TextView tvText, tvMeta, tvReactions;
        VH(@NonNull View item) {
            super(item);
            tvText = item.findViewById(R.id.tvText);
            tvMeta = item.findViewById(R.id.tvMeta);
            tvReactions = item.findViewById(R.id.tvReactions);
        }
    }

    private String renderReactions(JSONObject obj) {
        StringBuilder sb = new StringBuilder();
        JSONArray names = obj.names();
        if (names == null) return "";
        for (int i=0;i<names.length();i++) {
            String k = names.optString(i);
            int c = obj.optInt(k);
            if (i>0) sb.append(' ');
            sb.append(k).append(' ').append(c);
        }
        return sb.toString();
    }


    private void showMessageActions(DirectMessageDTO m) {
        String[] items = new String[]{"Edit", "Delete", "React"};
        new AlertDialog.Builder(ctx)
                .setItems(items, (d, which) -> {
                    if (which == 0) editMessageDialog(m);
                    else if (which == 1) deleteMessage(m.id);
                    else reactDialog(m.id);
                }).show();
    }


    private Map<String,String> authHeader() {
        return new java.util.HashMap<String,String>() {{ put("X-User-Id", String.valueOf(cb.me())); }};
    }


    private void editMessageDialog(DirectMessageDTO m) {
        final EditText input = new EditText(ctx);
        input.setText(m.text);
        new AlertDialog.Builder(ctx)
                .setTitle("Edit message")
                .setView(input)
                .setPositiveButton("Save", (dd, w) -> editMessage(m.id, input.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void reactDialog(long messageId) {
        final EditText input = new EditText(ctx);
        input.setHint("Emoji, e.g. 😀 👍 ❤️");
        new AlertDialog.Builder(ctx)
                .setTitle("React with emoji")
                .setView(input)
                .setPositiveButton("React", (dd, w) -> toggleReaction(messageId, input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editMessage(long messageId, String newText) {
        String url = BASE_V10(cb.peerId()) + "/messages/" + messageId;
        JSONObject body = new JSONObject();
        try { body.put("text", newText); } catch (JSONException ignored) {}
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                res -> {},
                err -> cb.onError("Edit failed")) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(ctx).add(req);
    }


    private void deleteMessage(long messageId) {
        String url = BASE_V10(cb.peerId()) + "/messages/" + messageId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, url, null,
                res -> {},
                err -> cb.onError("Delete failed")) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(ctx).add(req);
    }


    private void toggleReaction(long messageId, String emoji) {
        if (emoji.isEmpty()) return;
        String url = BASE_V10(cb.peerId()) + "/messages/" + messageId + "/reactions?emoji=" + android.net.Uri.encode(emoji);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null,
                res -> {},
                err -> cb.onError("Reaction failed")) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(ctx).add(req);
    }


    private void markRead(long messageId) {
        String url = BASE_V10(cb.peerId()) + "/messages/" + messageId + "/read";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null,
                res -> cb.onMarkedRead(messageId),
                err -> {}) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(ctx).add(req);
    }


    private String BASE_V10(long peerId) {
        return "http://coms-3090-026.class.las.iastate.edu:8080/api/v10/direct/" + peerId;
    }
}
