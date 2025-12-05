package com.questbuddy.tripmember.dto;

import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.trip.Trip;
import com.questbuddy.user.model.User;

public record PendingInviteDTO(
        Long tripId,
        String tripLabel,
        Long invitedById,
        String invitedByDisplayName
) {

    public static PendingInviteDTO from(TripMember m) {
        Trip trip = m.getTrip();
        User inviter = m.getInvitedBy();

        Long tripId = (trip != null ? trip.getId() : null);
        String label = buildTripLabel(trip);

        Long inviterId = (inviter != null ? inviter.getId() : null);
        String inviterName = (inviter != null ? buildUserLabel(inviter) : null);

        return new PendingInviteDTO(tripId, label, inviterId, inviterName);
    }

    private static String buildTripLabel(Trip t) {
        if (t == null) return null;

        try {
            String name = safe(t.getName());
            if (name != null) return "“" + name + "”";
        } catch (NoSuchMethodError | Exception ignored) {}

        try {
            String dest = safe(t.getDestination());
            if (dest != null) return "Trip to " + dest;
        } catch (NoSuchMethodError | Exception ignored) {}

        return "Trip #" + t.getId();
    }

    private static String buildUserLabel(User u) {
        if (u == null) return null;

        String display = safe(u.getUsername());
        if (display != null) return display;

        String uname = safe(u.getUsername());
        if (uname != null) return uname;

        String email = safe(u.getEmail());
        if (email != null) return email;

        return "User#" + u.getId();
    }

    private static String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
