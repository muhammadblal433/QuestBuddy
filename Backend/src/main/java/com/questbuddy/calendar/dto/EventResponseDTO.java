package com.questbuddy.calendar.dto;

import java.time.Instant;

// Data Transfer Object of a response for what was saved for the event in calendar
public record EventResponseDTO(
        Long id, String title, String description,
        Instant startAt, Instant endAt,
        String location, boolean allDay,
        Instant createdAt, Instant updatedAt
) {}
