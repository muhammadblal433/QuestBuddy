package com.questbuddy.messages.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * DTO to create a new trip (group) message.
 * Server will fill savedAt; client supplies a stable clientMessageId for idempotency.
 */
public record TripMessageCreateDTO(
        @NotBlank
        @Size(max = 2000)
        String content,

        Long parentMessageId,
        Long forwardFromMessageId,

        @NotBlank
        @Size(max = 64)
        String clientMessageId,

        Instant sentAt
) {}
