package com.questbuddy.events.trip.dto;

import java.time.Instant;
import java.util.List;

public record TripEventResponseDTO(
        Long id,
        Long tripId,
        Long creatorId,
        String name,
        Instant startsAt,
        Instant endsAt,
        String location,
        String notes,
        Integer position,
        List<String> attachmentRefs,
        Instant createdAt,
        Instant updatedAt,
        boolean deleted
) {}
