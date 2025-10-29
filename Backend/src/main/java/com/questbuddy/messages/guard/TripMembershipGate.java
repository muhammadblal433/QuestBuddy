package com.questbuddy.messages.guard;

public interface TripMembershipGate {
    boolean isMember(Long tripId, Long userId); // is or isnt a member of trip
}
