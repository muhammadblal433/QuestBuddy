package com.questbuddy.calendar;

import com.questbuddy.calendar.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Controller for events
 *
 * Note that all CRUD logic was handled in EventService.java (so that code is more streamlined here)
 */
@RestController
@RequestMapping("/api/v4/calendar/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService service) {
        eventService = service;
    }

    // POST - create event
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<EventResponseDTO> create(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid EventCreateDTO body
    ) {
        var out = eventService.create(userId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    // GET - list of all events so far by userId
    @GetMapping(produces = "application/json")
    public List<EventResponseDTO> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return eventService.list(userId, from, to);
    }

    // GET - list of all events by everyone (if from and to not null; then include range; else just ignore range)
    @GetMapping(value = "/all", produces = "application/json")
    public List<EventResponseDTO> listAll(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        if (from == null && to == null) {
            return eventService.listAll();
        }
        return eventService.listAllBetween(from, to);
    }

    // GET - list of all events by userId (if from and to not null; then include range; else just ignore range)
    @GetMapping(value = "/user/{userId}", produces = "application/json")
    public List<EventResponseDTO> listByUser(
            @RequestHeader("X-User-Id") Long requesterId,
            @PathVariable Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        if (from == null && to == null) {
            return eventService.listByUser(userId);
        }
        return eventService.listByUserBetween(userId, from, to);
    }

    // GET - event by id
    @GetMapping(value = "/{id}", produces = "application/json")
    public EventResponseDTO get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        return eventService.get(userId, id);
    }

    // PUT - update event by id
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public EventResponseDTO update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody @Valid EventUpdateDTO body
    ) {
        return eventService.update(userId, id, body);
    }

    // DELETE - Delete event by id
    @DeleteMapping("/{id}")
    public void delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        eventService.delete(userId, id);
    }

    // Health/test check to make sure that the file is being read
    @GetMapping("/ping")
    public String ping() {
        return "EventController is alive!";
    }

    // Map common exceptions

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> onValidation(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "validation_error",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(EventService.ResourceNotFound.class)
    public ResponseEntity<Map<String, Object>> onNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "not_found",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> onBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "bad_request",
                "message", e.getMessage()
        ));
    }
}