package com.questbuddy.events.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record TripEventCreateDTO(
        @NotBlank @Size(max = 160) String name,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt,
        @Size(max = 160) String location,
        String notes,
        Integer position,
        List<String> attachmentRefs
) {}
