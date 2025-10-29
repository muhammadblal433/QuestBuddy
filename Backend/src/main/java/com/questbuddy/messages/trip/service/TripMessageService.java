package com.questbuddy.messages.trip.service;

import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.guard.TripMembershipGate;
import com.questbuddy.messages.trip.mapper.TripMessageMapper;
import com.questbuddy.messages.trip.model.MessageReaction;
import com.questbuddy.messages.trip.model.TripMessage;
import com.questbuddy.messages.trip.repository.MessageReactionRepository;
import com.questbuddy.messages.trip.repository.TripMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TripMessageService {

    private final TripMessageRepository messages;
    private final MessageReactionRepository reactions;
    private final TripMembershipGate membershipGate;
    private final TripMessageMapper mapper;

    public TripMessageService(TripMessageRepository messages,
                              MessageReactionRepository reactions,
                              TripMembershipGate membershipGate,
                              TripMessageMapper mapper) {
        this.messages = messages;
        this.reactions = reactions;
        this.membershipGate = membershipGate;
        this.mapper = mapper;
    }

    // ----- helpers -----

    private TripMessage findExisting(Long senderId, String clientMessageId) {
        if (clientMessageId == null) {
            return null;
        }
        return messages.findBySenderIdAndClientMessageId(senderId, clientMessageId).orElse(null);
    }

    private TripMessage findByIdAndTrip(Long messageId, Long tripId) {
        if (messageId == null || tripId == null) {
            return null;
        }
        return messages.findByIdAndTripId(messageId, tripId).orElse(null);
    }

    private MessageReaction findReaction(Long messageId, Long userId, String emoji) {
        return reactions.findByMessageIdAndUserIdAndEmoji(messageId, userId, emoji).orElse(null);
    }

    private Map<String, Integer> recountFor(Long messageId) {
        List<MessageReaction> list = reactions.findByMessageId(messageId);
        if (list.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> out = new HashMap<String, Integer>();
        int k = 0;
        while (k < list.size()) {
            MessageReaction r = list.get(k);
            Integer prev = out.get(r.getEmoji());
            if (prev == null) {
                out.put(r.getEmoji(), 1);
            } else {
                out.put(r.getEmoji(), prev + 1);
            }
            k = k + 1;
        }
        return out;
    }

    private Set<String> myReactionsFor(Long messageId, Long me) {
        List<MessageReaction> list = reactions.findByMessageId(messageId);
        if (list.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> mine = new HashSet<String>();
        int t = 0;
        while (t < list.size()) {
            MessageReaction r = list.get(t);
            if (r.getUserId() != null && r.getUserId().equals(me)) {
                mine.add(r.getEmoji());
            }
            t = t + 1;
        }
        return Collections.unmodifiableSet(mine);
    }

    // post a message
    @Transactional
    public TripMessageResponseDTO post(Long me, Long tripId, TripMessageCreateDTO in) {
        if (!membershipGate.isMember(tripId, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this trip");
        }
        if (in == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }
        if (in.content() == null || in.content().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }
        if (in.clientMessageId() == null || in.clientMessageId().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientMessageId is required");
        }

        TripMessage existing = findExisting(me, in.clientMessageId());
        if (existing != null) {
            Map<String, Integer> counts = recountFor(existing.getId());
            Set<String> mine = myReactionsFor(existing.getId(), me);
            return mapper.toResponse(existing, counts, mine);
        }

        if (in.parentMessageId() != null) {
            TripMessage parent = findByIdAndTrip(in.parentMessageId(), tripId);
            if (parent == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "parentMessageId not found in this trip");
            }
        }
        if (in.forwardFromMessageId() != null) {
            TripMessage fwd = findByIdAndTrip(in.forwardFromMessageId(), tripId);
            if (fwd == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "forwardFromMessageId not found in this trip");
            }
        }

        Instant now = Instant.now();
        TripMessage toSave = mapper.toEntity(in, tripId, me, now, now);
        TripMessage saved = messages.save(toSave);

        return mapper.toResponse(saved, Collections.<String, Integer>emptyMap(), Collections.<String>emptySet());
    }

    // listing out messages
    @Transactional(readOnly = true)
    public List<TripMessageResponseDTO> list(Long me, Long tripId, Long beforeId, int limit) {
        if (!membershipGate.isMember(tripId, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this trip");
        }

        int cap = 50;
        if (limit <= 0 || limit > cap) {
            limit = cap;
        }

        List<TripMessage> page;
        if (beforeId == null) {
            page = messages.findTop50ByTripIdOrderByIdDesc(tripId);
        } else {
            page = messages.findTop50ByTripIdAndIdLessThanOrderByIdDesc(tripId, beforeId);
        }

        if (page.size() > limit) {
            page = new ArrayList<TripMessage>(page.subList(0, limit));
        }
        if (page.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = new ArrayList<Long>(page.size());
        int i = 0;
        while (i < page.size()) {
            ids.add(page.get(i).getId());
            i = i + 1;
        }

        List<MessageReaction> rs = reactions.findByMessageIdIn(ids);

        Map<Long, Map<String, Integer>> countsByMsgId = new HashMap<Long, Map<String, Integer>>();
        Map<Long, Set<String>> myByMsgId = new HashMap<Long, Set<String>>();

        int j = 0;
        while (j < rs.size()) {
            MessageReaction r = rs.get(j);
            Long mid = r.getMessageId();

            Map<String, Integer> cmap = countsByMsgId.get(mid);
            if (cmap == null) {
                cmap = new HashMap<String, Integer>();
                countsByMsgId.put(mid, cmap);
            }
            Integer prev = cmap.get(r.getEmoji());
            if (prev == null) {
                cmap.put(r.getEmoji(), 1);
            } else {
                cmap.put(r.getEmoji(), prev + 1);
            }

            if (r.getUserId() != null && r.getUserId().equals(me)) {
                Set<String> mine = myByMsgId.get(mid);
                if (mine == null) {
                    mine = new HashSet<String>();
                    myByMsgId.put(mid, mine);
                }
                mine.add(r.getEmoji());
            }
            j = j + 1;
        }

        return mapper.toResponses(page, countsByMsgId, myByMsgId);
    }

    // react to a msg -> afterwards update hashmap for count of rxn to msg
    @Transactional
    public Map<String, Integer> toggleReaction(Long me, Long tripId, Long messageId, String emoji) {
        if (!membershipGate.isMember(tripId, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this trip");
        }
        if (emoji == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emoji is required");
        }
        String trimmed = emoji.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emoji is required");
        }
        if (!messages.existsByIdAndTripId(messageId, tripId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this trip");
        }

        MessageReaction existing = findReaction(messageId, me, trimmed);
        if (existing != null) {
            reactions.delete(existing);
        } else {
            MessageReaction r = new MessageReaction();
            r.setMessageId(messageId);
            r.setUserId(me);
            r.setEmoji(trimmed);
            r.setReactedAt(Instant.now());
            reactions.save(r);
        }

        return recountFor(messageId);
    }
}