package com.example.androidexample;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripAPI {

    private static final String BASE_URL = "http://YOUR_SERVER_HOST:8080";
    // TODO: Adjust path to match your controller
    private static final String LIST_TRIPS = BASE_URL + "/api/v10/trips";

    // App-scoped RequestQueue owned by TripApi (no separate class/file)
    private static RequestQueue queue;

    private static RequestQueue getQueue(Context ctx) {
        if (queue == null) {
            synchronized (TripAPI.class) {
                if (queue == null) {
                    // Use applicationContext to avoid leaking an Activity
                    queue = Volley.newRequestQueue(ctx.getApplicationContext());
                }
            }
        }
        return queue;
    }

    public interface ListCallback {
        void onSuccess(List<TripDTO> trips);
        void onError(String message);
    }

    public static void fetchTrips(Context ctx, final ListCallback cb) {
        // If API returns an array: use JsonArrayRequest
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
            // If you need headers (e.g., auth), uncomment:
            // @Override public Map<String, String> getHeaders() {
            //     Map<String, String> h = new HashMap<>();
            //     h.put("Authorization", "Bearer " + token);
            //     return h;
            // }
        };

        getQueue(ctx).add(req);

        /* ---- If your endpoint returns an object wrapper, use this instead ----
        JsonObjectRequest reqObj = new JsonObjectRequest(
                Request.Method.GET,
                LIST_TRIPS,
                null,
                obj -> {
                    try {
                        JSONArray arr = obj.getJSONArray("content"); // adjust key
                        cb.onSuccess(parseTripArray(arr));
                    } catch (Exception e) {
                        cb.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> cb.onError(normalizeVolleyError(error))
        );
        getQueue(ctx).add(reqObj);
        ----------------------------------------------------------------------- */
    }

    private static List<TripDTO> parseTripArray(JSONArray arr) throws Exception {
        List<TripDTO> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
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

            out.add(t);
        }
        return out;
    }

    private static String normalizeVolleyError(VolleyError e) {
        if (e.networkResponse != null) return "HTTP " + e.networkResponse.statusCode;
        if (e.getCause() != null) return e.getCause().toString();
        return e.toString();
    }
}
