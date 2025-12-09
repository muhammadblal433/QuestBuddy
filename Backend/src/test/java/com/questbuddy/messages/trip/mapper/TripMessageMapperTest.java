package com.questbuddy.messages.trip.mapper;

import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.trip.model.TripMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TripMessageMapperTest {

    private final TripMessageMapper mapper = new TripMessageMapper();


    @Test
    public void testToEntity_withSentAtProvided() {
        Instant sent = Instant.now();
        Instant saved = Instant.now();

        TripMessageCreateDTO dto = new TripMessageCreateDTO(
                "hello",
                2L,
                3L,
                "client123",
                sent
        );

        TripMessage m = mapper.toEntity(dto, 10L, 5L, Instant.EPOCH, saved);

        assertEquals(Long.valueOf(10), m.getTripId());
        assertEquals(Long.valueOf(5), m.getSenderId());
        assertEquals("hello", m.getContent());
        assertEquals(Long.valueOf(2), m.getParentMessageId());
        assertEquals(Long.valueOf(3), m.getForwardFromMessageId());
        assertEquals("client123", m.getClientMessageId());
        assertEquals(sent, m.getSentAt());
        assertEquals(saved, m.getSavedAt());
        assertFalse(m.isEdited());
        assertFalse(m.isDeleted());
    }

    @Test
    public void testToEntity_withoutSentAt_usesFallback() {
        Instant fallback = Instant.now();
        Instant saved = Instant.now();

        TripMessageCreateDTO dto = new TripMessageCreateDTO(
                "hi",
                null,
                null,
                "client",
                null
        );

        TripMessage m = mapper.toEntity(dto, 10L, 5L, fallback, saved);

        assertEquals("hi", m.getContent());
        assertEquals(fallback, m.getSentAt());
        assertEquals(saved, m.getSavedAt());
    }

    @Test
    public void testToResponse_withReactionsAndMine() {
        TripMessage m = new TripMessage();
        m.setTripId(10L);
        m.setSenderId(5L);
        m.setContent("msg");
        m.setParentMessageId(2L);
        m.setForwardFromMessageId(3L);
        m.setSentAt(Instant.now());
        m.setSavedAt(Instant.now());
        m.setEdited(true);
        m.setEditedAt(Instant.now());
        m.setDeleted(false);
        m.setClientMessageId("x");
        m.setDeletedAt(null);
        m.setDeletedBy(null);
        m.setVersion(7L);

        // set private id field via reflection
        setId(m, 999L);

        Map<String,Integer> counts = new HashMap<>();
        counts.put("👍", 3);
        counts.put("🔥", 1);

        Set<String> mine = new HashSet<>();
        mine.add("👍");

        TripMessageResponseDTO dto = mapper.toResponse(m, counts, mine);

        assertEquals(Long.valueOf(999), dto.id());
        assertEquals(Long.valueOf(10), dto.tripId());
        assertEquals(Long.valueOf(5), dto.senderId());
        assertEquals("msg", dto.content());
        assertEquals(Long.valueOf(2), dto.parentMessageId());
        assertEquals(Long.valueOf(3), dto.forwardFromMessageId());
        assertEquals(Long.valueOf(7), dto.version());
        assertEquals(2, dto.reactions().size());
        assertEquals(1, dto.myReactions().size());
    }

    @Test
    public void testToResponse_emptyReactionsAndMine() {
        TripMessage m = new TripMessage();
        setId(m, 1L);
        m.setTripId(10L);
        m.setSenderId(5L);
        m.setContent("x");
        m.setSentAt(Instant.now());
        m.setSavedAt(Instant.now());
        m.setClientMessageId("a");
        m.setVersion(1L);

        TripMessageResponseDTO dto = mapper.toResponse(m, null, null);

        assertTrue(dto.reactions().isEmpty());
        assertTrue(dto.myReactions().isEmpty());
    }

    @Test
    public void testApplyEdit() {
        TripMessage m = new TripMessage();
        m.setContent("old");
        m.setEdited(false);

        Instant now = Instant.now();
        TripMessageEditDTO in = new TripMessageEditDTO("new", 1L);

        mapper.applyEdit(m, in, now);

        assertEquals("new", m.getContent());
        assertTrue(m.isEdited());
        assertEquals(now, m.getEditedAt());
    }

    @Test
    public void testToResponses_empty() {
        List<TripMessageResponseDTO> out = mapper.toResponses(Collections.emptyList(), null, null);
        assertTrue(out.isEmpty());
    }

    @Test
    public void testToResponses_mixed() {
        TripMessage m1 = new TripMessage();
        TripMessage m2 = new TripMessage();

        setId(m1, 10L);
        setId(m2, 20L);

        m1.setTripId(1L);
        m2.setTripId(1L);

        m1.setSenderId(5L);
        m2.setSenderId(6L);

        m1.setContent("a");
        m2.setContent("b");

        m1.setSentAt(Instant.now());
        m2.setSentAt(Instant.now());

        m1.setSavedAt(Instant.now());
        m2.setSavedAt(Instant.now());

        m1.setVersion(1L);
        m2.setVersion(1L);

        List<TripMessage> list = Arrays.asList(m1, m2);

        Map<Long, Map<String,Integer>> counts = new HashMap<>();
        counts.put(10L, Collections.singletonMap("x", 1));

        Map<Long, Set<String>> mine = new HashMap<>();
        mine.put(20L, Collections.singleton("y"));

        List<TripMessageResponseDTO> out = mapper.toResponses(list, counts, mine);

        assertEquals(2, out.size());

        TripMessageResponseDTO d1 = out.get(0);
        TripMessageResponseDTO d2 = out.get(1);

        assertEquals(Long.valueOf(10), d1.id());
        assertEquals(1, d1.reactions().size());
        assertTrue(d1.myReactions().isEmpty());

        assertEquals(Long.valueOf(20), d2.id());
        assertEquals(1, d2.myReactions().size());
        assertTrue(d2.reactions().isEmpty());
    }

    private void setId(TripMessage m, Long id) {
        try {
            java.lang.reflect.Field f = TripMessage.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(m, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
