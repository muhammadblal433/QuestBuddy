package com.questbuddy.tripmember.model;

import com.questbuddy.trip.Trip;
import com.questbuddy.user.model.User;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class TripMemberTest {

    @Test
    public void testPrePersistSetsTimestamps() {
        TripMember m = new TripMember();
        assertNull(m.getCreatedAt());
        assertNull(m.getUpdatedAt());

        m.onCreate();

        assertNotNull(m.getCreatedAt());
        assertNotNull(m.getUpdatedAt());
    }

    @Test
    public void testPreUpdateUpdatesTimestamp() throws InterruptedException {
        TripMember m = new TripMember();
        m.onCreate();
        Instant before = m.getUpdatedAt();

        Thread.sleep(5); // small delay
        m.onUpdate();
        Instant after = m.getUpdatedAt();

        assertTrue(after.isAfter(before));
    }

    @Test
    public void testGettersAndSetters() {
        TripMember m = new TripMember();

        Trip trip = new Trip();
        User user = new User();
        User inviter = new User();

        m.setTrip(trip);
        m.setUser(user);
        m.setInvitedBy(inviter);
        m.setRole(TripMember.Role.OWNER);
        m.setStatus(TripMember.Status.ACCEPTED);

        assertEquals(trip, m.getTrip());
        assertEquals(user, m.getUser());
        assertEquals(inviter, m.getInvitedBy());
        assertEquals(TripMember.Role.OWNER, m.getRole());
        assertEquals(TripMember.Status.ACCEPTED, m.getStatus());
    }
}
