package com.questbuddy.calendar;

import com.questbuddy.calendar.dto.*;
import org.springframework.stereotype.Component;

/**
 * This class is for 3 things:
 *
 * 1. Create an event from a create DTO
 * 2. Update an event from an update DTO
 * 3. Create a response DTO based on an event
 */
@Component
public class EventMapper {
    public Event toEntity(Long userId, EventCreateDTO dto) {
        Event e = new Event();
        e.setUserId(userId);
        e.setTitle(dto.title());
        e.setDescription(dto.description());
        e.setStartAt(dto.startAt());
        e.setEndAt(dto.endAt());
        e.setLocation(dto.location());
        e.setAllDay(dto.allDay());
        return e;
    }

    // Note: check for nullity of params - handles null case to allow for partial updates
    public void applyUpdate(Event e, EventUpdateDTO dto) {
        if (dto.title() != null) e.setTitle(dto.title());
        if (dto.description() != null) e.setDescription(dto.description());
        if (dto.startAt() != null) e.setStartAt(dto.startAt());
        if (dto.endAt() != null) e.setEndAt(dto.endAt());
        if (dto.location() != null) e.setLocation(dto.location());
        if (dto.allDay() != null) e.setAllDay(dto.allDay());
    }

    public EventResponseDTO toDto(Event e) {
        return new EventResponseDTO(
                e.getId(), e.getTitle(), e.getDescription(),
                e.getStartAt(), e.getEndAt(),
                e.getLocation(), e.isAllDay(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
