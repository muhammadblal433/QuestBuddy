package com.questbuddy.notification;

import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.dto.NotificationResponseDTO;
import com.questbuddy.notification.Notification;
import com.questbuddy.notification.NotificationType;

import org.springframework.stereotype.Component;
import java.time.Instant;

/**
 * This class is mainly for:
 *
 * 1) Create a notification from a CreateDTO
 * 2) Create a ResponseDTO from a notification
 */
@Component
public class NotificationMapper {

    // 1) Create noti from create dto
    public Notification fromCreate(NotificationCreateDTO dto,
                                   User recipient,
                                   Trip trip,
                                   Event event,
                                   Task task) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setMessage(dto.message());
        n.setType(dto.type() == null ? NotificationType.REMINDER : dto.type());
        n.setTrip(trip);
        n.setEvent(event);
        n.setTask(task);
        return n;
    }

    // 2) Create response dto from noti
    public NotificationResponseDTO toResponse(Notification n) {
        return new NotificationResponseDTO(
                n.getId(),
                n.getRecipient() != null ? n.getRecipient().getId() : null,
                n.getMessage(),
                n.getType(),
                n.getTrip()  != null ? n.getTrip().getId()  : null,
                n.getEvent() != null ? n.getEvent().getId() : null,
                n.getTask()  != null ? n.getTask().getId()  : null,
                n.getCreatedAt(),
                n.isRead()
        );
    }
}