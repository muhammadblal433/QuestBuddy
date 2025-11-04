package com.questbuddy.notification;

import com.questbuddy.notification.dto.NotificationWsPayload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationWsService {

    private final SimpMessagingTemplate ws;

    public NotificationWsService(SimpMessagingTemplate ws) {
        this.ws = ws;
    }

    public void publishNew(Notification n) {
        var payload = new NotificationWsPayload(
                "NEW",
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType() == null ? null : n.getType().name(),
                n.getRecipient() == null ? null : n.getRecipient().getId(),
                n.getCreatedAt(),
                n.isRead(),
                null
        );
        ws.convertAndSend("/topic/notifications/" + n.getRecipient().getId(), payload);
    }

    public void publishRead(Long recipientId, Long notificationId, long unreadCount) {
        var payload = new NotificationWsPayload(
                "READ",
                notificationId,
                null, null, null,
                recipientId,
                null,
                true,
                unreadCount
        );
        ws.convertAndSend("/topic/notifications/" + recipientId, payload);
    }
}