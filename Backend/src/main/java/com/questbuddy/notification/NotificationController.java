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
@RestController
@RequestMapping("/api/v7/notifications")
public class NotificationController {

    private final NotificationService notifications;
    private final NotificationMapper mapper;

    public NotificationController(NotificationService notifications, NotificationMapper mapper) {
        this.notifications = notifications;
        this.mapper = mapper;
    }

    /**
     * POST - create a notification
     *
     * Create a notification for a recipient (IDs in body).
     * */
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> create(@Valid @RequestBody NotificationCreateDTO dto) {
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

    /**
     *  GET - List notifications (filter can be toggled)

     *  List notifications for a user (pass recipientId in query; unread=true to filter).
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> list(
            @RequestParam Long recipientId,
            @RequestParam(name = "unread", required = false) Boolean unread
    ) {
        List<NotificationResponseDTO> out = notifications
                .listForUser(recipientId, unread)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(out);
    }

    /**
     * PUT - update read status
     *
     * Mark one notification as read (pass recipientId in query for ownership check).
     * */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markRead(
            @PathVariable Long id,
            @RequestParam Long recipientId
    ) {
        try {
            Notification n = notifications.markRead(id, recipientId);
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
     * DELETE - a notification for a user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Long recipientId
    ) {
        try {
            notifications.deleteForOwner(id, recipientId); // throws if not owner or not found
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