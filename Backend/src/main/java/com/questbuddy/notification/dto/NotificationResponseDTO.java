package com.questbuddy.notification.dto;

import com.questbuddy.notification.NotificationType;
import java.time.Instant;

/**
 *
 * @param id - of noti.
 * @param recipientId - id of user that recieves notification
 * @param title - of noti.
 * @param message- desc. of noti.
 * @param type - of noti. (ref to noti. class)
 * @param eventId - if related to event, the event id
 * @param tripId - if related to trip, the trip id
 * @param taskId - if related to task, the task id
 * @param createdAt - creation time of noti.
 * @param isRead - read status of noti.
 */
public record NotificationResponseDTO(
        Long id,
        Long recipientId,
        String title,
        String message,
        NotificationType type,
        Long eventId,
        Long tripId,
        Long taskId,
        Instant createdAt,
        boolean isRead
) {}