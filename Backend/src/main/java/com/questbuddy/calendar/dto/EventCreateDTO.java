package com.questbuddy.calendar.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

// Data Transfer Object of creating an event in calendar - no id , time created or updated needed
public record EventCreateDTO(
        @NotBlank @Size(max=200) String title,
        @Size(max=2000) String description,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        @Size(max=300) String location,
        boolean allDay
) {}