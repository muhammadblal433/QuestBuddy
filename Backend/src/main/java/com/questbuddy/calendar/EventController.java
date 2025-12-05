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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller for events
 *
 * Note that all CRUD logic was handled in EventService.java (so that code is more streamlined here)
 */
@RestController
@RequestMapping("/api/v4/calendar/events")
@Tag(
        name = "Events",
        description = "Operations for creating, listing, updating, deleting, and viewing calendar events."
)
public class EventController {
    private final EventService eventService;

    public EventController(EventService service) {
        eventService = service;
    }

    // PUT - update event by id
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Update an existing event",
            description = "Updates an existing calendar event by ID for the user identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Event updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation error or bad data)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found",
                    content = @Content
            )
    })
    public EventResponseDTO update(
            @Parameter(
                    description = "ID of the user updating the event",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(
                    description = "ID of the event to update",
                    example = "42"
            )
            @PathVariable Long id,
            @RequestBody @Valid EventUpdateDTO body
    ) {
        return eventService.update(userId, id, body);
    }

    // POST - create event
    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Create a new event",
            description = "Creates a new calendar event for the user identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Event created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation error or bad data)",
                    content = @Content
            )
    })
    public ResponseEntity<EventResponseDTO> create(
            @Parameter(
                    description = "ID of the user creating the event",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid EventCreateDTO body
    ) {
        var out = eventService.create(userId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    // GET - list of all events so far by userId
    @GetMapping(produces = "application/json")
    @Operation(
            summary = "List events for a user",
            description = "Returns all events for the user identified by X-User-Id, optionally filtered by a time range."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Events returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventResponseDTO.class))
            )
    })
    public List<EventResponseDTO> list(
            @Parameter(
                    description = "ID of the user whose events are being listed",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(
                    description = "Optional start of the time range (inclusive), ISO-8601",
                    example = "2025-11-20T00:00:00Z",
                    required = false
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(
                    description = "Optional end of the time range (inclusive), ISO-8601",
                    example = "2025-11-21T00:00:00Z",
                    required = false
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return eventService.list(userId, from, to);
    }

    // GET - list of all events by everyone (if from and to not null; then include range; else just ignore range)
    @GetMapping(value = "/all", produces = "application/json")
    @Operation(
            summary = "List all events",
            description = "Returns all events for all users. If both 'from' and 'to' are provided, filters by that time range."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Events returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventResponseDTO.class))
            )
    })
    public List<EventResponseDTO> listAll(
            @Parameter(
                    description = "ID of the requesting user (for auditing or permissions, if applicable)",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(
                    description = "Optional start of the time range (inclusive), ISO-8601",
                    example = "2025-11-20T00:00:00Z",
                    required = false
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(
                    description = "Optional end of the time range (inclusive), ISO-8601",
                    example = "2025-11-21T00:00:00Z",
                    required = false
            )
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
    @Operation(
            summary = "List events for a specific user",
            description = "Returns events for the specified user. If both 'from' and 'to' are provided, filters by that time range."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Events returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventResponseDTO.class))
            )
    })
    public List<EventResponseDTO> listByUser(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "1"
            )
            @RequestHeader("X-User-Id") Long requesterId,
            @Parameter(
                    description = "ID of the user whose events should be listed",
                    example = "5"
            )
            @PathVariable Long userId,
            @Parameter(
                    description = "Optional start of the time range (inclusive), ISO-8601",
                    example = "2025-11-20T00:00:00Z",
                    required = false
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(
                    description = "Optional end of the time range (inclusive), ISO-8601",
                    example = "2025-11-21T00:00:00Z",
                    required = false
            )
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
    @Operation(
            summary = "Get event by ID",
            description = "Returns a single event by its ID for the user identified by X-User-Id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Event returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found",
                    content = @Content
            )
    })
    public EventResponseDTO get(
            @Parameter(
                    description = "ID of the user requesting the event",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(
                    description = "ID of the event to fetch",
                    example = "42"
            )
            @PathVariable Long id
    ) {
        return eventService.get(userId, id);
    }

    // Health/test check to make sure that the file is being read
    @GetMapping("/ping")
    @Operation(
            summary = "Event service health check",
            description = "Simple endpoint to verify that EventController is alive and reachable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Event controller is alive"
            )
    })
    public String ping() {
        return "EventController is alive!";
    }

    // DELETE - Delete event by id
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete event by ID",
            description = "Deletes an event by its ID for the user identified by X-User-Id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Event deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found",
                    content = @Content
            )
    })
    public void delete(
            @Parameter(
                    description = "ID of the user requesting deletion",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(
                    description = "ID of the event to delete",
                    example = "42"
            )
            @PathVariable Long id
    ) {
        eventService.delete(userId, id);
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
