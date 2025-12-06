package com.example.androidexample.tripplanner;

import android.content.Context;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.tripplanner.TripEvent;
import com.example.androidexample.tripplanner.TripEventPage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TripEventApi {

    private static final String BASE_URL = "http://coms-3090-026.class.las.iastate.edu:8080/api/v13";

    private final Context context;

    public TripEventApi(Context context) {
        this.context = context.getApplicationContext();
    }

    public interface PageCallback {
        void onSuccess(TripEventPage page);
        void onError(String errorMessage);
    }

    public interface EventCallback {
        void onSuccess(TripEvent event);
        void onError(String errorMessage);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private Map<String, String> buildHeaders(long userId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-User-Id", String.valueOf(userId));
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private String parseVolleyError(VolleyError error) {
        if (error.networkResponse != null) {
            int status = error.networkResponse.statusCode;
            return "HTTP " + status;
        }
        return error.getMessage() != null ? error.getMessage() : "Unknown error";
    }

    public void listEvents(
            long userId,
            long tripId,
            String fromIso,
            String toIso,
            int page,
            int size,
            PageCallback callback
    ) {
        String base = BASE_URL + "/trips/" + tripId + "/events";
        Uri.Builder builder = Uri.parse(base).buildUpon();
        builder.appendQueryParameter("page", String.valueOf(page));
        builder.appendQueryParameter("size", String.valueOf(size));
        if (fromIso != null) builder.appendQueryParameter("from", fromIso);
        if (toIso != null) builder.appendQueryParameter("to", toIso);

        String url = builder.build().toString();

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        TripEventPage p = TripEventPage.fromJson(response);
                        callback.onSuccess(p);
                    } catch (JSONException e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> callback.onError(parseVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return buildHeaders(userId);
            }
        };

        ApiClient.getInstance(context).addToRequestQueue(req);
    }

    public void createEvent(
            long userId,
            long tripId,
            String name,
            String startsAtIso,
            String endsAtIso,
            String location,
            String notes,
            Integer position,
            EventCallback callback
    ) {
        String url = BASE_URL + "/trips/" + tripId + "/events";

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("startsAt", startsAtIso);
            body.put("endsAt", endsAtIso);

            if (location != null) body.put("location", location);
            if (notes != null) body.put("notes", notes);
            if (position != null) body.put("position", position);

            body.put("attachmentRefs", new org.json.JSONArray());

            System.out.println("CREATE EVENT BODY = " + body.toString());

        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    try {
                        TripEvent event = TripEvent.fromJson(response);
                        callback.onSuccess(event);
                    } catch (JSONException e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> callback.onError(parseVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildHeaders(userId);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        ApiClient.getInstance(context).addToRequestQueue(req);
    }


    public void editEvent(
            long userId,
            long tripId,
            long eventId,
            String name,
            String startsAtIso,
            String endsAtIso,
            String location,
            String notes,
            Integer position,
            EventCallback callback
    ) {
        String url = BASE_URL + "/trips/" + tripId + "/events/" + eventId;

        JSONObject body = new JSONObject();
        try {
            if (name != null) body.put("name", name);
            if (startsAtIso != null) body.put("startsAt", startsAtIso);
            if (endsAtIso != null) body.put("endsAt", endsAtIso);
            if (location != null) body.put("location", location);
            if (notes != null) body.put("notes", notes);
            if (position != null) body.put("position", position);

            body.put("attachmentRefs", new org.json.JSONArray());

            System.out.println("EDIT EVENT BODY = " + body.toString());
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    try {
                        TripEvent event = TripEvent.fromJson(response);
                        callback.onSuccess(event);
                    } catch (JSONException e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> callback.onError(parseVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return buildHeaders(userId);
            }
        };

        ApiClient.getInstance(context).addToRequestQueue(req);
    }

    public void deleteEvent(
            long userId,
            long tripId,
            long eventId,
            SimpleCallback callback
    ) {
        String url = BASE_URL + "/trips/" + tripId + "/events/" + eventId;

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> callback.onSuccess(),
                error -> callback.onError(parseVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return buildHeaders(userId);
            }
        };

        ApiClient.getInstance(context).addToRequestQueue(req);
    }
}
