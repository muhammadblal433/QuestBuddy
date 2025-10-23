package com.questbuddy.messages.dto;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Server response for a trip (group) message.
 * myReactions are the emojis the caller has added.
 */
public record TripMessageDTO(
        Long id,
        Long tripId,
        Long senderId,
        String content,
        Long parentMessageId,
        Long forwardFromMessageId,
        Instant sentAt,
        Instant savedAt,
        boolean edited, // has this message been edited yet?
        Long version,
        Map<String, Integer> reactions, // map of emoji rxns and their count
        Set<String> myReactions // set of emojis user has reacted w/ -> use set as duplicated nt possible
) {}
