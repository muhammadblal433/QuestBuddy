package com.example.androidexample.trips;
import android.net.Uri;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;


public class TripChatApi {

    // Takes any generic response typing
    public interface Callback<T> { void onSuccess(T value); void onError(Throwable t); }

    private final String baseUrl;
    private final RequestQueue queue;


    public TripChatApi(String baseUrl, RequestQueue queue) {
        this.baseUrl = baseUrl; this.queue = queue;
    }


    private Map<String, String> headers(long me) {
        Map<String, String> h = new HashMap<>();
        h.put("X-User-Id", String.valueOf(me));
        h.put("Accept", "application/json");
        h.put("Content-Type", "application/json");
        return h;
    }

    // GET /api/v9/trips/{tripId}/messages?beforeId=&limit=
    public void listMessages(long me, long tripId, Long beforeId, Integer limit,
                             Callback<List<TripMessageResponseDTO>> cb) {
        Uri.Builder b = Uri.parse(baseUrl + "/api/v9/trips/" + tripId + "/messages").buildUpon();
        if (beforeId != null) b.appendQueryParameter("beforeId", String.valueOf(beforeId));
        if (limit != null) b.appendQueryParameter("limit", String.valueOf(limit));
        String url = b.build().toString();


        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<TripMessageResponseDTO> out = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            out.add(parseMessage(response.getJSONObject(i)));
                        }
                        cb.onSuccess(out);
                    } catch (JSONException e) { cb.onError(e); }
                },
                error -> cb.onError(error)) {
            @Override public Map<String, String> getHeaders() { return headers(me); }
        };
        queue.add(req);
    }


    // POST /api/v9/trips/{tripId}/messages
    public void post(long me, long tripId, TripMessageCreateDTO in,
                     Callback<TripMessageResponseDTO> cb) {
        String url = baseUrl + "/api/v9/trips/" + tripId + "/messages";
        JSONObject body = new JSONObject();
        try {
            body.put("content", in.getContent());
            if (in.getParentMessageId() != null) body.put("parentMessageId", in.getParentMessageId());
            if (in.getForwardFromMessageId() != null) body.put("forwardFromMessageId", in.getForwardFromMessageId());
            body.put("clientMessageId", in.getClientMessageId());
            if (in.getSentAt() != null) body.put("sentAt", in.getSentAt());
        } catch (JSONException ignored) {}


        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> cb.onSuccess(parseMessage(response)),
                error -> cb.onError(error)) {
            @Override public Map<String, String> getHeaders() { return headers(me); }
        };
        queue.add(req);
    }

    // PUT /api/v9/trips/{tripId}/messages/{messageId}
    public void edit(long me, long tripId, long messageId, TripMessageEditDTO in,
                     Callback<TripMessageResponseDTO> cb) {
        String url = baseUrl + "/api/v9/trips/" + tripId + "/messages/" + messageId;
        JSONObject body = new JSONObject();
        try { body.put("content", in.getContent()); body.put("version", in.getVersion()); } catch (JSONException ignored) {}


        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> cb.onSuccess(parseMessage(response)),
                error -> cb.onError(error)) {
            @Override public Map<String, String> getHeaders() { return headers(me); }
        };
        queue.add(req);
    }

    // DELETE /api/v9/trips/{tripId}/messages/{messageId}?version=
    public void delete(long me, long tripId, long messageId, long version,
                       Callback<TripMessageResponseDTO> cb) {
        Uri url = Uri.parse(baseUrl + "/api/v9/trips/" + tripId + "/messages/" + messageId)
                .buildUpon().appendQueryParameter("version", String.valueOf(version)).build();


        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, url.toString(), null,
                response -> cb.onSuccess(parseMessage(response)),
                error -> cb.onError(error)) {
            @Override public Map<String, String> getHeaders() { return headers(me); }
        };
        queue.add(req);
    }

    // POST /api/v9/trips/{messageId}/reactions body: (for emoji's)
    public void react(long me, long tripId, long messageId, String emoji,
                      Callback<Map<String, Integer>> cb) {
        String url = baseUrl + "/api/v9/trips/" + tripId + "/messages/" + messageId + "/reactions";
        JSONObject body = new JSONObject();
        try { body.put("emoji", emoji); } catch (JSONException ignored) {}


        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> cb.onSuccess(jsonObjectToMap(response)),
                error -> cb.onError(error)) {
            @Override public Map<String, String> getHeaders() { return headers(me); }
        };
        queue.add(req);
    }


    // DELETE /api/v9/trips/{messageId}/reactions/{emoji}
    public void unreact(long me, long tripId, long messageId, String emoji,
                        Callback<Map<String, Integer>> cb) {
        String url = baseUrl + "/api/v9/trips/" + tripId + "/messages/" + messageId + "/reactions/" + emoji;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> cb.onSuccess(jsonObjectToMap(response)),
                error -> cb.onError(error)) {
            @Override public Map<String, String> getHeaders() { return headers(me); }
        };
        queue.add(req);
    }

    // ---- helpers ----
    private TripMessageResponseDTO parseMessage(JSONObject o) {
        long id = o.optLong("id");
        long tripId = o.optLong("tripId");
        long senderId = o.optLong("senderId");
        String content = o.isNull("content") ? null : o.optString("content", null);
        Long parentMessageId = o.isNull("parentMessageId") ? null : o.optLong("parentMessageId");
        Long forwardFromMessageId = o.isNull("forwardFromMessageId") ? null : o.optLong("forwardFromMessageId");
        String sentAt = o.isNull("sentAt") ? null : o.optString("sentAt", null);
        String savedAt = o.isNull("savedAt") ? null : o.optString("savedAt", null);
        boolean edited = o.optBoolean("edited", false);
        Long version = o.isNull("version") ? null : o.optLong("version");
        String editedAt = o.isNull("editedAt") ? null : o.optString("editedAt", null);
        boolean deleted = o.optBoolean("deleted", false);
        String deletedAt = o.isNull("deletedAt") ? null : o.optString("deletedAt", null);
        Long deletedBy = o.isNull("deletedBy") ? null : o.optLong("deletedBy");


        Map<String, Integer> reactions = new HashMap<>();
        JSONObject rx = o.optJSONObject("reactions");
        if (rx != null) {
            Iterator<String> it = rx.keys();
            while (it.hasNext()) { String k = it.next(); reactions.put(k, rx.optInt(k, 0)); }
        }
        Set<String> myReactions = new HashSet<>();
        JSONArray my = o.optJSONArray("myReactions");
        if (my != null) for (int i = 0; i < my.length(); i++) myReactions.add(my.optString(i));


        return new TripMessageResponseDTO(id, tripId, senderId, content,
                parentMessageId, forwardFromMessageId, sentAt, savedAt, edited, version,
                reactions, myReactions, editedAt, deleted, deletedAt, deletedBy);
    }


    private Map<String, Integer> jsonObjectToMap(JSONObject o) {
        Map<String, Integer> out = new HashMap<>();
        Iterator<String> it = o.keys();
        while (it.hasNext()) { String k = it.next(); out.put(k, o.optInt(k, 0)); }
        return out;
    }
}