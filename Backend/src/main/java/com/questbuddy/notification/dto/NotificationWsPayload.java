package com.questbuddy.notification.dto;

import java.time.Instant;

public record NotificationWsPayload(
        String event, // "NEW" | "READ"
        Long id,
        String title,
        String message,
        String type, // TASK | REMINDER | INVITE | ...
        Long recipientId,
        Instant createdAt,
        boolean isRead,
        Long unreadCount // populated for READ; null for NEW
) {}