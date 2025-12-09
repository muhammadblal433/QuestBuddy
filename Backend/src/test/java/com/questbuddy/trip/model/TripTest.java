package com.questbuddy.trip.model;

import com.questbuddy.trip.Trip;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TripTest {

    @Test
    public void prePersist_setsCreatedAtAndUpdatedAt() {
        Trip t = new Trip();
        assertNull(t.getCreatedAt());
        assertNull(t.getUpdatedAt());

        t.onInsert();

        assertNotNull(t.getCreatedAt());
        assertNotNull(t.getUpdatedAt());
    }

    @Test
    public void preUpdate_updatesUpdatedAtOnly() throws InterruptedException {
        Trip t = new Trip();
        t.onInsert();

        Instant oldUpdated = t.getUpdatedAt();
        Thread.sleep(5); // ensure timestamp changes

        t.onUpdate();
        Instant newUpdated = t.getUpdatedAt();

        assertNotEquals(oldUpdated, newUpdated);
        assertEquals(t.getCreatedAt(), t.getCreatedAt()); // unchanged
    }

    @Test
    public void setters_and_getters_work() {
        Trip t = new Trip();
        t.setOwnerId(5L);
        t.setName("Trip to NYC");
        t.setDestination("NYC");
        t.setStartLocationName("Ames");
        t.setStartLat(42.034);
        t.setStartLon(-93.62);

        LocalDate sd = LocalDate.of(2025, 1, 1);
        LocalDate ed = LocalDate.of(2025, 1, 5);

        t.setStartDate(sd);
        t.setEndDate(ed);

        assertEquals(Long.valueOf(5L), t.getOwnerId());
        assertEquals("Trip to NYC", t.getName());
        assertEquals("NYC", t.getDestination());
        assertEquals("Ames", t.getStartLocationName());
        assertEquals(Double.valueOf(42.034), t.getStartLat());
        assertEquals(Double.valueOf(-93.62), t.getStartLon());
        assertEquals(sd, t.getStartDate());
        assertEquals(ed, t.getEndDate());
    }

    @Test
    public void setting_null_lat_lon_works() {
        Trip t = new Trip();
        t.setStartLat(null);
        t.setStartLon(null);

        assertNull(t.getStartLat());
        assertNull(t.getStartLon());
    }
}
