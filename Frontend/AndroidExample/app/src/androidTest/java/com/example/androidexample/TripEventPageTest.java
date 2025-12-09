package com.example.androidexample;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tripplanner.TripEventPage;
import com.example.androidexample.tripplanner.TripEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TripEventPageTest {

    @Test
    public void fromJson_parsesPageCorrectly() throws Exception {
        JSONObject eventObj = new JSONObject();
        eventObj.put("id", 1);
        eventObj.put("name", "Lunch");

        JSONArray content = new JSONArray();
        content.put(eventObj);

        JSONObject root = new JSONObject();
        root.put("content", content);
        root.put("number", 2);
        root.put("size", 50);
        root.put("totalPages", 5);
        root.put("totalElements", 100);

        TripEventPage page = TripEventPage.fromJson(root);

        assertEquals(1, page.content.size());
        assertEquals(1, page.content.get(0).id);
        assertEquals(2, page.number);
        assertEquals(50, page.size);
        assertEquals(5, page.totalPages);
        assertEquals(100, page.totalElements);
    }
}