package com.example.androidexample.trips;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import org.json.JSONObject;

public class TripMembershipAPI {

    private static final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080";
    private static String pendingInvitesUrl(long userId) {
        return BASE_URL + "/api/v12/users/" + userId + "/trip-invites/pending";
    }

    private static final String USERS_URL = BASE_URL + "/api/v2/users";

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
                pendingInvitesUrl(userId),
                null,
                arr -> {
                    try {
                        List<TripInviteDTO> out = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            TripInviteDTO t = new TripInviteDTO();
                            t.tripId = o.getLong("tripId");
                            t.tripName = o.optString("tripLabel", null);
                            t.destination = null;
                            t.inviterDisplayName = o.optString("invitedByDisplayName", null);
                            t.inviterUsername = null;
                            out.add(t);
                        }
                        cb.onSuccess(out);
                    } catch (Exception e) {
                        cb.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return authHeaders(userId);
            }
        };

        getQueue(ctx).add(req);
    }

    public static void approveInvite(Context ctx, long userId, long tripId, final SimpleCallback cb) {
        String url = BASE_URL + "/api/v12/trips/" + tripId + "/members/approve";

        // Backend expects: { "status": "ACCEPTED" }
        JSONObject body = new JSONObject();
        try {
            body.put("status", "ACCEPTED");
        } catch (Exception e) {
            cb.onError("Failed to create request body");
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> cb.onSuccess(),
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return authJsonHeaders(userId);
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    // Handle 204 No Content response
                    if (response.data == null || response.data.length == 0) {
                        return Response.success(new JSONObject(),
                                HttpHeaderParser.parseCacheHeaders(response));
                    }
                    return super.parseNetworkResponse(response);
                } catch (Exception e) {
                    return Response.success(new JSONObject(),
                            HttpHeaderParser.parseCacheHeaders(response));
                }
            }
        };

        getQueue(ctx).add(req);
    }


    public static void declineInvite(Context ctx, long userId, long tripId, final SimpleCallback cb) {
        String url = BASE_URL + "/api/v12/trips/" + tripId + "/members/decline";

        // Backend expects: { "status": "DECLINED" }
        JSONObject body = new JSONObject();
        try {
            body.put("status", "DECLINED");
        } catch (Exception e) {
            cb.onError("Failed to create request body");
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> cb.onSuccess(),
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return authJsonHeaders(userId);
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    // Handle 204 No Content response
                    if (response.data == null || response.data.length == 0) {
                        return Response.success(new JSONObject(),
                                HttpHeaderParser.parseCacheHeaders(response));
                    }
                    return super.parseNetworkResponse(response);
                } catch (Exception e) {
                    return Response.success(new JSONObject(),
                            HttpHeaderParser.parseCacheHeaders(response));
                }
            }
        };

        getQueue(ctx).add(req);
    }

    public static void inviteByUsername(
            Context ctx,
            long inviterId,
            long tripId,
            String username,
            final SimpleCallback cb
    ) {
        String trimmed = username == null ? "" : username.trim();
        if (trimmed.isEmpty()) {
            cb.onError("Username required");
            return;
        }

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                USERS_URL,
                null,
                arr -> {
                    try {
                        Long targetUserId = null;

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject u = arr.getJSONObject(i);
                            String uname = u.optString("username", "");
                            if (uname.equalsIgnoreCase(trimmed)) {
                                targetUserId = u.getLong("id");
                                break;
                            }
                        }

                        if (targetUserId == null) {
                            cb.onError("User not found: " + trimmed);
                            return;
                        }

                        inviteByUserIdInternal(ctx, inviterId, tripId, targetUserId, cb);
                    } catch (Exception e) {
                        cb.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return authHeaders(inviterId);
            }
        };

        getQueue(ctx).add(req);
    }

    private static void inviteByUserIdInternal(
            Context ctx,
            long inviterId,
            long tripId,
            long targetUserId,
            final SimpleCallback cb
    ) {
        String url = BASE_URL + "/api/v12/trips/" + tripId + "/members/invite";

        JSONObject body = new JSONObject();
        try {
            body.put("userId", targetUserId);
        } catch (Exception ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                obj -> cb.onSuccess(),
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return authJsonHeaders(inviterId);
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    if (response.data == null || response.data.length == 0) {
                        return Response.success(new JSONObject(),
                                HttpHeaderParser.parseCacheHeaders(response));
                    }
                    return super.parseNetworkResponse(response);
                } catch (Exception e) {
                    return Response.success(new JSONObject(),
                            HttpHeaderParser.parseCacheHeaders(response));
                }
            }
        };

        getQueue(ctx).add(req);
    }
}
