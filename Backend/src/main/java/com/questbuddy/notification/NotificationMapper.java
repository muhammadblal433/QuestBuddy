package com.questbuddy.notification;

import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.dto.NotificationResponseDTO;
import com.questbuddy.notification.Notification;
import com.questbuddy.notification.NotificationType;

import com.questbuddy.trip.Trip;
import org.springframework.stereotype.Component;
import java.time.Instant;

import com.questbuddy.model.User;
import com.questbuddy.calendar.Event;
import com.questbuddy.model.Task;
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
        n.setTitle(dto.title());
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
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                /* eventId */ n.getEvent() != null ? n.getEvent().getId() : null,
                /* tripId  */ n.getTrip()  != null ? n.getTrip().getId()  : null,
                /* taskId  */ n.getTask()  != null ? n.getTask().getTaskId()  : null,
                n.getCreatedAt(),
                n.isRead()
        );
    }
}