package com.questbuddy.tripmember.security;

public interface TripMembershipGate {
    boolean isMember(Long tripId, Long userId);
    boolean isOwner(Long tripId, Long userId);
}
