package com.example.androidexample.tripplanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripEventPage {
    public List<TripEvent> content = new ArrayList<>();
    public int number;
    public int size;
    public int totalPages;
    public long totalElements;

    public static TripEventPage fromJson(JSONObject obj) throws JSONException {
        TripEventPage page = new TripEventPage();

        JSONArray contentArr = obj.getJSONArray("content");
        for (int i = 0; i < contentArr.length(); i++) {
            page.content.add(TripEvent.fromJson(contentArr.getJSONObject(i)));
        }

        page.number = obj.optInt("number", 0);
        page.size = obj.optInt("size", contentArr.length());
        page.totalPages = obj.optInt("totalPages", 1);
        page.totalElements = obj.optLong("totalElements", contentArr.length());

        return page;
    }
}
