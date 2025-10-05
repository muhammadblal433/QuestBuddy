package com.questbuddy.calendar;

import com.questbuddy.calendar.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Controller for events
 *
 * Note that all CRUD logic was handled in EventService.java (so that code is more streamlined here)
 */
@RestController
@RequestMapping("/api/v1/calendar/events")
public class EventController {
    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    // Parse and validate X-User-Id header (for param)
    private Long userIdFromHeader(String header) {
        try {
            return Long.valueOf(header);
        }
        catch (NumberFormatException e) {
            throw new ValidationException("Invalid X-User-Id header (must be a number).");
        }
    }

    // POST - create event
    @PostMapping
    public EventResponseDTO create(
            @RequestHeader("X-User-Id") String xUserId,
            @Valid @RequestBody EventCreateDTO body) {
        return service.create(userIdFromHeader(xUserId), body);
    }

    // GET - list of all events so far
    @GetMapping
    public List<EventResponseDTO> list(
            @RequestHeader("X-User-Id") String xUserId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return service.list(userIdFromHeader(xUserId), from, to);
    }

    // GET - event by id
    @GetMapping("/{id}")
    public EventResponseDTO get(
            @RequestHeader("X-User-Id") String xUserId,
            @PathVariable Long id) {
        return service.get(userIdFromHeader(xUserId), id);
    }

    // PUT - update event by id
    @PutMapping("/{id}")
    public EventResponseDTO update(
            @RequestHeader("X-User-Id") String xUserId,
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO body) {
        return service.update(userIdFromHeader(xUserId), id, body);
    }

    // DELETE - Delete event by id
    @DeleteMapping("/{id}")
    public void delete(
            @RequestHeader("X-User-Id") String xUserId,
            @PathVariable Long id) {
        service.delete(userIdFromHeader(xUserId), id);
    }
}
