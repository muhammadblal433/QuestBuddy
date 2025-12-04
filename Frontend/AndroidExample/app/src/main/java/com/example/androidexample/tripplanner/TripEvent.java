package com.example.androidexample.tripplanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TripEvent implements Serializable {
    public long id;
    public long tripId;
    public long creatorId;
    public String name;
    public String startsAt;
    public String endsAt;
    public String location;
    public String notes;
    public Integer position;
    public List<String> attachmentRefs;
    public String createdAt;
    public String updatedAt;
    public boolean deleted;

    public static TripEvent fromJson(JSONObject obj) throws JSONException {
        TripEvent e = new TripEvent();
        e.id = obj.optLong("id");
        e.tripId = obj.optLong("tripId");
        e.creatorId = obj.optLong("creatorId");
        e.name = obj.optString("name", null);
        e.startsAt = obj.optString("startsAt", null);
        e.endsAt = obj.optString("endsAt", null);
        e.location = obj.optString("location", null);
        e.notes = obj.optString("notes", null);

        if (obj.has("position") && !obj.isNull("position")) {
            e.position = obj.getInt("position");
        }

        e.attachmentRefs = new ArrayList<>();
        if (obj.has("attachmentRefs") && !obj.isNull("attachmentRefs")) {
            JSONArray arr = obj.getJSONArray("attachmentRefs");
            for (int i = 0; i < arr.length(); i++) {
                e.attachmentRefs.add(arr.getString(i));
            }
        }

        e.createdAt = obj.optString("createdAt", null);
        e.updatedAt = obj.optString("updatedAt", null);
        e.deleted = obj.optBoolean("deleted", false);
        return e;
    }
}