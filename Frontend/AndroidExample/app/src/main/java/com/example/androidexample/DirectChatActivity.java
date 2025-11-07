package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.DirectMessageDTO;
import com.example.androidexample.DirectMessageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;

public class DirectChatActivity extends AppCompatActivity implements DirectMessageAdapter.Callbacks {


    private long me;
    private long peerId;
    private String peerUsername, currentUsername;

    private RecyclerView rv;
    private EditText et;
    private Button btnSend;

    private ImageButton btnback;
    private DirectMessageAdapter adapter;

    private DirectDmWs dmWs;


    private boolean loading = false;


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_chat);


        me = getIntent().getLongExtra("currentUserId", -1);

        peerId = getIntent().getLongExtra("friendId", -1);
        peerUsername = getIntent().getStringExtra("friendUsername");
        currentUsername = getIntent().getStringExtra("currentUsername");


        TextView tvToolbar = findViewById(R.id.tvToolbar);
        tvToolbar.setText("@" + (peerUsername != null ? peerUsername : peerId));


        rv = findViewById(R.id.rvMessages);
        et = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnback = findViewById(R.id.btnBack);



        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);


        adapter = new DirectMessageAdapter(this, this);
        rv.setAdapter(adapter);


        // endless scroll (load older when scrolled to top)
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(-1) && !loading) { // top reached
                    long beforeId = adapter.smallestId();
                    if (beforeId != Long.MAX_VALUE) fetchMessages(beforeId, 50, true);
                }
            }
        });


        // initial page
        fetchMessages(null, 50, false);

        btnSend.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            sendMessage(text);
        });

        btnback.setOnClickListener(v -> {
            Intent i = new Intent(this, FriendsListActivity.class);
            i.putExtra("username", currentUsername);
            i.putExtra("userId", me);
            startActivity(i);
            finish();
        });
    }

    private Map<String,String> authHeader() {
        Map<String,String> h = new HashMap<>();
        h.put("X-User-Id", String.valueOf(me));
        return h;
    }

    private void fetchMessages(Long beforeId, int limit, boolean prepend) {
        loading = true;
        String url = BASE_V10() + "/messages?limit=" + limit + (beforeId != null ? ("&beforeId=" + beforeId) : "");
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                res -> {
                    List<DirectMessageDTO> list = new ArrayList<>();
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject o = res.optJSONObject(i);
                        if (o == null) continue;
                        DirectMessageDTO m = DirectMessageDTO.fromJson(o);
                        if (m.deleted) {
                            m.text = "(message deleted)";
                            m.reactions = null;
                        }
                        list.add(m);
                    }

                    // Ensure oldest -> newest within this page
                    list.sort(
                            java.util.Comparator
                                    .comparingLong((DirectMessageDTO m) -> m.createdAtEpochMs)
                                    .thenComparingLong(m -> m.id)
                    );

                    if (prepend) {
                        adapter.prepend(list);                 // older page goes on top
                    } else {
                        adapter.prepend(list);                 // first page
                        rv.scrollToPosition(adapter.getItemCount() - 1); // jump to latest
                    }
                    loading = false;
                },
                err -> {
                    loading = false;
                    Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show();
                }) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };
        Volley.newRequestQueue(this).add(req);
    }


    private void sendMessage(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) { et.setError("Message cannot be empty"); return; }
        if (trimmed.length() > 2000) { et.setError("Max 2000 characters"); return; }

        String url = BASE_V10() + "/messages";

        // Build body to match DirectMessageCreateDTO
        JSONObject body = new JSONObject();
        try {
            body.put("content", trimmed);                       //
            body.put("clientMessageId", genClientMsgId());      //
            body.put("sentAt", Instant.now()); //
            // body.put("parentMessageId", replyToId);          // or JSONObject.NULL
            // body.put("forwardFromMessageId", forwardFromId); // or JSONObject.NULL
        } catch (Exception ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                res -> {
                    et.setText("");
                },
                err -> {
                    Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show();
                }) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };

        Volley.newRequestQueue(this).add(req);
    }

    @Override protected void onStart() {
        super.onStart();
        dmWs = new DirectDmWs(
                WS_V10(),
                me,
                new DirectDmWs.Listener() {
                    @Override public void onOpen() {
                        android.util.Log.i("DM-WS", "connected");
                    }
                    @Override public void onClose(int code, String reason, boolean willRetry) {
                        android.util.Log.w("DM-WS", "closed " + code + " " + reason +
                                (willRetry ? " (reconnecting…)" : ""));
                    }
                    @Override public void onError(Throwable t) {
                        android.util.Log.e("DM-WS", "error", t);
                    }
                    @Override public void onEvent(org.json.JSONObject evt) {
                        handleWsEvent(evt);
                    }
                }
        );
        dmWs.connect();
    }

    @Override protected void onStop() {
        super.onStop();
        if (dmWs != null) { dmWs.close(); dmWs = null; }
    }

    private void handleWsEvent(org.json.JSONObject evt) {
        String event = evt.optString("event", "");

        if ("HELLO".equals(event) || "ACK".equals(event)) return;
        if ("ERROR".equals(event)) {
            android.util.Log.w("DM-WS", "server error: " + evt.optString("reason",""));
            return;
        }

        // --- NEW: handle messageId-only delete/update frames ---
        if (evt.has("messageId") && !"".equals(event)) {
            final long mid = evt.optLong("messageId", -1);
            if (mid > 0) {
                if (event.contains("DELET")) { // DM_DELETE or DM_DELETED etc.
                    runOnUiThread(() -> adapter.markDeleted(mid));
                    return;
                }
                // If reactions arrive as {event:"DM_REACTION", messageId, reactions:{...}}
                org.json.JSONObject rx = evt.optJSONObject("reactions");
                if (rx != null) {
                    runOnUiThread(() -> {
                        int i = adapter.indexOfId(mid);
                        if (i >= 0) {
                            // Rebuild just this row’s reactions field
                            // (Safest is to request a fresh row, but we can patch in place)
                            // If you prefer: fetch single row via REST here.
                            // Quick patch-in-place:
                            // NOTE: You'll need a tiny getter to access by index or set directly:
                            // data.get(i).reactions = rx; notifyItemChanged(i);
                        }
                    });
                    return;
                }
            }
        }
        // --- end new branch ---

        org.json.JSONObject m = evt.optJSONObject("message");
        if (m == null && (evt.has("content") || evt.has("id"))) {
            m = evt;
        }
        if (m == null) {
            android.util.Log.d("DM-WS", "unhandled frame: " + evt.toString());
            return;
        }

        DirectMessageDTO row = DirectMessageDTO.fromJson(m);
        if (!belongsToThisChat(row)) return;

        // Upsert (from step 2)
        runOnUiThread(() -> {
            if (row.deleted) { adapter.markDeleted(row.id);; return; }
            int existing = adapter.indexOfId(row.id);
            boolean wasMissing = existing < 0;
            adapter.upsert(row);
            if (wasMissing) rv.scrollToPosition(adapter.getItemCount() - 1);
        });
    }

    private boolean belongsToThisChat(DirectMessageDTO row) {
        return (row.senderId == peerId && row.recipientId == me)
                || (row.senderId == me && row.recipientId == peerId);
    }

    private String BASE_V10() {
        return "http://coms-3090-026.class.las.iastate.edu:8080/api/v10/direct/" + peerId;
    }

    private String WS_V10() {
        // ws://host:port/api/v10/direct/{peerId}/ws
        String pairKey = canonicalPairKey(me, peerId);
        return "ws://coms-3090-026.class.las.iastate.edu:8080/ws/messages/dm/" + pairKey + "/" + me;
    }

    private String canonicalPairKey(long a, long b) {
        long lo = Math.min(a, b);
        long hi = Math.max(a, b);
        return lo + ":" + hi;
    }

    private String genClientMsgId() {
        String id = java.util.UUID.randomUUID().toString(); // 36 chars with hyphens
        return id.length() <= 64 ? id : id.substring(0, 64);
    }


    // ==== Adapter callbacks ====
    @Override public long me() { return me; }
    @Override public long peerId() { return peerId; }
    @Override public void onNeedMore(long beforeId) { fetchMessages(beforeId, 50, true); }
    @Override public void onError(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
    @Override public void onMarkedRead(long messageId) { /* optionally show a tick */ }
}
