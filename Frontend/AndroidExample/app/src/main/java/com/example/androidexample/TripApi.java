package com.example.androidexample;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripAPI {

    private static final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080";
    private static final String LIST_TRIPS = BASE_URL + "/api/v6/trips";

    private static RequestQueue queue;
    private static RequestQueue getQueue(Context ctx) {
        if (queue == null) {
            synchronized (TripAPI.class) {
                if (queue == null) {
                    queue = Volley.newRequestQueue(ctx.getApplicationContext());
                }
            }
        }
        return queue;
    }

    // ---------- Callbacks ----------
    public interface ListCallback   { void onSuccess(List<TripDTO> trips); void onError(String message); }
    public interface OneCallback    { void onSuccess(TripDTO trip);        void onError(String message); }
    public interface VoidCallback   { void onSuccess();                    void onError(String message); }

    // ---------- GET (already had) ----------
    public static void fetchTrips(Context ctx, long userId, final ListCallback cb) {
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                LIST_TRIPS,
                null,
                arr -> {
                    try {
                        cb.onSuccess(parseTripArray(arr));
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

    // ---------- POST /api/v6/trips ----------
    public static void createTrip(Context ctx, long userId, TripDTO in, final OneCallback cb) {
        JSONObject body = toCreateOrUpdateJson(in);
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                LIST_TRIPS,
                body,
                obj -> {
                    try { cb.onSuccess(parseTrip(obj)); }
                    catch (Exception e) { cb.onError("Parse error: " + e.getMessage()); }
                },
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override public Map<String, String> getHeaders() { return authJsonHeaders(userId); }
        };
        getQueue(ctx).add(req);
    }

    // ---------- PUT /api/v6/trips/{id} ----------
    public static void updateTrip(Context ctx, long userId, long id, TripDTO in, final OneCallback cb) {
        String url = LIST_TRIPS + "/" + id;
        JSONObject body = toCreateOrUpdateJson(in);
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                obj -> {
                    try { cb.onSuccess(parseTrip(obj)); }
                    catch (Exception e) { cb.onError("Parse error: " + e.getMessage()); }
                },
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override public Map<String, String> getHeaders() { return authJsonHeaders(userId); }
        };
        getQueue(ctx).add(req);
    }

    // ---------- DELETE /api/v6/trips/{id} ----------
    public static void deleteTrip(Context ctx, long userId, long id, final VoidCallback cb) {
        String url = LIST_TRIPS + "/" + id;
        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                s -> cb.onSuccess(),
                error -> cb.onError(normalizeVolleyError(error))
        ) {
            @Override public Map<String, String> getHeaders() { return authHeaders(userId); }
        };
        getQueue(ctx).add(req);
    }

    // ---------- Helpers ----------
    private static Map<String, String> authHeaders(long userId) {
        Map<String, String> h = new HashMap<>();
        h.put("X-User-Id", String.valueOf(userId));
        h.put("Accept", "application/json");
        return h;
    }

    private static Map<String, String> authJsonHeaders(long userId) {
        Map<String, String> h = authHeaders(userId);
        h.put("Content-Type", "application/json; charset=utf-8");
        return h;
    }

    private static JSONObject toCreateOrUpdateJson(TripDTO t) {
        JSONObject o = new JSONObject();
        try {
            if (t.name != null) o.put("name", t.name);
            if (t.destination != null) o.put("destination", t.destination);
            if (t.startLocationName != null) o.put("startLocationName", t.startLocationName);
            if (t.startLat != null) o.put("startLat", t.startLat);
            if (t.startLon != null) o.put("startLon", t.startLon);
            if (t.startDate != null) o.put("startDate", t.startDate);
            if (t.endDate != null) o.put("endDate", t.endDate);
        } catch (Exception ignored) {}
        return o;
    }

    private static TripDTO parseTrip(JSONObject o) throws Exception {
        TripDTO t = new TripDTO();
        if (!o.isNull("id"))      t.id = o.getLong("id");
        if (!o.isNull("ownerId")) t.ownerId = o.getLong("ownerId");
        t.name = o.optString("name", null);
        t.destination = o.optString("destination", null);
        t.startLocationName = o.optString("startLocationName", null);
        if (!o.isNull("startLat")) t.startLat = o.getDouble("startLat");
        if (!o.isNull("startLon")) t.startLon = o.getDouble("startLon");
        t.startDate = o.optString("startDate", null);
        t.endDate   = o.optString("endDate", null);
        t.createdAt = o.optString("createdAt", null);
        t.updatedAt = o.optString("updatedAt", null);
        return t;
    }

    private static List<TripDTO> parseTripArray(JSONArray arr) throws Exception {
        List<TripDTO> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) out.add(parseTrip(arr.getJSONObject(i)));
        return out;
    }

    private static String normalizeVolleyError(VolleyError e) {
        if (e.networkResponse != null) {
            String body = "";
            try { body = new String(e.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8); } catch (Exception ignored) {}
            return "HTTP " + e.networkResponse.statusCode + (body.isEmpty() ? "" : (" - " + body));
        }
        if (e.getCause() != null) return e.getCause().toString();
        return e.toString();
    }
}
