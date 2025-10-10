package com.questbuddy.trip;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v6/trips")
public class TripController {

    private final TripService trips;

    // constructor
    public TripController(TripService trips) {
        this.trips = trips;
    }

    // POST - Create an event
    @PostMapping
    public TripResponseDTO create(
            @RequestHeader("X-User-Id") Long userId, @RequestBody @Valid TripCreateDTO body
    ) {
        return trips.create(userId, body);
    }

    // GET - List by owner (userId)
    @GetMapping
    public List<TripResponseDTO> list(@RequestHeader("X-User-Id") Long userId) {
        return trips.list(userId);
    }

    // GET - specific event by tripId
    @GetMapping("/{id}")
    public TripResponseDTO get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        return trips.get(userId, id);
    }

    // PUT - update an event by id
    @PutMapping("/{id}")
    public TripResponseDTO update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody @Valid TripUpdateDTO body
    ) {
        return trips.update(userId, id, body);
    }

    // DELETE - delete an event by id
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        trips.delete(userId, id);
    }
}