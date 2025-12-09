package com.questbuddy.events.trip.mapper;

import com.questbuddy.events.trip.dto.TripEventCreateDTO;
import com.questbuddy.events.trip.dto.TripEventEditDTO;
import com.questbuddy.events.trip.dto.TripEventResponseDTO;
import com.questbuddy.events.trip.model.TripEvent;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class TripEventMapperTest {

    @Test
    public void testFromCreate_basic() {
        Instant now = Instant.now();

        TripEventCreateDTO dto = new TripEventCreateDTO(
                "Dinner",
                now,
                now.plusSeconds(3600),
                "NYC",
                "notes",
                2,
                Arrays.asList("a","b")
        );

        TripEvent e = TripEventMapper.fromCreate(10L, 5L, dto);

        assertEquals(Long.valueOf(10), e.getTripId());
        assertEquals(Long.valueOf(5), e.getCreatorId());
        assertEquals("Dinner", e.getName());
        assertEquals("NYC", e.getLocation());
        assertEquals("notes", e.getNotes());
        assertEquals(Integer.valueOf(2), e.getPosition());
        assertEquals(now, e.getStartsAt());
        assertEquals(now.plusSeconds(3600), e.getEndsAt());
        assertNotNull(e.getAttachmentRefsJson());
    }

    @Test
    public void testFromCreate_nullAttachments() {
        Instant now = Instant.now();

        TripEventCreateDTO dto = new TripEventCreateDTO(
                "Stay", now, now, "Hotel", null, null, null
        );

        TripEvent e = TripEventMapper.fromCreate(2L, 3L, dto);

        assertNull(e.getAttachmentRefsJson());
    }

    @Test
    public void testToDTO_basic() {
        TripEvent e = new TripEvent();
        e.setId(55L);
        e.setTripId(10L);
        e.setCreatorId(5L);
        e.setName("Hiking");
        e.setStartsAt(Instant.parse("2025-01-01T00:00:00Z"));
        e.setEndsAt(Instant.parse("2025-01-01T02:00:00Z"));
        e.setLocation("Mount");
        e.setNotes("Bring water");
        e.setPosition(3);
        e.setAttachmentRefsJson("[\"x\",\"y\"]");
        e.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        e.setUpdatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        e.setDeletedAt(null);

        TripEventResponseDTO out = TripEventMapper.toDTO(e);

        assertEquals(Long.valueOf(55), out.id());
        assertEquals(Long.valueOf(10), out.tripId());
        assertEquals("Hiking", out.name());
        assertEquals(Arrays.asList("x","y"), out.attachmentRefs());
        assertFalse(out.deleted());
    }

    @Test
    public void testToDTO_deleted() {
        TripEvent e = new TripEvent();
        e.setId(1L);
        e.setTripId(2L);
        e.setCreatorId(3L);
        e.setName("abc");
        e.setStartsAt(Instant.now());
        e.setEndsAt(Instant.now());
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        e.setDeletedAt(Instant.now()); // <= deleted flag true

        TripEventResponseDTO out = TripEventMapper.toDTO(e);
        assertTrue(out.deleted());
    }

    @Test
    public void testApplyEdit_updatesOnlyNonNull() {
        TripEvent e = new TripEvent();
        e.setName("old");
        e.setLocation("loc");
        e.setNotes("notes");
        e.setPosition(1);
        e.setStartsAt(Instant.parse("2025-01-01T00:00:00Z"));
        e.setEndsAt(Instant.parse("2025-01-01T01:00:00Z"));
        e.setAttachmentRefsJson(null);

        TripEventEditDTO edit = new TripEventEditDTO(
                "new",        // update
                null,         // stays old
                Instant.parse("2025-01-01T03:00:00Z"), // update
                null,         // stays old
                "updated note",
                9,
                Arrays.asList("z")
        );

        TripEventMapper.applyEdit(e, edit);

        assertEquals("new", e.getName());
        assertEquals("loc", e.getLocation());
        assertEquals("updated note", e.getNotes());
        assertEquals(Integer.valueOf(9), e.getPosition());
        assertEquals(Instant.parse("2025-01-01T03:00:00Z"), e.getEndsAt());
        assertNotNull(e.getAttachmentRefsJson());
    }

    @Test
    public void testToJson_nullOrEmpty() {
        assertNull(TripEventMapper.toJson(null));
        assertNull(TripEventMapper.toJson(Collections.emptyList()));
    }

    @Test
    public void testToJson_valid() {
        String json = TripEventMapper.toJson(Arrays.asList("a","b"));
        assertTrue(json.contains("a"));
        assertTrue(json.contains("b"));
    }

    @Test
    public void testFromJson_nullOrBlank() {
        assertEquals(Collections.emptyList(), TripEventMapper.fromJson(null));
        assertEquals(Collections.emptyList(), TripEventMapper.fromJson(""));
        assertEquals(Collections.emptyList(), TripEventMapper.fromJson("   "));
    }

    @Test
    public void testFromJson_valid() {
        List<String> list = TripEventMapper.fromJson("[\"x\",\"y\"]");
        assertEquals(Arrays.asList("x","y"), list);
    }
}
