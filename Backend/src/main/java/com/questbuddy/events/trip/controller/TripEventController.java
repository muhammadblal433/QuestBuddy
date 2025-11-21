package com.questbuddy.events.trip.controller;

import com.questbuddy.events.trip.dto.TripEventCreateDTO;
import com.questbuddy.events.trip.dto.TripEventEditDTO;
import com.questbuddy.events.trip.dto.TripEventResponseDTO;
import com.questbuddy.events.trip.service.TripEventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v13/trips/{tripId}/events")
@Tag(
        name = "Trip Events",
        description = "CRUD operations for events attached to a specific trip, including listing with optional date range and pagination."
)
public class TripEventController {

    private final TripEventService service;

    public TripEventController(TripEventService service) {
        this.service = service;
    }

    @PutMapping("/{eventId}")
    @Operation(
            summary = "Edit a trip event",
            description = "Edits an existing event within a trip. The caller must be a member/owner of the trip, "
                    + "identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Event updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripEventResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to edit events for this trip",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or event not found",
                    content = @Content
            )
    })
    public TripEventResponseDTO edit(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip that owns the event",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "ID of the event to edit",
                    example = "3"
            )
            @PathVariable Long eventId,
            @RequestBody @Valid TripEventEditDTO in) {
        return service.edit(me, tripId, eventId, in);
    }

    @PostMapping
    @Operation(
            summary = "Create a new trip event",
            description = "Creates a new event within a specific trip. The caller must be a member/owner of the trip, "
                    + "identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Event created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripEventResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation error)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to create events for this trip",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content
            )
    })
    public TripEventResponseDTO create(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip that will own the new event",
                    example = "10"
            )
            @PathVariable Long tripId,
            @RequestBody @Valid TripEventCreateDTO in) {
        return service.create(me, tripId, in);
    }

    @GetMapping
    @Operation(
            summary = "List trip events",
            description = "Lists events for a trip, optionally filtered by a time range and paginated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Events returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripEventResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to view events for this trip",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content
            )
    })
    public Page<TripEventResponseDTO> list(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip whose events are being listed",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "Optional start of the time range (inclusive), ISO-8601",
                    example = "2025-11-20T00:00:00Z",
                    required = false
            )
            @RequestParam(required = false) Instant from,
            @Parameter(
                    description = "Optional end of the time range (inclusive), ISO-8601",
                    example = "2025-11-21T00:00:00Z",
                    required = false
            )
            @RequestParam(required = false) Instant to,
            @Parameter(
                    description = "Zero-based page index",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,
            @Parameter(
                    description = "Number of events per page",
                    example = "50"
            )
            @RequestParam(defaultValue = "50") int size) {
        return service.list(me, tripId, from, to, page, size);
    }

    @DeleteMapping("/{eventId}")
    @Operation(
            summary = "Delete a trip event",
            description = "Deletes an event within a trip. The caller must be a member/owner of the trip."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Event deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to delete events for this trip",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or event not found",
                    content = @Content
            )
    })
    public void delete(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip that owns the event",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "ID of the event to delete",
                    example = "3"
            )
            @PathVariable Long eventId) {
        service.delete(me, tripId, eventId);
    }
}
