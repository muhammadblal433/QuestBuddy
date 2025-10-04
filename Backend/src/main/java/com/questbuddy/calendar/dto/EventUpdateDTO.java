package com.questbuddy.calendar.dto;

import java.time.Instant;

// Data Transfer Object for updating an event in the calander
// Note: allow for blank values so that we can "partially update" an event
public record EventUpdateDTO(
        String title,
        String description,
        Instant startAt,
        Instant endAt,
        String location,
        Boolean allDay
) {}
