package com.questbuddy.notification.service;

import com.questbuddy.notification.Notification;
import com.questbuddy.notification.NotificationMapper;
import com.questbuddy.notification.NotificationRepository;
import com.questbuddy.notification.NotificationService;
import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.ws.NotificationBroadcaster;
import com.questbuddy.user.model.User;
import com.questbuddy.user.repository.UserRepository;
import com.questbuddy.trip.Trip;
import com.questbuddy.trip.TripRepository;
import com.questbuddy.calendar.Event;
import com.questbuddy.calendar.EventRepository;
import com.questbuddy.task.model.Task;
import com.questbuddy.task.repository.TaskRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

    @Mock
    NotificationRepository notifications;
    @Mock UserRepository users;
    @Mock TripRepository trips;
    @Mock EventRepository events;
    @Mock TaskRepository tasks;
    @Mock NotificationBroadcaster ws;
    @Mock
    NotificationMapper mapper;

    @InjectMocks
    NotificationService service;

    User user;

    @Before
    public void setup() {
        user = new User();
        user.setId(5L);
    }

    @Test
    public void testCreate_success() {
        NotificationCreateDTO dto =
                new NotificationCreateDTO(5L, "T", "M", null, null, null, null);

        Notification built = new Notification();
        Notification saved = new Notification();
        saved.setId(99L);
        saved.setRecipient(user);

        when(users.findById(5L)).thenReturn(Optional.of(user));
        when(mapper.fromCreate(eq(dto), eq(user), any(), any(), any())).thenReturn(built);
        when(notifications.save(built)).thenReturn(saved);

        Notification out = service.create(dto);

        assertEquals(Long.valueOf(99), out.getId());
        verify(ws).publishNew(saved);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_recipientNotFound() {
        NotificationCreateDTO dto =
                new NotificationCreateDTO(5L, "T", "M", null, null, null, null);

        when(users.findById(5L)).thenReturn(Optional.empty());

        service.create(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_tripNotFound() {
        NotificationCreateDTO dto =
                new NotificationCreateDTO(5L, "T", "M", null, null, 10L, null);

        when(users.findById(5L)).thenReturn(Optional.of(user));
        when(trips.findById(10L)).thenReturn(Optional.empty());

        service.create(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_eventNotFound() {
        NotificationCreateDTO dto =
                new NotificationCreateDTO(5L, "T", "M", null, 33L, null, null);

        when(users.findById(5L)).thenReturn(Optional.of(user));
        when(events.findById(33L)).thenReturn(Optional.empty());

        service.create(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_taskNotFound() {
        NotificationCreateDTO dto =
                new NotificationCreateDTO(5L, "T", "M", null, null, null, 8L);

        when(users.findById(5L)).thenReturn(Optional.of(user));
        when(tasks.findById(8L)).thenReturn(Optional.empty());

        service.create(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_badReference() {
        NotificationCreateDTO dto =
                new NotificationCreateDTO(5L, "T", "M", null, null, null, null);

        Notification built = new Notification();

        when(users.findById(5L)).thenReturn(Optional.of(user));
        when(mapper.fromCreate(any(), any(), any(), any(), any())).thenReturn(built);
        when(notifications.save(built)).thenThrow(new DataIntegrityViolationException("fk"));

        service.create(dto);
    }

    @Test
    public void testListForUser_unreadOnly() {
        Notification n = new Notification();
        when(notifications.findAllByRecipient_IdAndIsReadOrderByCreatedAtDesc(5L, false))
                .thenReturn(List.of(n));

        List<Notification> out = service.listForUser(5L, true);
        assertEquals(1, out.size());
    }

    @Test
    public void testListForUser_all() {
        Notification n = new Notification();
        when(notifications.findAllByRecipient_IdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(n));

        List<Notification> out = service.listForUser(5L, null);
        assertEquals(1, out.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkRead_notFound() {
        when(notifications.findById(9L)).thenReturn(Optional.empty());
        service.markRead(9L, 5L);
    }

    @Test(expected = SecurityException.class)
    public void testMarkRead_wrongOwner() {
        Notification n = new Notification();
        User other = new User();
        other.setId(99L);
        n.setRecipient(other);

        when(notifications.findById(10L)).thenReturn(Optional.of(n));

        service.markRead(10L, 5L);
    }

    @Test
    public void testMarkRead_alreadyRead() {
        Notification n = new Notification();
        n.setRecipient(user);
        n.setRead(true);

        when(notifications.findById(11L)).thenReturn(Optional.of(n));

        Notification out = service.markRead(11L, 5L);

        assertTrue(out.isRead());
        verify(notifications, never()).save(any());
        verify(ws, never()).publishRead(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testMarkRead_newlyRead() {
        Notification n = new Notification();
        n.setId(50L);
        n.setRecipient(user);
        n.setRead(false);

        Notification saved = new Notification();
        saved.setId(50L);
        saved.setRecipient(user);
        saved.setRead(true);

        when(notifications.findById(12L)).thenReturn(Optional.of(n));
        when(notifications.save(n)).thenReturn(saved);
        when(notifications.countByRecipient_IdAndIsReadFalse(5L)).thenReturn(3L);

        Notification out = service.markRead(12L, 5L);

        assertTrue(out.isRead());
        verify(ws).publishRead(5L, 50L, 3L);
    }

    @Test
    public void testDelete_success() {
        Notification n = new Notification();
        n.setRecipient(user);

        when(notifications.findById(15L)).thenReturn(Optional.of(n));

        boolean ok = service.deleteForOwner(15L, 5L);
        assertTrue(ok);
        verify(notifications).delete(n);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelete_notFound() {
        when(notifications.findById(15L)).thenReturn(Optional.empty());
        service.deleteForOwner(15L, 5L);
    }

    @Test(expected = SecurityException.class)
    public void testDelete_forbidden() {
        Notification n = new Notification();
        User other = new User();
        other.setId(99L);
        n.setRecipient(other);

        when(notifications.findById(15L)).thenReturn(Optional.of(n));

        service.deleteForOwner(15L, 5L);
    }
}
