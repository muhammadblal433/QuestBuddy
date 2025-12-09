package com.questbuddy.messages.trip.service;

import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.trip.mapper.TripMessageMapper;
import com.questbuddy.messages.trip.model.MessageReaction;
import com.questbuddy.messages.trip.model.TripMessage;
import com.questbuddy.messages.trip.model.TripReadProgress;
import com.questbuddy.messages.trip.repository.MessageReactionRepository;
import com.questbuddy.messages.trip.repository.TripMessageRepository;
import com.questbuddy.messages.trip.repository.TripReadProgressRepository;
import com.questbuddy.messages.guard.TripMembershipGate;
import com.questbuddy.messages.ws.ChatBroadcaster;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TripMessageServiceTest {

    @Mock TripMessageRepository repo;
    @Mock MessageReactionRepository rxRepo;
    @Mock TripMembershipGate gate;
    @Mock TripMessageMapper mapper;
    @Mock ChatBroadcaster broadcaster;
    @Mock TripReadProgressRepository readRepo;

    @InjectMocks TripMessageService service;

    @Test(expected = ResponseStatusException.class)
    public void testPost_notMember() {
        when(gate.isMember(10L, 5L)).thenReturn(false);
        service.post(5L, 10L, new TripMessageCreateDTO("x", null, null, "c", Instant.now()));
    }

    @Test(expected = ResponseStatusException.class)
    public void testPost_nullBody() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        service.post(5L, 10L, null);
    }

    @Test(expected = ResponseStatusException.class)
    public void testPost_badContent() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        TripMessageCreateDTO dto = new TripMessageCreateDTO(" ", null, null, "c", Instant.now());
        service.post(5L, 10L, dto);
    }

    @Test(expected = ResponseStatusException.class)
    public void testPost_missingClientMessageId() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        TripMessageCreateDTO dto = new TripMessageCreateDTO("hi", null, null, " ", Instant.now());
        service.post(5L, 10L, dto);
    }

    @Test
    public void testPost_idempotentExisting() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        TripMessageCreateDTO dto = new TripMessageCreateDTO("hi", null, null, "client", Instant.now());

        TripMessage existing = new TripMessage();
        setId(existing, 9L);

        when(repo.findBySenderIdAndClientMessageId(5L, "client"))
                .thenReturn(Optional.of(existing));

        when(rxRepo.findByMessageId(9L)).thenReturn(Collections.emptyList());
        when(mapper.toResponse(eq(existing), anyMap(), anySet()))
                .thenReturn(mock(TripMessageResponseDTO.class));

        TripMessageResponseDTO out = service.post(5L, 10L, dto);
        assertNotNull(out);
    }

    @Test(expected = ResponseStatusException.class)
    public void testPost_badParent() {
        when(gate.isMember(10L, 5L)).thenReturn(true);

        TripMessageCreateDTO dto = new TripMessageCreateDTO("hi", 50L, null, "c", Instant.now());

        when(repo.findBySenderIdAndClientMessageId(anyLong(), anyString()))
                .thenReturn(Optional.empty());

        when(repo.findByIdAndTripId(50L, 10L)).thenReturn(Optional.empty());

        service.post(5L, 10L, dto);
    }

    @Test
    public void testPost_success() {
        when(gate.isMember(10L, 5L)).thenReturn(true);

        TripMessageCreateDTO dto = new TripMessageCreateDTO("hi", null, null, "c", Instant.now());

        TripMessage entity = new TripMessage();
        setId(entity, 100L);

        when(repo.findBySenderIdAndClientMessageId(anyLong(), anyString()))
                .thenReturn(Optional.empty());

        when(mapper.toEntity(eq(dto), eq(10L), eq(5L), any(), any()))
                .thenReturn(entity);

        when(repo.save(entity)).thenReturn(entity);

        TripMessageResponseDTO resp = mock(TripMessageResponseDTO.class);
        when(mapper.toResponse(eq(entity), anyMap(), anySet())).thenReturn(resp);

        TripMessageResponseDTO out = service.post(5L, 10L, dto);
        assertNotNull(out);

        verify(broadcaster).tripMessageNew(10L, resp);
    }

    // ------------ list() ------------

    @Test(expected = ResponseStatusException.class)
    public void testList_notMember() {
        when(gate.isMember(10L, 5L)).thenReturn(false);
        service.list(5L, 10L, null, 50);
    }

    @Test
    public void testList_emptyPage() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        when(repo.findTop50ByTripIdOrderByIdDesc(10L))
                .thenReturn(Collections.emptyList());

        List<TripMessageResponseDTO> result = service.list(5L, 10L, null, 50);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testList_success() {
        when(gate.isMember(10L, 5L)).thenReturn(true);

        TripMessage m1 = new TripMessage();
        TripMessage m2 = new TripMessage();
        setId(m1, 10L);
        setId(m2, 20L);

        List<TripMessage> page = Arrays.asList(m1, m2);

        when(repo.findTop50ByTripIdOrderByIdDesc(10L)).thenReturn(page);
        when(rxRepo.findByMessageIdIn(Arrays.asList(10L,20L)))
                .thenReturn(Collections.emptyList());

        when(mapper.toResponses(eq(page), anyMap(), anyMap()))
                .thenReturn(Arrays.asList(mock(TripMessageResponseDTO.class),
                        mock(TripMessageResponseDTO.class)));

        List<TripMessageResponseDTO> out = service.list(5L, 10L, null, 50);
        assertEquals(2, out.size());
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_notMember() {
        when(gate.isMember(10L, 5L)).thenReturn(false);
        service.edit(5L, 10L, 99L, new TripMessageEditDTO("x",1L));
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_nullBody() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        service.edit(5L, 10L, 99L, null);
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_badContent() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        service.edit(5L, 10L, 99L, new TripMessageEditDTO(" ",1L));
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_missingVersion() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        service.edit(5L, 10L, 99L, new TripMessageEditDTO("abc", null));
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_notFound() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        when(repo.findByIdAndTripId(99L, 10L)).thenReturn(Optional.empty());
        service.edit(5L, 10L, 99L, new TripMessageEditDTO("abc",1L));
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_deleted() {
        when(gate.isMember(10L, 5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m, 99L);
        m.setDeleted(true);

        when(repo.findByIdAndTripId(99L,10L)).thenReturn(Optional.of(m));
        service.edit(5L,10L,99L,new TripMessageEditDTO("abc",1L));
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_notSender() {
        when(gate.isMember(10L, 5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m, 99L);
        m.setDeleted(false);
        m.setSenderId(999L);

        when(repo.findByIdAndTripId(99L,10L))
                .thenReturn(Optional.of(m));

        service.edit(5L,10L,99L,new TripMessageEditDTO("abc",1L));
    }

    @Test(expected = ResponseStatusException.class)
    public void testEdit_versionMismatch() {
        when(gate.isMember(10L, 5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m, 99L);
        m.setDeleted(false);
        m.setSenderId(5L);
        m.setVersion(5L);

        when(repo.findByIdAndTripId(99L,10L))
                .thenReturn(Optional.of(m));

        service.edit(5L,10L,99L,new TripMessageEditDTO("abc", 1L));
    }

    @Test
    public void testEdit_success() {
        when(gate.isMember(10L, 5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m, 99L);
        m.setDeleted(false);
        m.setSenderId(5L);
        m.setVersion(1L);

        when(repo.findByIdAndTripId(99L,10L))
                .thenReturn(Optional.of(m));

        when(repo.save(m)).thenReturn(m);

        when(rxRepo.findByMessageId(99L)).thenReturn(Collections.emptyList());

        TripMessageResponseDTO dto = mock(TripMessageResponseDTO.class);
        when(mapper.toResponse(eq(m), anyMap(), anySet())).thenReturn(dto);

        TripMessageResponseDTO out =
                service.edit(5L,10L,99L,new TripMessageEditDTO("abc",1L));

        assertNotNull(out);
        verify(broadcaster).tripEdit(10L, dto);
    }

    // ------------ delete() ------------

    @Test(expected = ResponseStatusException.class)
    public void testDelete_notMember() {
        when(gate.isMember(10L, 5L)).thenReturn(false);
        service.delete(5L, 10L, 100L, 1L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testDelete_nullVersion() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        service.delete(5L, 10L, 100L, null);
    }

    @Test(expected = ResponseStatusException.class)
    public void testDelete_notFound() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        when(repo.findByIdAndTripId(100L,10L)).thenReturn(Optional.empty());
        service.delete(5L,10L,100L,1L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testDelete_notSender() {
        when(gate.isMember(10L,5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m,100L);
        m.setSenderId(99L);

        when(repo.findByIdAndTripId(100L,10L)).thenReturn(Optional.of(m));

        service.delete(5L,10L,100L,1L);
    }

    @Test
    public void testDelete_alreadyDeleted() {
        when(gate.isMember(10L,5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m,100L);
        m.setSenderId(5L);
        m.setDeleted(true);

        when(repo.findByIdAndTripId(100L,10L)).thenReturn(Optional.of(m));

        TripMessageResponseDTO dto = mock(TripMessageResponseDTO.class);
        when(mapper.toResponse(eq(m), anyMap(), anySet())).thenReturn(dto);

        TripMessageResponseDTO out = service.delete(5L,10L,100L,1L);
        assertNotNull(out);
    }

    @Test(expected = ResponseStatusException.class)
    public void testDelete_versionMismatch() {
        when(gate.isMember(10L,5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m,100L);
        m.setSenderId(5L);
        m.setDeleted(false);
        m.setVersion(5L);

        when(repo.findByIdAndTripId(100L,10L)).thenReturn(Optional.of(m));

        service.delete(5L,10L,100L,1L);
    }

    @Test
    public void testDelete_success() {
        when(gate.isMember(10L,5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m,100L);
        m.setSenderId(5L);
        m.setVersion(1L);
        m.setDeleted(false);

        when(repo.findByIdAndTripId(100L,10L)).thenReturn(Optional.of(m));
        when(repo.save(m)).thenReturn(m);

        when(rxRepo.findByMessageId(100L)).thenReturn(Collections.emptyList());

        TripMessageResponseDTO dto = mock(TripMessageResponseDTO.class);
        when(mapper.toResponse(eq(m), anyMap(), anySet())).thenReturn(dto);

        TripMessageResponseDTO out = service.delete(5L,10L,100L,1L);
        assertNotNull(out);

        verify(broadcaster).tripDelete(10L, dto);
    }

    @Test(expected = ResponseStatusException.class)
    public void testToggle_notMember() {
        when(gate.isMember(10L, 5L)).thenReturn(false);
        service.toggleReaction(5L, 10L, 100L, "🔥");
    }

    @Test(expected = ResponseStatusException.class)
    public void testToggle_nullEmoji() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        service.toggleReaction(5L, 10L, 100L, null);
    }

    @Test(expected = ResponseStatusException.class)
    public void testToggle_emptyEmoji() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        service.toggleReaction(5L, 10L, 100L, " ");
    }

    @Test(expected = ResponseStatusException.class)
    public void testToggle_msgNotFound() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        when(repo.existsByIdAndTripId(100L,10L)).thenReturn(false);
        service.toggleReaction(5L, 10L, 100L, "🔥");
    }

    @Test
    public void testToggle_removeExisting() {
        when(gate.isMember(10L,5L)).thenReturn(true);
        when(repo.existsByIdAndTripId(100L,10L)).thenReturn(true);

        MessageReaction r = new MessageReaction();
        r.setEmoji("🔥");
        r.setUserId(5L);
        r.setMessageId(100L);

        when(rxRepo.findByMessageIdAndUserIdAndEmoji(100L,5L,"🔥"))
                .thenReturn(Optional.of(r));

        when(rxRepo.findByMessageId(100L)).thenReturn(Collections.emptyList());

        Map<String,Integer> out = service.toggleReaction(5L,10L,100L,"🔥");
        assertTrue(out.isEmpty());

        verify(broadcaster).tripReactionToggle(10L,100L,"🔥");
    }

    @Test
    public void testToggle_addNew() {
        when(gate.isMember(10L,5L)).thenReturn(true);
        when(repo.existsByIdAndTripId(100L,10L)).thenReturn(true);

        when(rxRepo.findByMessageIdAndUserIdAndEmoji(100L,5L,"🔥"))
                .thenReturn(Optional.empty());

        List<MessageReaction> list = new ArrayList<>();
        MessageReaction r = new MessageReaction();
        r.setEmoji("🔥");
        r.setMessageId(100L);
        r.setUserId(5L);
        list.add(r);

        when(rxRepo.findByMessageId(100L)).thenReturn(list);

        Map<String,Integer> out = service.toggleReaction(5L,10L,100L,"🔥");
        assertEquals(1, out.get("🔥").intValue());

        verify(broadcaster).tripReactionToggle(10L,100L,"🔥");
    }

    // ------------ markReadProgress() ------------

    @Test(expected = ResponseStatusException.class)
    public void testMarkRead_notMember() {
        when(gate.isMember(10L, 5L)).thenReturn(false);
        service.markReadProgress(5L,10L,200L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testMarkRead_nullMessage() {
        when(gate.isMember(10L, 5L)).thenReturn(true);
        service.markReadProgress(5L,10L,null);
    }

    @Test(expected = ResponseStatusException.class)
    public void testMarkRead_notInTrip() {
        when(gate.isMember(10L, 5L)).thenReturn(true);

        when(repo.findByIdAndTripId(200L,10L)).thenReturn(Optional.empty());

        service.markReadProgress(5L,10L,200L);
    }

    @Test
    public void testMarkRead_newRecord() {
        when(gate.isMember(10L,5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m,200L);
        when(repo.findByIdAndTripId(200L,10L)).thenReturn(Optional.of(m));

        when(readRepo.findByTripIdAndUserId(10L,5L)).thenReturn(Optional.empty());
        when(repo.countByTripIdAndIdGreaterThanAndSenderIdNotAndIsDeletedFalse(10L,200L,5L))
                .thenReturn(3L);

        int unread = service.markReadProgress(5L,10L,200L);
        assertEquals(3, unread);

        verify(broadcaster).tripReadReceipt(eq(10L), eq(5L), eq(200L), eq(3));
    }

    @Test
    public void testMarkRead_advanceExisting() {
        when(gate.isMember(10L,5L)).thenReturn(true);

        TripMessage m = new TripMessage();
        setId(m,500L);
        when(repo.findByIdAndTripId(500L,10L)).thenReturn(Optional.of(m));

        TripReadProgress pr = new TripReadProgress();
        setReadId(pr, 1L);
        pr.setTripId(10L);
        pr.setUserId(5L);
        pr.setLastReadMessageId(100L);

        when(readRepo.findByTripIdAndUserId(10L,5L))
                .thenReturn(Optional.of(pr));

        when(repo.countByTripIdAndIdGreaterThanAndSenderIdNotAndIsDeletedFalse(10L,500L,5L))
                .thenReturn(2L);

        int unread = service.markReadProgress(5L,10L,500L);
        assertEquals(2, unread);

        verify(broadcaster).tripReadReceipt(eq(10L), eq(5L), eq(500L), eq(2));
    }

    private void setId(TripMessage m, Long id) {
        try {
            java.lang.reflect.Field f = TripMessage.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(m,id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setReadId(TripReadProgress p, Long id) {
        try {
            java.lang.reflect.Field f = TripReadProgress.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(p,id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
