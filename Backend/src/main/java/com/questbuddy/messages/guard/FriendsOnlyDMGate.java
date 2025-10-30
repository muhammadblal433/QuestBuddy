package com.questbuddy.messages.guard;

import com.questbuddy.friends.model.FriendshipStatus;
import com.questbuddy.friends.repository.FriendshipRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "messaging.dm.gate", havingValue = "friends") // activate when you want friends-only
@Primary // prefer this gate if multiple beans exist
public class FriendsOnlyDMGate implements DirectMessagingGate {

    private final FriendshipRepository friendships;

    public FriendsOnlyDMGate(FriendshipRepository friendships) {
        this.friendships = friendships;
    }

    @Override
    public boolean canDM(Long me, Long peer) {
        if (me == null || peer == null || me.equals(peer)) return false;          // no nulls, no self-DM
        if (friendships.existsBlockedEitherDirection(me, peer)) return false;     // respect blocks
        return friendships.findEitherDirection(me, peer)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)             // must be friends
                .orElse(false);
    }
}