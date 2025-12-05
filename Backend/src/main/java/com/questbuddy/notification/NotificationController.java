package com.questbuddy.notification;

import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.dto.NotificationResponseDTO;
import com.questbuddy.notification.NotificationMapper;
import com.questbuddy.notification.NotificationService;
import com.questbuddy.notification.Notification;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
@RequestMapping("/api/v7/notifications")
@Tag(
        name = "Notifications",
        description = "Create, list, mark as read, and delete user notifications."
)
public class NotificationController {

    private final NotificationService notifications;
    private final NotificationMapper mapper;

    public NotificationController(NotificationService notifications, NotificationMapper mapper) {
        this.notifications = notifications;
        this.mapper = mapper;
    }

    /**
     * PUT - update read status
     */
    @PutMapping("/{id}/read")
    @Operation(
            summary = "Mark a notification as read",
            description = "Marks a notification as read for the given user (X-User-Id). "
                    + "Only the recipient can mark their notifications as read."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification marked as read successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificationResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request (invalid ID or other bad input)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to modify this notification (wrong owner)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found",
                    content = @Content
            )
    })
    public ResponseEntity<NotificationResponseDTO> markRead(
            @Parameter(
                    description = "ID of the notification to mark as read",
                    example = "123"
            )
            @PathVariable Long id,
            @Parameter(
                    description = "ID of the user marking the notification as read",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me
    ) {
        try {
            Notification n = notifications.markRead(id, me);
            return ResponseEntity.ok(mapper.toResponse(n));
        } catch (IllegalArgumentException ex) {
            if ("notification_not_found".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
            }
            return ResponseEntity.badRequest().build(); // 400 for other bad input
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 wrong owner
        }
    }

    /**
     * POST - create a notification
     *
     * Create a notification for a recipient (IDs in body).
     */
    @PostMapping
    @Operation(
            summary = "Create a notification",
            description = "Creates a notification for the given recipient and optional trip/event/task references. "
                    + "The referenced entities are resolved and validated by the service."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Notification created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificationResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request (recipient or referenced entities not found, or invalid payload)",
                    content = @Content
            )
    })
    public ResponseEntity<NotificationResponseDTO> create(
            @Valid @RequestBody NotificationCreateDTO dto) {
        try {
            Notification n = notifications.create(dto); // service resolves/validates refs
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(n));
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if ("recipient_not_found".equals(msg) || "trip_not_found".equals(msg)
                    || "event_not_found".equals(msg) || "task_not_found".equals(msg)
                    || "bad_reference".equals(msg)) {
                return ResponseEntity.badRequest().body(null); // 400
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /** Simple health check for Notifications controller */
    @GetMapping("/ping")
    @Operation(
            summary = "Notification service health check",
            description = "Simple endpoint to verify that NotificationController v7 is alive and reachable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Controller is alive"
            )
    })
    public String ping() {
        return "NotificationController v7 is alive!";
    }

    /**
     *  GET - List notifications (filter can be toggled)
     */
    @GetMapping
    @Operation(
            summary = "List notifications for a user",
            description = "Lists notifications for the user identified by X-User-Id. "
                    + "Optionally filters by unread=true/false."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificationResponseDTO.class))
            )
    })
    public ResponseEntity<List<NotificationResponseDTO>> list(
            @Parameter(
                    description = "ID of the user whose notifications are being listed",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "If true, only unread notifications are returned; if false, only read; if null, all.",
                    example = "true",
                    required = false
            )
            @RequestParam(name = "unread", required = false) Boolean unread
    ) {
        List<NotificationResponseDTO> out = notifications
                .listForUser(me, unread)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(out);
    }

    /**
     * DELETE - a notification for a user
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a notification for a user",
            description = "Deletes a notification for the user identified by X-User-Id. "
                    + "Only the recipient can delete their own notifications."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Notification deleted successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request (invalid ID or other bad input)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to delete this notification",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "ID of the notification to delete",
                    example = "123"
            )
            @PathVariable Long id,
            @Parameter(
                    description = "ID of the user requesting deletion",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me
    ) {
        try {
            notifications.deleteForOwner(id, me); // throws if not owner or not found
            return ResponseEntity.noContent().build();     // 204
        } catch (IllegalArgumentException ex) {
            if ("notification_not_found".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
            }
            return ResponseEntity.badRequest().build(); // 400 (bad ids, etc.)
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 (not recipient's notif)
        }
    }
}
