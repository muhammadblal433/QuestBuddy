package com.questbuddy.tripmember.dto;

import com.questbuddy.trip.Trip;
import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.user.model.User;
import org.junit.Test;

import static org.junit.Assert.*;

public class PendingInviteDTOTest {

    @Test
    public void testFromWithFullData() {
        // Trip
        Trip trip = new Trip();
        trip.setName("Holiday Trip");
        trip.setDestination("Japan");

        // Inviter
        User inviter = new User();
        inviter.setId(10L);
        inviter.setUsername("inviterUser");

        // TripMember
        TripMember m = new TripMember();
        m.setTrip(trip);
        m.setInvitedBy(inviter);

        PendingInviteDTO dto = PendingInviteDTO.from(m);

        assertNull("Trip ID is null because Trip.id is null", dto.tripId());
        assertEquals("“Holiday Trip”", dto.tripLabel());
        assertEquals(Long.valueOf(10L), dto.invitedById());
        assertEquals("inviterUser", dto.invitedByDisplayName());
    }

    @Test
    public void testFromWithNulls() {
        TripMember m = new TripMember();

        PendingInviteDTO dto = PendingInviteDTO.from(m);

        assertNull(dto.tripId());
        assertNull(dto.tripLabel());
        assertNull(dto.invitedById());
        assertNull(dto.invitedByDisplayName());
    }

    @Test
    public void testTripLabelFallbacks() {
        Trip t = new Trip();

        // Case 1: name exists
        t.setName("Goa");
        assertEquals("“Goa”", PendingInviteDTO.from(makeMember(t, null)).tripLabel());

        // Case 2: name null, destination exists
        t.setName(null);
        t.setDestination("Spain");
        assertEquals("Trip to Spain", PendingInviteDTO.from(makeMember(t, null)).tripLabel());

        // Case 3: fallback to #id
        t.setDestination(null);
        assertEquals("Trip #null", PendingInviteDTO.from(makeMember(t, null)).tripLabel());
    }

    private TripMember makeMember(Trip t, User inviter) {
        TripMember m = new TripMember();
        m.setTrip(t);
        m.setInvitedBy(inviter);
        return m;
    }
}
