package com.questbuddy.events.trip.dto;

import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record TripEventEditDTO(
        @Size(max = 160) String name,
        Instant startsAt,
        Instant endsAt,
        @Size(max = 160) String location,
        String notes,
        Integer position,
        List<String> attachmentRefs
) {}

