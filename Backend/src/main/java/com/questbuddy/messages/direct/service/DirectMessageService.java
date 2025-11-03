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
import com.questbuddy.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

/**
 * This class handles the main logic of direct messaging.
 */
@Service
public class DirectMessageService {

    private final DirectMessageRepository messages;
    private final DirectMessageReactionRepository reactions;
    private final UserRepository users;
    private final DirectMessagingGate dmGate;
    private final DirectMessageMapper mapper;

    public DirectMessageService(DirectMessageRepository messages,
                                DirectMessageReactionRepository reactions,
                                UserRepository users,
                                DirectMessagingGate dmGate,
                                DirectMessageMapper mapper) {
        this.messages = messages;
        this.reactions = reactions;
        this.users = users;
        this.dmGate = dmGate;
        this.mapper = mapper;
    }

    // helper func to check basic stuff (is DM allowed? does User exist?)
    private void ensureCanDM(Long me, Long peerId) {
        if (!dmGate.canDM(me, peerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "DM not allowed");
        }
        if (peerId == null || !users.existsById(peerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Peer user not found");
        }
    }

    // list "limit" number of most recent messages between two users
    @Transactional(readOnly = true)
    public List<DirectMessageResponseDTO> list(Long me, Long peerId, Long beforeId, int limit) {
        ensureCanDM(me, peerId);

        List<DirectMessage> page = messages.pageConversation(me, peerId, beforeId, PageRequest.of(0, limit));
        if (page.isEmpty()) return Collections.emptyList();

        // Batch-load reactions for this page
        List<Long> ids = new ArrayList<>(page.size());
        for (DirectMessage m : page) ids.add(m.getId());
        List<DirectMessageReaction> rxnList = reactions.findByMessageIdIn(ids);

        Map<Long, Map<String, Integer>> counts = new HashMap<>();
        Map<Long, Set<String>> mine = new HashMap<>();

        for (DirectMessageReaction r : rxnList) {
            counts.computeIfAbsent(r.getMessageId(), k -> new HashMap<>())
                    .merge(r.getEmoji(), 1, Integer::sum);
            if (r.getUserId() != null && r.getUserId().equals(me)) {
                mine.computeIfAbsent(r.getMessageId(), k -> new HashSet<>()).add(r.getEmoji());
            }
        }

        return mapper.toResponses(page, counts, mine);
    }

    // sends a new dm from "me" to "peerId"
    @Transactional
    public DirectMessageResponseDTO post(Long me, Long peerId, DirectMessageCreateDTO in) {
        ensureCanDM(me, peerId);
        if (in == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }
        if (in.content() == null || in.content().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }
        if (in.clientMessageId() == null || in.clientMessageId().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientMessageId is required");
        }

        // same sender + clientMessageId returns existing
        Optional<DirectMessage> existing = messages.findBySenderIdAndClientMessageId(me, in.clientMessageId());
        if (existing.isPresent()) {
            DirectMessage e = existing.get();
            return mapper.toResponse(e, recountFor(e.getId()), myReactionsFor(e.getId(), me));
        }

        DirectMessage entity = mapper.toEntity(me, peerId, in, Instant.now());
        DirectMessage saved = messages.save(entity);
        return mapper.toResponse(saved, Collections.emptyMap(), Collections.emptySet());
    }

    // edit a message between "me" and "peerId"
    @Transactional
    public DirectMessageResponseDTO edit(Long me, Long peerId, Long messageId, DirectMessageEditDTO in) {
        ensureCanDM(me, peerId);
        if (in == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }

        DirectMessage m = messages.findByIdInConversation(messageId, me, peerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this DM"));

        if (!m.getSenderId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sender can edit");
        }
        if (in.version() == null || !in.version().equals(m.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Version mismatch");
        }

        mapper.applyEdit(m, in, Instant.now());
        DirectMessage saved = messages.save(m);
        return mapper.toResponse(saved, recountFor(saved.getId()), myReactionsFor(saved.getId(), me));
    }

    // delete a message
    @Transactional
    public void delete(Long me, Long peerId, Long messageId) {
        ensureCanDM(me, peerId);
        DirectMessage m = messages.findByIdInConversation(messageId, me, peerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this DM"));

        if (!m.getSenderId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sender can delete");
        }
        m.setDeleted(true);
        m.setDeletedAt(Instant.now());
        m.setDeletedBy(me);
        messages.save(m);
    }

    // react to a message
    @Transactional
    public Map<String, Integer> toggleReaction(Long me, Long peerId, Long messageId, String emoji) {
        ensureCanDM(me, peerId);
        if (emoji == null || emoji.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emoji is required");
        }
        String trimmed = emoji.trim();

        DirectMessage m = messages.findByIdInConversation(messageId, me, peerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this DM"));

        Optional<DirectMessageReaction> ex = reactions.findByMessageIdAndUserIdAndEmoji(messageId, me, trimmed);
        if (ex.isPresent()) {
            reactions.delete(ex.get());
        } else {
            DirectMessageReaction r = new DirectMessageReaction();
            r.setMessageId(messageId);
            r.setUserId(me);
            r.setEmoji(trimmed);
            r.setReactedAt(Instant.now());
            reactions.save(r);
        }
        return recountFor(messageId);
    }

    // mark a message as read
    @Transactional
    public void markRead(Long me, Long peerId, Long messageId) {
        ensureCanDM(me, peerId);
        DirectMessage m = messages.findByIdInConversation(messageId, me, peerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this DM"));

        if (!m.getRecipientId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recipient can mark read");
        }
        if (m.getReadAt() == null) {
            m.setReadAt(Instant.now());
            m.setReadByUserId(me);
            messages.save(m);
        }
    }

    //helpers

    private Map<String, Integer> recountFor(Long messageId) {
        List<DirectMessageReaction> list = reactions.findByMessageId(messageId);
        if (list.isEmpty()) return Collections.emptyMap();
        Map<String, Integer> out = new HashMap<>();
        for (DirectMessageReaction r : list) {
            out.merge(r.getEmoji(), 1, Integer::sum);
        }
        return Collections.unmodifiableMap(out);
    }

    private Set<String> myReactionsFor(Long messageId, Long me) {
        List<DirectMessageReaction> list = reactions.findByMessageId(messageId);
        if (list.isEmpty()) return Collections.emptySet();
        Set<String> mine = new HashSet<>();
        for (DirectMessageReaction r : list) {
            if (r.getUserId() != null && r.getUserId().equals(me)) {
                mine.add(r.getEmoji());
            }
        }
        return Collections.unmodifiableSet(mine);
    }
}
