package com.questbuddy.trip;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("api/v6/trips")
@Tag(
        name = "Trips",
        description = "Endpoints for creating, listing, updating, fetching, and deleting trips."
)
public class TripController {

    private final TripService trips;

    // constructor
    public TripController(TripService trips) {
        this.trips = trips;
    }

    // PUT - update an event by id
    @PutMapping("/{id}")
    @Operation(
            summary = "Update a trip",
            description = "Updates an existing trip owned by the user identified by X-User-Id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trip updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found or not owned by this user",
                    content = @Content
            )
    })
    public TripResponseDTO update(
            @Parameter(
                    description = "ID of the user who owns the trip",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(
                    description = "ID of the trip to update",
                    example = "10"
            )
            @PathVariable Long id,
            @RequestBody @Valid TripUpdateDTO body
    ) {
        return trips.update(userId, id, body);
    }

    // POST - Create an event
    @PostMapping
    @Operation(
            summary = "Create a trip",
            description = "Creates a new trip owned by the user identified by X-User-Id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trip created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripResponseDTO.class))
            )
    })
    public TripResponseDTO create(
            @Parameter(
                    description = "ID of the user creating the trip",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid TripCreateDTO body
    ) {
        return trips.create(userId, body);
    }

    // GET - List by owner (userId)
    @GetMapping
    @Operation(
            summary = "List trips for a user",
            description = "Returns all trips owned by the user identified by X-User-Id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trips returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripResponseDTO.class))
            )
    })
    public List<TripResponseDTO> list(
            @Parameter(
                    description = "ID of the user whose trips are being listed",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId) {
        return trips.list(userId);
    }

    // GET - specific event by tripId
    @GetMapping("/{id}")
    @Operation(
            summary = "Get a trip by ID",
            description = "Fetches a single trip by ID, owned by the user identified by X-User-Id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trip returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found or not owned by this user",
                    content = @Content
            )
    })
    public TripResponseDTO get(
            @Parameter(
                    description = "ID of the user who owns the trip",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(
                    description = "ID of the trip to fetch",
                    example = "10"
            )
            @PathVariable Long id
    ) {
        return trips.get(userId, id);
    }

    // DELETE - delete an event by id
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a trip",
            description = "Deletes a trip owned by the user identified by X-User-Id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Trip deleted successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found or not owned by this user",
                    content = @Content
            )
    })
    public void delete(
            @Parameter(
                    description = "ID of the user who owns the trip",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(
                    description = "ID of the trip to delete",
                    example = "10"
            )
            @PathVariable Long id
    ) {
        trips.delete(userId, id);
    }
}
