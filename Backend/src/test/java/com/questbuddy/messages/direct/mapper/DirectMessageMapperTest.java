package com.questbuddy.messages.direct.mapper;

import com.questbuddy.messages.direct.dto.*;
import com.questbuddy.messages.direct.model.DirectMessage;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;

public class DirectMessageMapperTest {

    private final DirectMessageMapper mapper = new DirectMessageMapper();


    @Test
    public void testToEntity_basic() {
        Instant now = Instant.now();
        DirectMessageCreateDTO dto = new DirectMessageCreateDTO(
                "hello world", 5L, 7L, "abc123", now
        );

        DirectMessage msg = mapper.toEntity(1L, 2L, dto, now);

        assertEquals(Long.valueOf(1L), msg.getSenderId());
        assertEquals(Long.valueOf(2L), msg.getRecipientId());
        assertEquals("hello world", msg.getContent());
        assertEquals(Long.valueOf(5L), msg.getParentMessageId());
        assertEquals(Long.valueOf(7L), msg.getForwardFromMessageId());
        assertEquals("abc123", msg.getClientMessageId());
        assertEquals(now, msg.getSentAt());
        assertEquals(now, msg.getSavedAt());
        assertFalse(msg.isEdited());
        assertEquals(Long.valueOf(1L), msg.getVersion());
        assertFalse(msg.isDeleted());
    }

    @Test
    public void testApplyEdit_basic() {
        DirectMessage m = new DirectMessage();
        m.setContent("old");
        m.setEdited(false);
        m.setVersion(2L);

        DirectMessageEditDTO editDTO = new DirectMessageEditDTO("new content", 2L);
        Instant editedAt = Instant.now();

        mapper.applyEdit(m, editDTO, editedAt);

        assertEquals("new content", m.getContent());
        assertTrue(m.isEdited());
        assertEquals(editedAt, m.getEditedAt());
        assertEquals(Long.valueOf(3L), m.getVersion());
    }

    @Test
    public void testApplyEdit_versionNull() {
        DirectMessage m = new DirectMessage();
        m.setVersion(null);

        DirectMessageEditDTO editDTO = new DirectMessageEditDTO("x", 1L);
        Instant editedAt = Instant.now();

        mapper.applyEdit(m, editDTO, editedAt);

        assertEquals(Long.valueOf(1L), m.getVersion());
    }


    @Test
    public void testToResponse_allFields() {
        DirectMessage m = new DirectMessage();
        m.setId(10L);
        m.setSenderId(1L);
        m.setRecipientId(2L);
        m.setContent("content");
        m.setParentMessageId(5L);
        m.setForwardFromMessageId(7L);
        m.setSentAt(Instant.now());
        m.setSavedAt(Instant.now());
        m.setEdited(true);
        m.setVersion(3L);
        m.setEditedAt(Instant.now());
        m.setDeleted(false);
        m.setDeletedAt(null);
        m.setDeletedBy(null);
        m.setReadAt(Instant.now());
        m.setReadByUserId(2L); // equals recipientId → readByRecipient = true

        Map<String, Integer> counts = new HashMap<>();
        counts.put("👍", 2);
        Set<String> mine = new HashSet<>(Collections.singletonList("👍"));

        DirectMessageResponseDTO dto = mapper.toResponse(m, counts, mine);

        assertEquals(Long.valueOf(10L), dto.id());
        assertEquals(Long.valueOf(1L), dto.senderId());
        assertEquals(Long.valueOf(2L), dto.recipientId());
        assertEquals("content", dto.content());
        assertEquals(Long.valueOf(5L), dto.parentMessageId());
        assertEquals(Long.valueOf(7L), dto.forwardFromMessageId());
        assertTrue(dto.edited());
        assertEquals(Long.valueOf(3L), dto.version());
        assertEquals(1, dto.reactions().size());
        assertEquals(Integer.valueOf(2), dto.reactions().get("👍"));
        assertTrue(dto.myReactions().contains("👍"));
        assertTrue(dto.readByRecipient());
    }

    @Test
    public void testToResponse_emptyMaps() {
        DirectMessage m = new DirectMessage();
        m.setId(10L);
        m.setSenderId(1L);
        m.setRecipientId(2L);

        DirectMessageResponseDTO dto = mapper.toResponse(m, null, null);

        assertNotNull(dto);
        assertTrue(dto.reactions().isEmpty());
        assertTrue(dto.myReactions().isEmpty());
    }


    @Test
    public void testToResponses_emptyList() {
        List<DirectMessageResponseDTO> out = mapper.toResponses(Collections.emptyList(), null, null);
        assertTrue(out.isEmpty());
    }

    @Test
    public void testToResponses_batch() {
        DirectMessage m1 = new DirectMessage();
        m1.setId(10L);
        m1.setSenderId(1L);
        m1.setRecipientId(2L);

        DirectMessage m2 = new DirectMessage();
        m2.setId(11L);
        m2.setSenderId(1L);
        m2.setRecipientId(2L);

        Map<Long, Map<String, Integer>> counts = new HashMap<>();
        counts.put(10L, Map.of("😀", 1));

        Map<Long, Set<String>> mine = new HashMap<>();
        mine.put(10L, Set.of("😀"));

        List<DirectMessageResponseDTO> out = mapper.toResponses(
                Arrays.asList(m1, m2), counts, mine);

        assertEquals(2, out.size());
        assertEquals(Long.valueOf(10L), out.get(0).id());
        assertEquals(1, out.get(0).reactions().size());
        assertEquals(Long.valueOf(11L), out.get(1).id());
    }
}
