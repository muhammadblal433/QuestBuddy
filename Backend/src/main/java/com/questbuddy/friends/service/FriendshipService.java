package com.questbuddy.friends.service;

import com.questbuddy.friends.dto.FriendDTO;
import com.questbuddy.friends.dto.FriendSuggestionDTO;

import java.util.List;

public interface FriendshipService {
    void sendRequest(Long meId, Long targetUserId);
    void accept(Long meId, Long requesterId);
    void reject(Long meId, Long requesterId);
    void unfriend(Long meId, Long otherUserId);
    void block(Long meId, Long otherUserId);
    void unblock(Long meId, Long otherUserId);

    List<FriendDTO> listFriends(Long meId);
    List<FriendDTO> incomingRequests(Long meId);
    List<FriendDTO> outgoingRequests(Long meId);
    List<FriendSuggestionDTO> suggestions(Long meId, int limit);
}