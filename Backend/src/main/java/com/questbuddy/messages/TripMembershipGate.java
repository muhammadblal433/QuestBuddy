package com.questbuddy.messages;

public interface TripMembershipGate {
    boolean isMember(Long tripId, Long userId); // is or isnt a member of trip
}
