package com.questbuddy.messages.direct.service;

import com.questbuddy.messages.direct.dto.DirectMessageCreateDTO;
import com.questbuddy.messages.direct.dto.DirectMessageEditDTO;
import com.questbuddy.messages.direct.dto.DirectMessageResponseDTO;
import com.questbuddy.messages.direct.mapper.DirectMessageMapper;
import com.questbuddy.messages.direct.model.DirectMessage;
import com.questbuddy.messages.direct.model.DirectMessageReaction;
import com.questbuddy.messages.direct.repository.DirectMessageReactionRepository;
import com.questbuddy.messages.direct.repository.DirectMessageRepository;
import com.questbuddy.messages.guard.DirectMessagingGate;
import com.questbuddy.messages.ws.ChatBroadcaster;
import com.questbuddy.user.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class DirectMessageServiceTest {

    @Mock DirectMessageRepository messages;
    @Mock DirectMessageReactionRepository reactions;
    @Mock UserRepository users;
    @Mock DirectMessagingGate dmGate;
    @Mock DirectMessageMapper mapper;
    @Mock ChatBroadcaster chat;

    @InjectMocks DirectMessageService service;

    @Before
    public void setup() {}

    private DirectMessageResponseDTO makeDTO() {
        return new DirectMessageResponseDTO(
                1L,  // id
                5L,  // sender
                10L, // recipient
                "x",
                null,
                null,
                Instant.now(),
                Instant.now(),
                false,
                1L,
                emptyMap(),
                emptySet(),
                null,
                false,
                null,
                null,
                null,
                false
        );
    }

    @Test
    public void testPost_success_newMessage() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessageCreateDTO dto = new DirectMessageCreateDTO(
                "hello",
                null,
                null,
                "abc123",
                Instant.now()
        );

        when(messages.findBySenderIdAndClientMessageId(me, "abc123"))
                .thenReturn(Optional.empty());

        DirectMessage entity = new DirectMessage();
        entity.setId(1L);
        entity.setSenderId(me);
        entity.setRecipientId(peer);
        entity.setContent("hello");
        entity.setClientMessageId("abc123");
        entity.setSavedAt(Instant.now());
        entity.setVersion(1L);

        when(mapper.toEntity(eq(me), eq(peer), eq(dto), any()))
                .thenReturn(entity);

        when(messages.save(entity)).thenReturn(entity);

        DirectMessageResponseDTO resp = makeDTO();
        when(mapper.toResponse(eq(entity), eq(emptyMap()), eq(emptySet())))
                .thenReturn(resp);

        DirectMessageResponseDTO out = service.post(me, peer, dto);

        assertNotNull(out);
        verify(chat).dmMessageNew(eq(me), eq(peer), eq(resp));
    }

    @Test(expected = ResponseStatusException.class)
    public void testPost_rejectsEmptyContent() {
        Long me = 5L, peer = 10L;
        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessageCreateDTO dto = new DirectMessageCreateDTO(
                "", null, null, "id1", Instant.now()
        );

        service.post(me, peer, dto);
    }

    @Test
    public void testEdit_success() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessageEditDTO in = new DirectMessageEditDTO("updated", 1L);

        DirectMessage msg = new DirectMessage();
        msg.setId(77L);
        msg.setSenderId(me);
        msg.setRecipientId(peer);
        msg.setVersion(1L);
        msg.setContent("old");

        when(messages.findByIdInConversation(77L, me, peer))
                .thenReturn(Optional.of(msg));

        doAnswer(a -> {
            msg.setContent("updated");
            msg.setEdited(true);
            msg.setVersion(2L);
            return null;
        }).when(mapper).applyEdit(eq(msg), eq(in), any());

        when(messages.save(msg)).thenReturn(msg);

        Map<String,Integer> counts = emptyMap();
        Set<String> mine = emptySet();

        DirectMessageResponseDTO dto = makeDTO();
        when(mapper.toResponse(msg, counts, mine)).thenReturn(dto);

        DirectMessageResponseDTO out = service.edit(me, peer, 77L, in);

        assertNotNull(out);
        verify(chat).dmEdit(me, peer, dto);
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_wrongSender_forbidden() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessageEditDTO in = new DirectMessageEditDTO("updated", 1L);

        DirectMessage msg = new DirectMessage();
        msg.setId(77L);
        msg.setSenderId(99L);
        msg.setRecipientId(peer);
        msg.setVersion(1L);

        when(messages.findByIdInConversation(77L, me, peer))
                .thenReturn(Optional.of(msg));

        service.edit(me, peer, 77L, in);
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_versionMismatch_conflict() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessageEditDTO in = new DirectMessageEditDTO("updated", 99L);

        DirectMessage msg = new DirectMessage();
        msg.setId(77L);
        msg.setSenderId(me);
        msg.setRecipientId(peer);
        msg.setVersion(1L);

        when(messages.findByIdInConversation(77L, me, peer))
                .thenReturn(Optional.of(msg));

        service.edit(me, peer, 77L, in);
    }

    @Test
    public void testDelete_success() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessage msg = new DirectMessage();
        msg.setId(55L);
        msg.setSenderId(me);
        msg.setRecipientId(peer);
        msg.setDeleted(false);

        when(messages.findByIdInConversation(55L, me, peer))
                .thenReturn(Optional.of(msg));

        when(messages.save(msg)).thenReturn(msg);

        DirectMessageResponseDTO dto = makeDTO();
        when(mapper.toResponse(eq(msg), eq(emptyMap()), eq(emptySet())))
                .thenReturn(dto);

        service.delete(me, peer, 55L);

        verify(chat).dmDelete(me, peer, dto);
    }

    @Test(expected = ResponseStatusException.class)
    public void testDelete_notSender_forbidden() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessage msg = new DirectMessage();
        msg.setId(55L);
        msg.setSenderId(111L);
        msg.setRecipientId(peer);

        when(messages.findByIdInConversation(55L, me, peer))
                .thenReturn(Optional.of(msg));

        service.delete(me, peer, 55L);
    }

    @Test
    public void testToggleReaction_addReaction() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessage msg = new DirectMessage();
        msg.setId(99L);
        msg.setSenderId(peer);
        msg.setRecipientId(me);

        when(messages.findByIdInConversation(99L, me, peer))
                .thenReturn(Optional.of(msg));

        when(reactions.findByMessageIdAndUserIdAndEmoji(99L, me, "🔥"))
                .thenReturn(Optional.empty());

        Map<String,Integer> outMap = Collections.singletonMap("🔥", 1);
        when(reactions.findByMessageId(99L))
                .thenReturn(List.of(makeReaction(99L, me, "🔥")));

        Map<String,Integer> res = service.toggleReaction(me, peer, 99L, "🔥");

        assertEquals(Integer.valueOf(1), res.get("🔥"));
        verify(chat).dmReactionToggle(me, peer, 99L, "🔥");
    }

    @Test
    public void testToggleReaction_removeReaction() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessage msg = new DirectMessage();
        msg.setId(88L);
        msg.setSenderId(peer);
        msg.setRecipientId(me);

        when(messages.findByIdInConversation(88L, me, peer))
                .thenReturn(Optional.of(msg));

        DirectMessageReaction r = makeReaction(88L, me, "👍");
        when(reactions.findByMessageIdAndUserIdAndEmoji(88L, me, "👍"))
                .thenReturn(Optional.of(r));

        when(reactions.findByMessageId(88L))
                .thenReturn(Collections.emptyList());

        Map<String,Integer> res = service.toggleReaction(me, peer, 88L, "👍");

        assertTrue(res.isEmpty());
        verify(chat).dmReactionToggle(me, peer, 88L, "👍");
    }

    @Test
    public void testMarkRead_success() {
        Long me = 10L, peer = 5L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessage msg = new DirectMessage();
        msg.setId(33L);
        msg.setSenderId(peer);
        msg.setRecipientId(me);
        msg.setReadAt(null);

        when(messages.findByIdInConversation(33L, me, peer))
                .thenReturn(Optional.of(msg));

        when(messages.save(msg)).thenReturn(msg);

        service.markRead(me, peer, 33L);

        verify(chat).dmReadReceipt(eq(me), eq(peer), eq(33L), eq(me), isNull());
    }

    @Test(expected = ResponseStatusException.class)
    public void testMarkRead_notRecipient_forbidden() {
        Long me = 5L, peer = 10L;

        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessage msg = new DirectMessage();
        msg.setId(33L);
        msg.setSenderId(me);
        msg.setRecipientId(peer);

        when(messages.findByIdInConversation(33L, me, peer))
                .thenReturn(Optional.of(msg));

        service.markRead(me, peer, 33L);
    }


    @Test
    public void testList_success() {
        Long me = 5L, peer = 10L;
        when(dmGate.canDM(me, peer)).thenReturn(true);
        when(users.existsById(peer)).thenReturn(true);

        DirectMessage m1 = new DirectMessage();
        m1.setId(1L);
        m1.setSenderId(me);
        m1.setRecipientId(peer);

        DirectMessage m2 = new DirectMessage();
        m2.setId(2L);
        m2.setSenderId(peer);
        m2.setRecipientId(me);

        when(messages.pageConversation(eq(me), eq(peer), isNull(), eq(PageRequest.of(0, 50))))
                .thenReturn(List.of(m1, m2));

        when(reactions.findByMessageIdIn(List.of(1L, 2L))).thenReturn(List.of());

        List<DirectMessageResponseDTO> dtos =
                List.of(makeDTO(), makeDTO());

        when(mapper.toResponses(anyList(), anyMap(), anyMap()))
                .thenReturn(dtos);

        List<DirectMessageResponseDTO> out = service.list(me, peer, null, 50);

        assertEquals(2, out.size());
    }

    private DirectMessageReaction makeReaction(Long msgId, Long userId, String emoji) {
        DirectMessageReaction r = new DirectMessageReaction();
        r.setMessageId(msgId);
        r.setUserId(userId);
        r.setEmoji(emoji);
        r.setReactedAt(Instant.now());
        return r;
    }
}
