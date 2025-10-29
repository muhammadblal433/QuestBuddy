package com.questbuddy.messages.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Edit payload for a trip message.
 * Requires the current version to prevent overwriting concurrent edits.
 */
public record TripMessageEditDTO(
        @NotBlank
        @Size(max = 2000)
        String content,

        @NotNull
        Long version
) {}