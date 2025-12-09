package com.questbuddy.calendar.mapper;

import com.questbuddy.calendar.Event;
import com.questbuddy.calendar.EventMapper;
import com.questbuddy.calendar.dto.EventCreateDTO;
import com.questbuddy.calendar.dto.EventUpdateDTO;
import com.questbuddy.calendar.dto.EventResponseDTO;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class EventMapperTest {

    private final EventMapper mapper = new EventMapper();

    @Test
    public void testToEntity() {
        EventCreateDTO dto = new EventCreateDTO(
                "Title",
                "Desc",
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-02T00:00:00Z"),
                "Loc",
                true
        );

        Event e = mapper.toEntity(5L, dto);

        assertEquals(Long.valueOf(5), e.getUserId());
        assertEquals("Title", e.getTitle());
        assertEquals("Desc", e.getDescription());
        assertEquals("Loc", e.getLocation());
        assertTrue(e.isAllDay());
    }

    @Test
    public void testApplyUpdate_allFields() {
        Event e = new Event();
        e.setTitle("Old");
        e.setDescription("OldDesc");
        e.setStartAt(Instant.now());
        e.setEndAt(Instant.now());
        e.setLocation("OldLoc");
        e.setAllDay(false);

        EventUpdateDTO dto = new EventUpdateDTO(
                "NewTitle",
                "NewDesc",
                Instant.parse("2025-02-01T00:00:00Z"),
                Instant.parse("2025-02-02T00:00:00Z"),
                "NewLoc",
                true
        );

        mapper.applyUpdate(e, dto);

        assertEquals("NewTitle", e.getTitle());
        assertEquals("NewDesc", e.getDescription());
        assertEquals("NewLoc", e.getLocation());
        assertTrue(e.isAllDay());
    }

    @Test
    public void testApplyUpdate_nullFields_doNotChange() {
        Event e = new Event();
        e.setTitle("T");
        e.setDescription("D");
        e.setStartAt(Instant.parse("2025-01-01T00:00:00Z"));
        e.setEndAt(Instant.parse("2025-01-02T00:00:00Z"));
        e.setLocation("L");
        e.setAllDay(false);

        EventUpdateDTO dto = new EventUpdateDTO(null, null, null, null, null, null);
        mapper.applyUpdate(e, dto);

        assertEquals("T", e.getTitle());
        assertEquals("D", e.getDescription());
        assertEquals("L", e.getLocation());
        assertFalse(e.isAllDay());
    }

    @Test
    public void testToDto() {
        Event e = new Event();
        e.setTitle("A");
        e.setDescription("B");
        Instant s = Instant.parse("2025-03-01T00:00:00Z");
        Instant en = Instant.parse("2025-03-02T00:00:00Z");
        e.setStartAt(s);
        e.setEndAt(en);
        e.setLocation("X");
        e.setAllDay(true);
        e.setCreatedAt(s);
        e.setUpdatedAt(en);

        EventResponseDTO dto = mapper.toDto(e);

        assertEquals("A", dto.title());
        assertEquals("B", dto.description());
        assertEquals("X", dto.location());
        assertTrue(dto.allDay());
    }
}
