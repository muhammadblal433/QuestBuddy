// src/main/java/com/questbuddy/notification/ws/NotificationBroadcaster.java
package com.questbuddy.notification.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.notification.Notification;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds compact JSON payloads and pushes them to the connected user via NotificationEndpoint.
 */
@Component
public class NotificationBroadcaster {

    private final ObjectMapper om;

    public NotificationBroadcaster(ObjectMapper om) {
        this.om = om;
    }

    /** Push a NEW notification event to the recipient. */
    public void publishNew(Notification n) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("kind", "NEW");
        payload.put("id", n.getId());
        payload.put("title", n.getTitle());
        payload.put("message", n.getMessage());
        payload.put("type", n.getType().name());
        payload.put("recipientId", n.getRecipient().getId());
        payload.put("createdAt", n.getCreatedAt());
        payload.put("read", n.isRead());

        send(n.getRecipient().getId(), payload);
    }

    /** Push a READ event + updated unreadCount for the badge. */
    public void publishRead(Long recipientId, Long notificationId, long unreadCount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("kind", "READ");
        payload.put("id", notificationId);
        payload.put("recipientId", recipientId);
        payload.put("read", true);
        payload.put("unreadCount", unreadCount);

        send(recipientId, payload);
    }

    private void send(Long userId, Map<String, Object> payload) {
        try {
            String json = om.writeValueAsString(payload);
            NotificationEndpoint.sendToUser(userId, json);
        } catch (Exception ignored) {}
    }
}