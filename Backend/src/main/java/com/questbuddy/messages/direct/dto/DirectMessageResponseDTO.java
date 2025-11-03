package com.questbuddy.messages.direct.dto;


import java.time.Instant;
import java.util.Map;
import java.util.Set;


/**
 * Server response for a direct message.
 * myReactions are the emojis the caller has added.
 *
 * NOTE: Difference between this and TripMessage ver. is that added read reciepts feature
 */
public record DirectMessageResponseDTO(
        Long id,
        Long senderId,
        Long recipientId,
        String content,
        Long parentMessageId,
        Long forwardFromMessageId,
        Instant sentAt,
        Instant savedAt,
        boolean edited,
        Long version,
        Map<String, Integer> reactions,
        Set<String> myReactions,
        Instant editedAt,
        boolean deleted,
        Instant deletedAt,
        Long deletedBy,

// Read receipt (for sender visibility)
        Instant readAt,
        boolean readByRecipient
) {}