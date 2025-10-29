package com.questbuddy.messages.guard;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * allows any non-null user to post/list in any non-null trip.
 */
@Component
@ConditionalOnMissingBean(TripMembershipGate.class)
public class AllowAllTripMembershipGate implements TripMembershipGate {

    @Override
    public boolean isMember(Long tripId, Long userId) {
        if (tripId == null) {
            return false;
        }
        if (userId == null) {
            return false;
        }
        return true;
    }
}
