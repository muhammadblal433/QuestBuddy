package com.questbuddy.messages.trip.controller;

import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.trip.service.TripMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v9/trips")
@Tag(
        name = "Trip Messages",
        description = "Endpoints for trip group chat: listing, sending, editing, deleting, and reacting to messages."
)
public class TripMessageController {

    private final TripMessageService service;

    public TripMessageController(TripMessageService service) {
        this.service = service;
    }

    // PUT - edit a message
    @PutMapping("/{tripId}/messages/{messageId}")
    @Operation(
            summary = "Edit a trip message",
            description = "Edits the content of an existing message in a trip group chat. "
                    + "The caller is identified by the X-User-Id header and is typically only allowed to edit their own messages."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message edited successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripMessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation error)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to edit this message",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or message not found",
                    content = @Content
            )
    })
    public TripMessageResponseDTO edit(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip that owns the message",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "ID of the message to edit",
                    example = "1001"
            )
            @PathVariable Long messageId,
            @RequestBody @Valid TripMessageEditDTO in) {
        return service.edit(me, tripId, messageId, in);
    }

    // POST - send a message to a trip gc
    @PostMapping("/{tripId}/messages")
    @Operation(
            summary = "Send a message to a trip chat",
            description = "Sends a new message to the group chat for the specified trip. "
                    + "The caller is identified by the X-User-Id header and must be a member of the trip."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message sent successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripMessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation error)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to send messages to this trip",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content
            )
    })
    public TripMessageResponseDTO post(
            @Parameter(
                    description = "ID of the user sending the message",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip whose chat is being used",
                    example = "10"
            )
            @PathVariable Long tripId,
            @RequestBody @Valid TripMessageCreateDTO in) {
        return service.post(me, tripId, in);
    }

    // POST - react to a message
    @PostMapping("/{tripId}/messages/{messageId}/reactions")
    @Operation(
            summary = "Toggle a reaction on a trip message",
            description = "Adds or removes a reaction (emoji) by the authenticated user to a specific trip message. "
                    + "If the emoji is already present from this user, it is removed; otherwise it is added."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reaction toggled successfully; returns updated reaction counts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or message not found",
                    content = @Content
            )
    })
    public Map<String, Integer> react(
            @Parameter(
                    description = "ID of the user reacting",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip that owns the message",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "ID of the message to react to",
                    example = "1001"
            )
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body) {
        String emoji = null;
        if (body != null) {
            emoji = body.get("emoji");
        }
        return service.toggleReaction(me, tripId, messageId, emoji);
    }

    // GET - List of "limit" messages before "beforeId" for a trip
    @GetMapping("/{tripId}/messages")
    @Operation(
            summary = "List messages in a trip chat",
            description = "Lists up to 'limit' messages for a given trip, optionally before a given message ID "
                    + "(useful for pagination). The caller is identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Messages returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripMessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found or user not allowed to view this chat",
                    content = @Content
            )
    })
    public List<TripMessageResponseDTO> list(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip whose chat messages are being listed",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "If provided, only messages with ID less than this will be returned",
                    example = "1001",
                    required = false
            )
            @RequestParam(required = false) Long beforeId,
            @Parameter(
                    description = "Maximum number of messages to return",
                    example = "50"
            )
            @RequestParam(defaultValue = "50") int limit) {
        return service.list(me, tripId, beforeId, limit);
    }

    @GetMapping("/messages/ping")
    @Operation(
            summary = "Trip messages service health check",
            description = "Simple endpoint to verify that TripMessageController is alive."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Controller is alive"
            )
    })
    public String ping() {
        return "ok";
    }

    // DELETE - delete a message
    @DeleteMapping("/{tripId}/messages/{messageId}")
    @Operation(
            summary = "Delete a trip message",
            description = "Deletes a message in a trip group chat, using an optimistic locking version field. "
                    + "The caller is identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message deleted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TripMessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Version conflict when attempting to delete the message",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or message not found",
                    content = @Content
            )
    })
    public TripMessageResponseDTO delete(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip that owns the message",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "ID of the message to delete",
                    example = "1001"
            )
            @PathVariable Long messageId,
            @Parameter(
                    description = "Expected version of the message for optimistic locking",
                    example = "1"
            )
            @RequestParam("version") Long version) {
        return service.delete(me, tripId, messageId, version);
    }

    // DELETE - delete a reaction
    @DeleteMapping("/{tripId}/messages/{messageId}/reactions/{emoji}")
    @Operation(
            summary = "Remove a reaction from a trip message",
            description = "Removes the specified emoji reaction from a message for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reaction removed; returns updated reaction counts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or message not found",
                    content = @Content
            )
    })
    public Map<String, Integer> unreact(
            @Parameter(
                    description = "ID of the user removing the reaction",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip that owns the message",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "ID of the message to remove the reaction from",
                    example = "1001"
            )
            @PathVariable Long messageId,
            @Parameter(
                    description = "Emoji reaction to remove",
                    example = "👍"
            )
            @PathVariable String emoji) {
        return service.toggleReaction(me, tripId, messageId, emoji);
    }
}
