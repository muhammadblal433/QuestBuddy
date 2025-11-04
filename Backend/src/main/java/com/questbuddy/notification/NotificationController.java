package com.questbuddy.notification;

import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.dto.NotificationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification REST API (v7), user-id in path (no X-User-Id).
 */
@RestController
@RequestMapping("/api/v7")
public class NotificationController {

    private final NotificationService notifications;
    private final NotificationMapper mapper;

    public NotificationController(NotificationService notifications, NotificationMapper mapper) {
        this.notifications = notifications;
        this.mapper = mapper;
    }

    // Health check
    @GetMapping("/notifications/ping")
    public String ping() {
        return "NotificationController v7 is alive!";
    }

    /**
     * POST - create a notification
     *
     * Create a notification for a recipient (recipientId from path).
     * If recipientId is present in the body, it is ignored in favor of the path value.
     */
    @PostMapping("/users/{recipientId}/notifications")
    public ResponseEntity<NotificationResponseDTO> create(
            @PathVariable Long recipientId,
            @Valid @RequestBody NotificationCreateDTO body
    ) {
        try {
            NotificationCreateDTO dto = new NotificationCreateDTO(
                    recipientId,                      // override from path
                    body.title(),
                    body.message(),
                    body.type(),
                    body.tripId(),
                    body.eventId(),
                    body.taskId()
            );
            Notification n = notifications.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(n));
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if ("recipient_not_found".equals(msg) || "trip_not_found".equals(msg)
                    || "event_not_found".equals(msg) || "task_not_found".equals(msg)
                    || "bad_reference".equals(msg)) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * GET - List notifications for a user (optionally unread-only), newest first.
     *
     * @param userId     recipient user id
     * @param unread     set unread=true to filter only unread
     */
    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<List<NotificationResponseDTO>> list(
            @PathVariable Long userId,
            @RequestParam(name = "unread", required = false) Boolean unread
    ) {
        List<NotificationResponseDTO> out = notifications
                .listForUser(userId, unread)
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(out);
    }

    /**
     * PUT - update read status (enforces ownership via userId).
     */
    @PutMapping("/users/{userId}/notifications/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markRead(
            @PathVariable Long userId,
            @PathVariable Long id
    ) {
        try {
            Notification n = notifications.markRead(id, userId);
            return ResponseEntity.ok(mapper.toResponse(n));
        } catch (IllegalArgumentException ex) {
            if ("notification_not_found".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * DELETE - delete one notification owned by {userId}.
     */
    @DeleteMapping("/users/{userId}/notifications/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long id
    ) {
        try {
            notifications.deleteForOwner(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            if ("notification_not_found".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}