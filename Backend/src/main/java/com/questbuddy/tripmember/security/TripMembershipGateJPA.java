package com.questbuddy.tripmember.security;

import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.tripmember.repository.TripMemberRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

// v12 fallback to trip owner when no join-row exists
import com.questbuddy.trip.TripRepository;

@Component
@Primary
public class TripMembershipGateJPA implements TripMembershipGate {
    private final TripMemberRepository repo;
    private final TripRepository trips;

    public TripMembershipGateJPA(TripMemberRepository repo, TripRepository trips) {
        this.repo = repo;
        this.trips = trips;
    }

    @Override
    public boolean isMember(Long tripId, Long userId) {
        // accepted member OR the owner counts as a member
        return repo.existsByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMember.Status.ACCEPTED)
                || isOwner(tripId, userId);
    }

    @Override
    public boolean isOwner(Long tripId, Long userId) {
        // Prefer join-table truth (role + status)
        boolean viaJoin = repo.findByTrip_IdAndUser_Id(tripId, userId)
                .map(m -> m.getStatus() == TripMember.Status.ACCEPTED && m.getRole() == TripMember.Role.OWNER)
                .orElse(false);
        if (viaJoin) return true;

        // Fallback for legacy trips: compare against trips.owner_id
        return trips.findById(tripId)
                .map(t -> {
                    Long ownerId = t.getOwnerId();
                    return ownerId != null && ownerId.equals(userId);
                })
                .orElse(false);
    }
}
