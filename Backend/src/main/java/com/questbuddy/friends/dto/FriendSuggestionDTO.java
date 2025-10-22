package com.questbuddy.friends.dto;

public record FriendSuggestionDTO(
        Long id,
        String displayName,
        String username,
        int mutualCount
) {}