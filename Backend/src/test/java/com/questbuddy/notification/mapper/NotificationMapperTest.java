package com.questbuddy.notification.mapper;

import com.questbuddy.notification.Notification;
import com.questbuddy.notification.NotificationMapper;
import com.questbuddy.notification.NotificationType;
import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.dto.NotificationResponseDTO;
import com.questbuddy.user.model.User;
import com.questbuddy.trip.Trip;
import com.questbuddy.calendar.Event;
import com.questbuddy.task.model.Task;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Instant;

public class NotificationMapperTest {

    private final NotificationMapper mapper = new NotificationMapper();

    @Test
    public void testFromCreate_defaultsToReminder() {
        NotificationCreateDTO dto = new NotificationCreateDTO(
                5L, "Hello", "Msg", null, null, null, null
        );

        User user = new User();
        user.setId(5L);

        Notification n = mapper.fromCreate(dto, user, null, null, null);

        assertEquals("Hello", n.getTitle());
        assertEquals("Msg", n.getMessage());
        assertEquals(NotificationType.REMINDER, n.getType());
        assertEquals(user, n.getRecipient());
    }

    @Test
    public void testToResponse_handlesNullReferences() {
        Notification n = new Notification();
        n.setId(1L);
        n.setTitle("T");
        n.setMessage("M");
        n.setCreatedAt(Instant.now());
        n.setType(NotificationType.REMINDER);

        User user = new User();
        user.setId(5L);
        n.setRecipient(user);

        NotificationResponseDTO dto = mapper.toResponse(n);

        assertNull(dto.eventId());
        assertNull(dto.taskId());
        assertNull(dto.tripId());
    }
}

