package com.questbuddy.tripmember.security;

import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.tripmember.repository.TripMemberRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class TripMembershipGateJPA implements TripMembershipGate {
    private final TripMemberRepository repo;

    public TripMembershipGateJPA(TripMemberRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean isMember(Long tripId, Long userId) {
        return repo.existsByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMember.Status.ACCEPTED);
    }

    @Override
    public boolean isOwner(Long tripId, Long userId) {
        return repo.findByTrip_IdAndUser_Id(tripId, userId)
                .map(m -> m.getStatus() == TripMember.Status.ACCEPTED && m.getRole() == TripMember.Role.OWNER)
                .orElse(false);
    }
}

