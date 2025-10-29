package com.questbuddy.notification.dto;

import com.questbuddy.notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creation of a notification - every param is used except creation time and read status.
 *
 * @param recipientId - id of user that recieves notification
 * @param title - of noti.
 * @param message- desc. of noti.
 * @param type - of noti. (ref to noti. class)
 * @param eventId - if related to event, the event id
 * @param tripId - if related to trip, the trip id
 * @param taskId - if related to task, the task id
 */
public record NotificationCreateDTO(
    @NotNull Long recipientId,
    @NotBlank @Size(max = 140) String title,
    @NotBlank @Size(max = 2000) String message,
    NotificationType type, // optional; defaults to INFO if null
    Long eventId,
    Long tripId,
    Long taskId
) {}