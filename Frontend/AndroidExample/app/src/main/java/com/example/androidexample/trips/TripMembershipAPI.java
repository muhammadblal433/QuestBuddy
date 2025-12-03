package com.example.androidexample.trips;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripMembershipAPI {

    private static final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080";
    private static final String LIST_MY_INVITES = BASE_URL + "/api/v12/me/trip-invites";

    private static RequestQueue queue;
    private static RequestQueue getQueue(Context ctx) {
        if (queue == null) {
            synchronized (TripMembershipAPI.class) {
                if (queue == null) {
                    queue = Volley.newRequestQueue(ctx.getApplicationContext());
                }
            }
        }
        return queue;
    }

    public interface InvitesCallback {
        void onSuccess(List<TripInviteDTO> invites);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    private static Map<String, String> authJsonHeaders(long userId) {
        Map<String, String> h = new HashMap<>();
        h.put("X-User-Id", String.valueOf(userId));
        h.put("Accept", "application/json");
        h.put("Content-Type", "application/json; charset=utf-8");
        return h;
    }

    private static Map<String, String> authHeaders(long userId) {
        Map<String, String> h = new HashMap<>();
        h.put("X-User-Id", String.valueOf(userId));
        h.put("Accept", "application/json");
        return h;
    }

    private static String normalizeVolleyError(VolleyError e) {
        if (e.networkResponse != null) {
            String body = "";
            try {
                body = new String(e.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
            return "HTTP " + e.networkResponse.statusCode + (body.isEmpty() ? "" : (" - " + body));
        }
        if (e.getCause() != null) return e.getCause().toString();
        return e.toString();
    }

    public static void listMyInvites(Context ctx, long userId, final InvitesCallback cb) {
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                LIST_MY_INVITES,
                null,
                arr -> {
                    try {
                        List<TripInviteDTO> out = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            TripInviteDTO t = new TripInviteDTO();
                            t.tripId = o.getLong("tripId");
                            t.tripName = o.optString("tripName", null);
                            t.destination = o.optString("destination", null);
                            t.inviterUsername = o.optString("inviterUsername", null);
                            t.inviterDisplayName = o.optString("inviterDisplayName", null);
                            out.add(t);
                        }
                        cb.onSuccess(out);
                    } catch (Exception e) {
                        cb.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override public Map<String, String> getHeaders() { return authHeaders(userId); }
        };

        getQueue(ctx).add(req);
    }

    public static void approveInvite(Context ctx, long userId, long tripId, final SimpleCallback cb) {
        String url = BASE_URL + "/api/v12/trips/" + tripId + "/members/approve";
        JSONObject body = new JSONObject();
        try {
            body.put("status", "ACCEPTED");
        } catch (Exception ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                obj -> cb.onSuccess(),
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override public Map<String, String> getHeaders() { return authJsonHeaders(userId); }
        };

        getQueue(ctx).add(req);
    }

    public static void declineInvite(Context ctx, long userId, long tripId, final SimpleCallback cb) {
        String url = BASE_URL + "/api/v12/trips/" + tripId + "/members/decline";
        JSONObject body = new JSONObject();
        try {
            body.put("status", "DECLINED");
        } catch (Exception ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                obj -> cb.onSuccess(),
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override public Map<String, String> getHeaders() { return authJsonHeaders(userId); }
        };

        getQueue(ctx).add(req);
    }
}
