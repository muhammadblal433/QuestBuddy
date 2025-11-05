package com.example.androidexample;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

public class DirectChatActivity extends AppCompatActivity implements DirectMessageAdapter.Callbacks {


    private long me;
    private long peerId;
    private String peerUsername;


    private RecyclerView rv;
    private EditText et;
    private Button btnSend;
    private DirectMessageAdapter adapter;


    private boolean loading = false;


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_chat);


        me = getIntent().getLongExtra("currentUserId", -1);
        peerId = getIntent().getLongExtra("friendId", -1);
        peerUsername = getIntent().getStringExtra("friendUsername");


        TextView tvToolbar = findViewById(R.id.tvToolbar);
        tvToolbar.setText("@" + (peerUsername != null ? peerUsername : peerId));


        rv = findViewById(R.id.rvMessages);
        et = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);


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
                        if (o != null) list.add(DirectMessageDTO.fromJson(o));
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
                    DirectMessageDTO m = DirectMessageDTO.fromJson(res);   // ensure model reads "content" field
                    adapter.append(m);
                    rv.scrollToPosition(adapter.getItemCount() - 1);
                    et.setText("");
                },
                err -> {
                    Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show();
                }) {
            @Override public Map<String, String> getHeaders() { return authHeader(); }
        };

        Volley.newRequestQueue(this).add(req);
    }


    private String BASE_V10() {
        return "http://coms-3090-026.class.las.iastate.edu:8080/api/v10/direct/" + peerId;
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
