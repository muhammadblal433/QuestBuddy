package com.questbuddy.events.trip.service;

import com.questbuddy.events.trip.dto.TripEventCreateDTO;
import com.questbuddy.events.trip.dto.TripEventEditDTO;
import com.questbuddy.events.trip.dto.TripEventResponseDTO;
import com.questbuddy.events.trip.model.TripEvent;
import com.questbuddy.events.trip.repository.TripEventRepository;
import com.questbuddy.tripmember.service.TripMembershipService;
import com.questbuddy.notification.NotificationService;
import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.NotificationType;
import com.questbuddy.user.model.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.data.domain.*;

import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TripEventServiceTest {

    @Mock
    private TripEventRepository repo;

    @Mock
    private TripMembershipService membership;

    @Mock
    private NotificationService notifications;

    @InjectMocks
    private TripEventService service;

    private TripEvent event;

    @Before
    public void setUp() {
        event = new TripEvent();
        event.setId(55L);
        event.setTripId(10L);
        event.setCreatorId(5L);
        event.setName("Old Name");
        event.setStartsAt(Instant.parse("2025-01-01T00:00:00Z"));
        event.setEndsAt(Instant.parse("2025-01-01T01:00:00Z"));
    }

    private TripEventCreateDTO newCreateDTO() {
        return new TripEventCreateDTO(
                "Dinner",
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T01:00:00Z"),
                "Place",
                "Bring snacks",
                1,
                Arrays.asList("a.png", "b.jpg")
        );
    }

    @Test
    public void testList_noRange() {
        Page<TripEvent> pg = new PageImpl<>(List.of(event));
        when(repo.findByTripIdAndDeletedAtIsNullOrderByStartsAtAscIdAsc(eq(10L), any()))
                .thenReturn(pg);

        Page<TripEventResponseDTO> out = service.list(5L, 10L, null, null, 0, 50);

        assertEquals(1, out.getTotalElements());
        verify(membership).ensureMember(5L, 10L);
    }

    @Test
    public void testList_withRange() {
        Page<TripEvent> pg = new PageImpl<>(List.of(event));
        when(repo.findByTripIdAndDeletedAtIsNullAndStartsAtBetweenOrderByStartsAtAscIdAsc(
                eq(10L), any(), any(), any()))
                .thenReturn(pg);

        Instant f = Instant.parse("2024-01-01T00:00:00Z");
        Instant t = Instant.parse("2030-01-01T00:00:00Z");

        Page<TripEventResponseDTO> out = service.list(5L, 10L, f, t, 1, 20);

        assertEquals(1, out.getTotalElements());
        verify(membership).ensureMember(5L, 10L);
    }

    @Test
    public void testCreate_success() {
        when(repo.save(any(TripEvent.class))).thenAnswer(inv -> {
            TripEvent e = inv.getArgument(0);
            e.setId(99L);
            return e;
        });

        User u = new User();
        u.setId(7L);
        when(membership.listAccepted(5L, 10L)).thenReturn(List.of(u));

        TripEventResponseDTO out = service.create(5L, 10L, newCreateDTO());

        assertEquals(Long.valueOf(99L), out.id());
        verify(repo).save(any(TripEvent.class));
        verify(notifications).create(any(NotificationCreateDTO.class));
        verify(membership).ensureMember(5L, 10L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testCreate_invalidDate() {
        TripEventCreateDTO bad =
                new TripEventCreateDTO("Bad",
                        Instant.parse("2025-01-02T00:00:00Z"),
                        Instant.parse("2025-01-01T00:00:00Z"),
                        null, null, null, null);

        service.create(5L, 10L, bad);
    }

    @Test
    public void testEdit_success_creator() {
        when(repo.findByIdAndTripIdAndDeletedAtIsNull(55L, 10L))
                .thenReturn(Optional.of(event));

        User u = new User();
        u.setId(7L);
        when(membership.listAccepted(5L, 10L)).thenReturn(List.of(u));

        TripEventEditDTO edit =
                new TripEventEditDTO("New Name",
                        Instant.parse("2025-01-01T00:10:00Z"),
                        Instant.parse("2025-01-01T01:00:00Z"),
                        "New Loc",
                        "Some notes",
                        2,
                        null);

        TripEventResponseDTO out = service.edit(5L, 10L, 55L, edit);

        assertEquals("New Name", out.name());
        verify(membership).ensureMember(5L, 10L);
        verify(repo).findByIdAndTripIdAndDeletedAtIsNull(55L, 10L);
    }

    @Test
    public void testEdit_success_owner() {
        event.setCreatorId(5L);
        when(repo.findByIdAndTripIdAndDeletedAtIsNull(55L, 10L))
                .thenReturn(Optional.of(event));
        when(membership.isOwner(9L, 10L)).thenReturn(true);

        TripEventEditDTO edit = new TripEventEditDTO("X", null, null, null, null, null, null);

        TripEventResponseDTO res = service.edit(9L, 10L, 55L, edit);

        assertEquals("X", res.name());
        verify(membership).ensureMember(9L, 10L);
        verify(membership).isOwner(9L, 10L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_notOwnerNotCreator() {
        event.setCreatorId(5L);
        when(repo.findByIdAndTripIdAndDeletedAtIsNull(55L, 10L))
                .thenReturn(Optional.of(event));
        when(membership.isOwner(9L, 10L)).thenReturn(false);

        service.edit(9L, 10L, 55L, new TripEventEditDTO("x", null, null, null, null, null, null));
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_eventNotFound() {
        when(repo.findByIdAndTripIdAndDeletedAtIsNull(55L, 10L))
                .thenReturn(Optional.empty());

        service.edit(5L, 10L, 55L, new TripEventEditDTO(null, null, null, null, null, null, null));
    }

    @Test
    public void testDelete_success_creator() {
        when(repo.findByIdAndTripIdAndDeletedAtIsNull(55L, 10L))
                .thenReturn(Optional.of(event));

        User u = new User();
        u.setId(7L);
        when(membership.listAccepted(5L, 10L)).thenReturn(List.of(u));

        service.delete(5L, 10L, 55L);

        assertNotNull(event.getDeletedAt());
        verify(notifications).create(any(NotificationCreateDTO.class));
        verify(membership).ensureMember(5L, 10L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testDelete_forbidden() {
        event.setCreatorId(5L);
        when(repo.findByIdAndTripIdAndDeletedAtIsNull(55L, 10L))
                .thenReturn(Optional.of(event));
        when(membership.isOwner(9L, 10L)).thenReturn(false);

        service.delete(9L, 10L, 55L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testDelete_eventNotFound() {
        when(repo.findByIdAndTripIdAndDeletedAtIsNull(55L, 10L))
                .thenReturn(Optional.empty());

        service.delete(5L, 10L, 55L);
    }
}
