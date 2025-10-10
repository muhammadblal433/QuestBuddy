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
@RequestMapping("/api/v4/calendar/events")
public class EventController {
    private final EventService eventService;


    public EventController(EventService service) {
        eventService = service;
    }


    // POST - create event
    @PostMapping
    public EventResponseDTO create(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid EventCreateDTO body
    ) {
        return eventService.create(userId, body);
    }


    // GET - list of all events so far by userId
    @GetMapping
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
    @GetMapping("/all")
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
    @GetMapping("/user/{userId}")
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
    @GetMapping("/{id}")
    public EventResponseDTO get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        return eventService.get(userId, id);
    }


    // PUT - update event by id
    @PutMapping("/{id}")
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
}
