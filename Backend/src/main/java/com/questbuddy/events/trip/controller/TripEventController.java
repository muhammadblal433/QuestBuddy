package com.questbuddy.events.trip.controller;

import com.questbuddy.events.trip.dto.TripEventCreateDTO;
import com.questbuddy.events.trip.dto.TripEventEditDTO;
import com.questbuddy.events.trip.dto.TripEventResponseDTO;
import com.questbuddy.events.trip.service.TripEventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v13/trips/{tripId}/events")
public class TripEventController {

    private final TripEventService service;

    public TripEventController(TripEventService service) {
        this.service = service;
    }

    @GetMapping
    public Page<TripEventResponseDTO> list(@RequestHeader("X-User-Id") Long me,
                                           @PathVariable Long tripId,
                                           @RequestParam(required = false) Instant from,
                                           @RequestParam(required = false) Instant to,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        return service.list(me, tripId, from, to, page, size);
    }

    @PostMapping
    public TripEventResponseDTO create(@RequestHeader("X-User-Id") Long me,
                                       @PathVariable Long tripId,
                                       @RequestBody @Valid TripEventCreateDTO in) {
        return service.create(me, tripId, in);
    }

    @PatchMapping("/{eventId}")
    public TripEventResponseDTO edit(@RequestHeader("X-User-Id") Long me,
                                     @PathVariable Long tripId,
                                     @PathVariable Long eventId,
                                     @RequestBody @Valid TripEventEditDTO in) {
        return service.edit(me, tripId, eventId, in);
    }

    @DeleteMapping("/{eventId}")
    public void delete(@RequestHeader("X-User-Id") Long me,
                       @PathVariable Long tripId,
                       @PathVariable Long eventId) {
        service.delete(me, tripId, eventId);
    }
}
