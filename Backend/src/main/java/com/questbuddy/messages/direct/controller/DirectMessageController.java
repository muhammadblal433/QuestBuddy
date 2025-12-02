package com.questbuddy.messages.direct.controller;

import com.questbuddy.messages.direct.dto.DirectMessageCreateDTO;
import com.questbuddy.messages.direct.dto.DirectMessageEditDTO;
import com.questbuddy.messages.direct.dto.DirectMessageResponseDTO;
import com.questbuddy.messages.direct.service.DirectMessageService;
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
@RequestMapping("/api/v10/direct")
@Tag(
        name = "Direct Messages",
        description = "Endpoints for sending, editing, listing, reacting to, and marking direct messages as read."
)
public class DirectMessageController {

    private final DirectMessageService service;

    public DirectMessageController(DirectMessageService service) {
        this.service = service;
    }

    // PUT - edit a direct message
    @PutMapping("/{peerId}/messages/{messageId}")
    @Operation(
            summary = "Edit a direct message",
            description = "Edits the content of an existing direct message in a conversation with the given peer. "
                    + "The caller is identified by the X-User-Id header and can typically only edit their own messages."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message edited successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DirectMessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation error)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User not allowed to edit this message",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation or message not found",
                    content = @Content
            )
    })
    public DirectMessageResponseDTO edit(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the conversation peer",
                    example = "25"
            )
            @PathVariable Long peerId,
            @Parameter(
                    description = "ID of the message to edit",
                    example = "1001"
            )
            @PathVariable Long messageId,
            @RequestBody @Valid DirectMessageEditDTO in) {
        return service.edit(me, peerId, messageId, in);
    }

    // POST - send a direct message
    @PostMapping("/{peerId}/messages")
    @Operation(
            summary = "Send a direct message",
            description = "Sends a new direct message from the authenticated user (X-User-Id) "
                    + "to the specified peer."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message sent successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DirectMessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation error)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User not allowed to send messages to this peer",
                    content = @Content
            )
    })
    public DirectMessageResponseDTO post(
            @Parameter(
                    description = "ID of the user sending the message",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the conversation peer",
                    example = "25"
            )
            @PathVariable Long peerId,
            @RequestBody @Valid DirectMessageCreateDTO in) {
        return service.post(me, peerId, in);
    }

    // POST - toggle a reaction
    @PostMapping("/{peerId}/messages/{messageId}/reactions")
    @Operation(
            summary = "Toggle a reaction on a message",
            description = "Adds or removes a reaction (emoji) by the authenticated user to a specific message."
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
                    description = "Conversation or message not found",
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
                    description = "ID of the conversation peer",
                    example = "25"
            )
            @PathVariable Long peerId,
            @Parameter(
                    description = "ID of the message to react to",
                    example = "1001"
            )
            @PathVariable Long messageId,
            @Parameter(
                    description = "Emoji reaction to toggle",
                    example = "👍"
            )
            @RequestParam String emoji) {
        return service.toggleReaction(me, peerId, messageId, emoji);
    }

    // POST - mark as read (recipient only)
    @PostMapping("/{peerId}/messages/{messageId}/read")
    @Operation(
            summary = "Mark a direct message as read",
            description = "Marks the given message as read by the authenticated user, typically the recipient."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message marked as read",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation or message not found",
                    content = @Content
            )
    })
    public void markRead(
            @Parameter(
                    description = "ID of the user marking the message as read",
                    example = "25"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the conversation peer",
                    example = "5"
            )
            @PathVariable Long peerId,
            @Parameter(
                    description = "ID of the message to mark as read",
                    example = "1001"
            )
            @PathVariable Long messageId) {
        service.markRead(me, peerId, messageId);
    }

    // GET - List of "limit" messages before "beforeId" for conversation
    @GetMapping("/{peerId}/messages")
    @Operation(
            summary = "List direct messages in a conversation",
            description = "Lists up to 'limit' messages in a conversation with the specified peer, "
                    + "optionally before a given message ID (for pagination)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Messages returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DirectMessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation not found or user not allowed",
                    content = @Content
            )
    })
    public List<DirectMessageResponseDTO> list(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the conversation peer",
                    example = "25"
            )
            @PathVariable Long peerId,
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
        return service.list(me, peerId, beforeId, limit);
    }

    @GetMapping("/messages/ping")
    @Operation(
            summary = "Direct messages service health check",
            description = "Simple endpoint to verify that DirectMessageController is alive."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Controller is alive"
            )
    })
    public String ping() { return "ok"; }

    // DELETE - delete a direct message
    @DeleteMapping("/{peerId}/messages/{messageId}")
    @Operation(
            summary = "Delete a direct message",
            description = "Deletes a direct message in a conversation with the specified peer. "
                    + "The caller is identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation or message not found",
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
                    description = "ID of the conversation peer",
                    example = "25"
            )
            @PathVariable Long peerId,
            @Parameter(
                    description = "ID of the message to delete",
                    example = "1001"
            )
            @PathVariable Long messageId) {
        service.delete(me, peerId, messageId);
    }

    // DELETE - delete a reaction (specific emoji)
    @DeleteMapping("/{peerId}/messages/{messageId}/reactions/{emoji}")
    @Operation(
            summary = "Remove a reaction from a message",
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
                    description = "Conversation or message not found",
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
                    description = "ID of the conversation peer",
                    example = "25"
            )
            @PathVariable Long peerId,
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
        return service.toggleReaction(me, peerId, messageId, emoji);
    }
}
