package com.questbuddy.calendar.service;

import com.questbuddy.calendar.*;
import com.questbuddy.calendar.dto.*;
import com.questbuddy.notification.NotificationService;
import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.NotificationType;
import jakarta.validation.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.time.Instant;
import java.util.*;

import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {

    @Mock
    private EventRepository repo;

    @Mock
    private EventMapper mapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventService service;

    private static final Instant S = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant E = Instant.parse("2025-01-02T00:00:00Z");

    // ---------- create ----------

    @Test
    public void testCreate_success() {
        EventCreateDTO dto = new EventCreateDTO("T", "D", S, E, "L", false);
        Event e = new Event();
        e.setTitle("T"); e.setStartAt(S); e.setEndAt(E);
        Event saved = new Event();
        saved.setId(10L);
        saved.setTitle("T");
        saved.setStartAt(S);
        saved.setEndAt(E);

        when(mapper.toEntity(5L, dto)).thenReturn(e);
        when(repo.save(e)).thenReturn(saved);
        when(notificationService.create(any())).thenReturn(null);

        when(mapper.toDto(saved)).thenReturn(new EventResponseDTO(
                10L,"T","D",S,E,"L",false,S,E
        ));

        EventResponseDTO out = service.create(5L, dto);
        assertEquals(Long.valueOf(10L), out.id());
    }

    @Test(expected = ValidationException.class)
    public void testCreate_invalidRange() {
        EventCreateDTO dto = new EventCreateDTO(
                "T","D",
                E,  // start
                S,  // end BEFORE start
                "L",
                false
        );
        service.create(5L, dto);
    }

    @Test
    public void testCreate_notificationFailsButStillSucceeds() {
        EventCreateDTO dto = new EventCreateDTO("T", "D", S, E, "L", false);
        Event e = new Event();
        Event saved = new Event();
        saved.setId(99L);
        saved.setTitle("T");
        saved.setStartAt(S);
        saved.setEndAt(E);

        when(mapper.toEntity(5L, dto)).thenReturn(e);
        when(repo.save(e)).thenReturn(saved);
        doThrow(new RuntimeException("fail")).when(notificationService).create(any());
        when(mapper.toDto(saved)).thenReturn(
                new EventResponseDTO(99L,"T","D",S,E,"L",false,S,E)
        );

        EventResponseDTO out = service.create(5L, dto);
        assertEquals(Long.valueOf(99L), out.id());
    }

    // ---------- list ----------

    @Test
    public void testList_noRange() {
        Event e = new Event();
        e.setId(1L);
        when(repo.findAllByUserId(eq(5L), any())).thenReturn(List.of(e));
        when(mapper.toDto(e)).thenReturn(new EventResponseDTO(
                1L,"T","D",S,E,"L",false,S,E
        ));

        var list = service.list(5L, null, null);
        assertEquals(1, list.size());
    }

    @Test(expected = ValidationException.class)
    public void testList_partialRangeInvalid() {
        service.list(5L, S, null);
    }

    @Test
    public void testList_validRange() {
        Event e = new Event();
        e.setId(1L);
        when(repo.findAllByUserIdAndStartAtBetween(eq(5L), eq(S), eq(E), any()))
                .thenReturn(List.of(e));
        when(mapper.toDto(e)).thenReturn(new EventResponseDTO(
                1L,"T","D",S,E,"L",false,S,E
        ));

        var list = service.list(5L, S, E);
        assertEquals(1, list.size());
    }

    // ---------- listAll ----------

    @Test
    public void testListAll() {
        Event e = new Event();
        when(repo.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(e));
        when(mapper.toDto(e)).thenReturn(new EventResponseDTO(
                1L,"T","D",S,E,"L",false,S,E
        ));
        assertEquals(1, service.listAll().size());
    }

    // ---------- listAllBetween ----------

    @Test
    public void testListAllBetween_success() {
        Event e = new Event();
        when(repo.findAllByStartAtBetween(eq(S), eq(E), any()))
                .thenReturn(List.of(e));
        when(mapper.toDto(e)).thenReturn(
                new EventResponseDTO(1L,"T","D",S,E,"L",false,S,E)
        );

        assertEquals(1, service.listAllBetween(S,E).size());
    }

    @Test(expected = ValidationException.class)
    public void testListAllBetween_invalidRange() {
        service.listAllBetween(E, S);
    }

    // ---------- listByUser ----------

    @Test
    public void testListByUser() {
        Event e = new Event();
        when(repo.findAllByUserId(eq(5L), any())).thenReturn(List.of(e));
        when(mapper.toDto(e)).thenReturn(
                new EventResponseDTO(1L,"T","D",S,E,"L",false,S,E)
        );
        assertEquals(1, service.listByUser(5L).size());
    }

    // ---------- listByUserBetween ----------

    @Test
    public void testListByUserBetween_success() {
        Event e = new Event();
        when(repo.findAllByUserIdAndStartAtBetween(eq(5L), eq(S), eq(E), any()))
                .thenReturn(List.of(e));
        when(mapper.toDto(e)).thenReturn(
                new EventResponseDTO(1L,"T","D",S,E,"L",false,S,E)
        );

        assertEquals(1, service.listByUserBetween(5L,S,E).size());
    }

    @Test(expected = ValidationException.class)
    public void testListByUserBetween_invalidRange() {
        service.listByUserBetween(5L, E, S);
    }

    // ---------- get ----------

    @Test
    public void testGet_found() {
        Event e = new Event();
        e.setId(10L);
        when(repo.findByIdAndUserId(10L,5L)).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(
                new EventResponseDTO(10L,"T","D",S,E,"L",false,S,E)
        );

        assertEquals(Long.valueOf(10L), service.get(5L,10L).id());
    }

    @Test(expected = EventService.ResourceNotFound.class)
    public void testGet_notFound() {
        when(repo.findByIdAndUserId(10L,5L)).thenReturn(Optional.empty());
        service.get(5L,10L);
    }

    // ---------- update ----------

    @Test
    public void testUpdate_success() {
        Event e = new Event();
        e.setId(12L);
        e.setStartAt(S);
        e.setEndAt(E);

        EventUpdateDTO dto = new EventUpdateDTO("N",null,null,null,null,null);
        Event saved = new Event();
        saved.setId(12L);
        saved.setStartAt(S);
        saved.setEndAt(E);
        saved.setTitle("N");

        when(repo.findByIdAndUserId(12L,5L)).thenReturn(Optional.of(e));
        when(notificationService.create(any())).thenReturn(null);

        when(repo.save(e)).thenReturn(saved);
        when(notificationService.create(any())).thenReturn(null);

        when(mapper.toDto(saved)).thenReturn(
                new EventResponseDTO(12L,"N","D",S,E,"L",false,S,E)
        );

        assertEquals(Long.valueOf(12L), service.update(5L,12L,dto).id());
    }

    @Test(expected = EventService.ResourceNotFound.class)
    public void testUpdate_notFound() {
        when(repo.findByIdAndUserId(12L,5L)).thenReturn(Optional.empty());
        service.update(5L,12L,new EventUpdateDTO(null,null,null,null,null,null));
    }

    @Test(expected = ValidationException.class)
    public void testUpdate_invalidRange() {
        Event e = new Event();
        e.setStartAt(S);
        e.setEndAt(E);

        when(repo.findByIdAndUserId(12L, 5L)).thenReturn(Optional.of(e));

        // DTO provides invalid range
        EventUpdateDTO dto = new EventUpdateDTO(null, null, E, S, null, null);
        service.update(5L, 12L, dto);
    }


    @Test
    public void testUpdate_notificationFails() {
        Event e = new Event();
        e.setStartAt(S);
        e.setEndAt(E);
        Event saved = new Event();
        saved.setId(12L);

        when(repo.findByIdAndUserId(12L,5L)).thenReturn(Optional.of(e));
        when(repo.save(e)).thenReturn(saved);
        doThrow(new RuntimeException()).when(notificationService).create(any());
        when(mapper.toDto(saved)).thenReturn(
                new EventResponseDTO(12L,"T","D",S,E,"L",false,S,E)
        );

        var out = service.update(5L,12L,new EventUpdateDTO(null,null,null,null,null,null));
        assertEquals(Long.valueOf(12L), out.id());
    }

    @Test
    public void testDelete_success() {
        Event e = new Event();
        e.setTitle("X");

        when(repo.findByIdAndUserId(55L,5L)).thenReturn(Optional.of(e));
        when(notificationService.create(any())).thenReturn(null);

        when(repo.deleteByIdAndUserId(55L,5L)).thenReturn(1L);

        service.delete(5L,55L);
    }

    @Test(expected = EventService.ResourceNotFound.class)
    public void testDelete_notFoundBeforeDeletion() {
        when(repo.findByIdAndUserId(55L,5L)).thenReturn(Optional.empty());
        service.delete(5L,55L);
    }

    @Test(expected = EventService.ResourceNotFound.class)
    public void testDelete_notFoundAfterDeletion() {
        Event e = new Event();
        e.setTitle("X");
        when(repo.findByIdAndUserId(55L,5L)).thenReturn(Optional.of(e));
        when(repo.deleteByIdAndUserId(55L,5L)).thenReturn(0L);
        service.delete(5L,55L);
    }
}

