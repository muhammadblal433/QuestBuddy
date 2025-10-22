package com.questbuddy.friends.dto;

import com.questbuddy.friends.model.FriendshipStatus;

public record FriendDTO(
        Long id,
        String displayName,
        String username,
        FriendshipStatus status
) {}