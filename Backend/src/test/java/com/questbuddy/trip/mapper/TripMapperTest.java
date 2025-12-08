package com.questbuddy.trip.mapper;

import com.questbuddy.trip.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TripMapperTest {

    private final TripMapper mapper = new TripMapper();

    @Test
    public void toEntity_mapsFieldsCorrectly() {
        TripCreateDTO dto = new TripCreateDTO(
                "My Trip",
                "LA",
                "Ames",
                40.1,
                -93.5,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 5)
        );

        Trip t = mapper.toEntity(7L, dto);

        assertEquals(Long.valueOf(7L), t.getOwnerId());
        assertEquals("My Trip", t.getName());
        assertEquals("LA", t.getDestination());
        assertEquals("Ames", t.getStartLocationName());
        assertEquals(Double.valueOf(40.1), t.getStartLat());
        assertEquals(Double.valueOf(-93.5), t.getStartLon());
        assertEquals(LocalDate.of(2025, 1, 1), t.getStartDate());
        assertEquals(LocalDate.of(2025, 1, 5), t.getEndDate());
    }

    @Test
    public void toDto_mapsFieldsCorrectly() {
        Trip t = new Trip();
        t.setOwnerId(5L);
        t.setName("Test Trip");
        t.setDestination("NYC");
        t.setStartLocationName("Ames");
        t.setStartLat(10.0);
        t.setStartLon(-10.0);
        t.setStartDate(LocalDate.of(2024, 1, 1));
        t.setEndDate(LocalDate.of(2024, 1, 5));

        Instant now = Instant.now();
        t.onInsert();

        TripResponseDTO dto = mapper.toDto(t);

        assertEquals(t.getId(), dto.id());
        assertEquals(Long.valueOf(5L), dto.ownerId());
        assertEquals("Test Trip", dto.name());
        assertEquals("NYC", dto.destination());
        assertEquals("Ames", dto.startLocationName());
        assertEquals(Double.valueOf(10.0), dto.startLat());
        assertEquals(Double.valueOf(-10.0), dto.startLon());
        assertEquals(LocalDate.of(2024, 1, 1), dto.startDate());
        assertEquals(LocalDate.of(2024, 1, 5), dto.endDate());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.updatedAt());
    }

    @Test
    public void applyUpdate_updatesOnlyProvidedFields() {
        Trip t = new Trip();
        t.setOwnerId(1L);
        t.setName("Old Name");
        t.setDestination("Old Dest");
        t.setStartLocationName("OldLoc");
        t.setStartLat(1.0);
        t.setStartLon(2.0);
        t.setStartDate(LocalDate.of(2024, 1, 1));
        t.setEndDate(LocalDate.of(2024, 1, 5));

        TripUpdateDTO dto = new TripUpdateDTO(
                "New Name",
                null,
                "NewLoc",
                null,
                -50.0,
                null,
                LocalDate.of(2024, 1, 10)
        );

        mapper.applyUpdate(t, dto);

        assertEquals("New Name", t.getName());
        assertEquals("Old Dest", t.getDestination());
        assertEquals("NewLoc", t.getStartLocationName());
        assertEquals(Double.valueOf(1.0), t.getStartLat());
        assertEquals(Double.valueOf(-50.0), t.getStartLon());
        assertEquals(LocalDate.of(2024, 1, 1), t.getStartDate());
        assertEquals(LocalDate.of(2024, 1, 10), t.getEndDate());
    }
}
