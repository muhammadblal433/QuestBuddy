package com.example.androidexample;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.tripplanner.TripEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TripEventTest {

    @Test
    public void fromJson_parsesCorrectly() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("id", 10);
        obj.put("tripId", 3);
        obj.put("creatorId", 99);
        obj.put("name", "Hiking");
        obj.put("startsAt", "2025-01-01T10:00:00Z");
        obj.put("endsAt", "2025-01-01T12:00:00Z");
        obj.put("location", "Park");
        obj.put("notes", "Bring water");
        obj.put("position", 2);
        obj.put("attachmentRefs", new JSONArray("[\"a.png\",\"b.jpg\"]"));
        obj.put("createdAt", "2025-01-01");
        obj.put("updatedAt", "2025-01-02");
        obj.put("deleted", true);

        TripEvent e = TripEvent.fromJson(obj);

        assertEquals(10, e.id);
        assertEquals(3, e.tripId);
        assertEquals(99, e.creatorId);
        assertEquals("Hiking", e.name);
        assertEquals("2025-01-01T10:00:00Z", e.startsAt);
        assertEquals("2025-01-01T12:00:00Z", e.endsAt);
        assertEquals("Park", e.location);
        assertEquals("Bring water", e.notes);
        assertEquals(Integer.valueOf(2), e.position);
        assertEquals(2, e.attachmentRefs.size());
        assertEquals("2025-01-01", e.createdAt);
        assertEquals("2025-01-02", e.updatedAt);
        assertTrue(e.deleted);
    }
}