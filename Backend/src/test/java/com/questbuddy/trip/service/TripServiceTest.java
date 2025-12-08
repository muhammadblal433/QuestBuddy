package com.questbuddy.trip.service;

import com.questbuddy.trip.*;
import com.questbuddy.trip.TripService.ResourceNotFound;
import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.tripmember.service.TripMembershipService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.data.domain.Sort;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TripServiceTest {

    @Mock
    private TripRepository repo;

    @Mock
    private TripMapper mapper;

    @Mock
    private TripMembershipService membershipService;

    @InjectMocks
    private TripService service;

    private Trip buildTrip(Long id, Long ownerId, LocalDate start, LocalDate end) {
        Trip t = new Trip();
        t.setOwnerId(ownerId);
        t.setStartDate(start);
        t.setEndDate(end);
        try {
            var f = Trip.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(t, id);
        } catch (Exception ignored) {}
        return t;
    }

    @Test
    public void create_success_savesTrip_andSeedsOwner() {
        Long ownerId = 5L;

        TripCreateDTO dto = new TripCreateDTO(
                "Trip",
                "NY",
                "Ames",
                40.0,
                -90.0,
                LocalDate.of(2025,1,1),
                LocalDate.of(2025,1,5)
        );

        Trip entity = buildTrip(null, ownerId, dto.startDate(), dto.endDate());
        Trip saved = buildTrip(100L, ownerId, dto.startDate(), dto.endDate());

        when(mapper.toEntity(ownerId, dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(
                new TripResponseDTO(
                        100L, ownerId, dto.name(), dto.destination(),
                        dto.startLocationName(), dto.startLat(), dto.startLon(),
                        dto.startDate(), dto.endDate(), null, null
                )
        );

        TripResponseDTO result = service.create(ownerId, dto);

        assertEquals(Long.valueOf(100L), result.id());
        verify(repo).save(entity);
        verify(membershipService).seedOwner(ownerId, 100L);
        verify(mapper).toDto(saved);
    }

    @Test(expected = ValidationException.class)
    public void create_throwsWhenEndDateBeforeStartDate() {
        TripCreateDTO dto = new TripCreateDTO(
                "Trip",
                "NY",
                "Test",
                40.0,
                -90.0,
                LocalDate.of(2025,1,5),
                LocalDate.of(2025,1,1)
        );

        service.create(5L, dto);
    }

    @Test
    public void list_combinesOwnedAndMemberTrips_sorted_noDuplicates() {
        Long userId = 7L;

        Trip owned1 = buildTrip(1L, userId, LocalDate.of(2025,1,3), LocalDate.of(2025,1,5));
        Trip owned2 = buildTrip(2L, userId, LocalDate.of(2025,1,1), LocalDate.of(2025,1,2));
        Trip member1 = buildTrip(3L, 99L, LocalDate.of(2025,1,4), LocalDate.of(2025,1,6));

        // duplicate case: member2 is same ID as owned1
        Trip member2 = buildTrip(1L, 99L, LocalDate.of(2025,1,3), LocalDate.of(2025,1,5));

        when(repo.findAllByOwnerId(eq(userId), any(Sort.class)))
                .thenReturn(List.of(owned1, owned2));

        when(repo.findAllForUserAsMemberWithStatus(
                eq(userId), eq(TripMember.Status.ACCEPTED)))
                .thenReturn(List.of(member1, member2));

        when(mapper.toDto(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            return new TripResponseDTO(
                    t.getId(), t.getOwnerId(), "N", "D", null, null, null,
                    t.getStartDate(), t.getEndDate(), null, null
            );
        });

        List<TripResponseDTO> result = service.list(userId);

        assertEquals(3, result.size());
        assertEquals(Long.valueOf(2L), result.get(0).id());
        assertEquals(Long.valueOf(1L), result.get(1).id());
        assertEquals(Long.valueOf(3L), result.get(2).id());

        verify(repo).findAllByOwnerId(eq(userId), any(Sort.class));
        verify(repo).findAllForUserAsMemberWithStatus(userId, TripMember.Status.ACCEPTED);
    }

    @Test
    public void list_emptyOwnedAndMember_returnsEmptyList() {
        Long userId = 9L;

        when(repo.findAllByOwnerId(eq(userId), any(Sort.class)))
                .thenReturn(List.of());

        when(repo.findAllForUserAsMemberWithStatus(
                eq(userId), eq(TripMember.Status.ACCEPTED)))
                .thenReturn(List.of());

        List<TripResponseDTO> result = service.list(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    public void get_success_returnsDto() {
        Trip t = buildTrip(10L, 5L, LocalDate.of(2025,1,1), LocalDate.of(2025,1,4));
        when(repo.findByIdAndOwnerId(10L, 5L)).thenReturn(Optional.of(t));
        when(mapper.toDto(t)).thenReturn(
                new TripResponseDTO(10L, 5L, "Trip", "NY", null, null, null,
                        t.getStartDate(), t.getEndDate(), null, null)
        );

        TripResponseDTO result = service.get(5L, 10L);

        assertEquals(Long.valueOf(10L), result.id());
        verify(repo).findByIdAndOwnerId(10L, 5L);
    }

    @Test(expected = ResourceNotFound.class)
    public void get_notFound_throws() {
        when(repo.findByIdAndOwnerId(10L, 5L)).thenReturn(Optional.empty());
        service.get(5L, 10L);
    }

    @Test
    public void delete_success_callsRepo() {
        when(repo.deleteByIdAndOwnerId(10L, 5L)).thenReturn(1L);

        service.delete(5L, 10L);

        verify(repo).deleteByIdAndOwnerId(10L, 5L);
    }

    @Test(expected = ResourceNotFound.class)
    public void delete_zeroDeleted_throwsNotFound() {
        when(repo.deleteByIdAndOwnerId(10L, 5L)).thenReturn(0L);
        service.delete(5L, 10L);
    }

    @Test
    public void update_success_savesAndReturnsDto() {
        Trip existing = buildTrip(20L, 5L, LocalDate.of(2025,1,1), LocalDate.of(2025,1,5));
        when(repo.findByIdAndOwnerId(20L, 5L)).thenReturn(Optional.of(existing));

        TripUpdateDTO dto = new TripUpdateDTO(
                "NewName",
                "NewDest",
                "NewLoc",
                10.0,
                -10.0,
                LocalDate.of(2025,1,2),
                LocalDate.of(2025,1,6)
        );

        when(repo.save(existing)).thenReturn(existing);

        when(mapper.toDto(existing)).thenReturn(
                new TripResponseDTO(20L, 5L, "NewName", "NewDest",
                        "NewLoc", 10.0, -10.0,
                        LocalDate.of(2025,1,2),
                        LocalDate.of(2025,1,6),
                        null, null)
        );

        TripResponseDTO result = service.update(5L, 20L, dto);

        assertEquals(Long.valueOf(20L), result.id());
        assertEquals("NewName", result.name());
        verify(repo).save(existing);
    }

    @Test(expected = ResourceNotFound.class)
    public void update_notFound_throws() {
        when(repo.findByIdAndOwnerId(20L, 5L)).thenReturn(Optional.empty());
        service.update(5L, 20L, new TripUpdateDTO(null,null,null,null,null,null,null));
    }

    @Test(expected = ValidationException.class)
    public void update_invalidRange_throws() {
        Trip existing = buildTrip(20L, 5L, LocalDate.of(2025,1,4), LocalDate.of(2025,1,5));
        when(repo.findByIdAndOwnerId(20L, 5L)).thenReturn(Optional.of(existing));

        // new end < new start
        TripUpdateDTO dto = new TripUpdateDTO(
                null, null, null, null, null,
                LocalDate.of(2025,1,5),
                LocalDate.of(2025,1,3)
        );

        service.update(5L, 20L, dto);
    }
}
