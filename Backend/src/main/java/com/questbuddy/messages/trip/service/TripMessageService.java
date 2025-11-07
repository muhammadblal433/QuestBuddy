package com.questbuddy.messages.trip.service;

import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
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

// For read reciepts
import com.questbuddy.messages.trip.model.TripReadProgress;
import com.questbuddy.messages.trip.repository.TripReadProgressRepository;

import com.questbuddy.messages.ws.ChatBroadcaster; //add for websocket

@Service
public class TripMessageService {

    private final TripMessageRepository messages;
    private final MessageReactionRepository reactions;
    private final TripMembershipGate membershipGate;
    private final TripMessageMapper mapper;
    private final ChatBroadcaster chatBroadcaster;
    private final TripReadProgressRepository readProgress;

    public TripMessageService(TripMessageRepository messages,
                              MessageReactionRepository reactions,
                              TripMembershipGate membershipGate,
                              TripMessageMapper mapper,
                              ChatBroadcaster chatBroadcaster,
                              TripReadProgressRepository readProgress) {
        this.messages = messages;
        this.reactions = reactions;
        this.membershipGate = membershipGate;
        this.mapper = mapper;
        this.chatBroadcaster = chatBroadcaster;
        this.readProgress = readProgress;
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

        // Build response and broadcast to the trip
        TripMessageResponseDTO out = mapper.toResponse(saved, Collections.<String, Integer>emptyMap(), Collections.<String>emptySet());
        chatBroadcaster.tripMessageNew(tripId, out); // for ws
        return out;
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

    @Transactional
    public TripMessageResponseDTO edit(Long me,
                                       Long tripId,
                                       Long messageId,
                                       TripMessageEditDTO in) {

        if (!membershipGate.isMember(tripId, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this trip");
        }
        if (in == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }
        if (in.content() == null || in.content().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }
        if (in.version() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "version is required");
        }

        // load message
        TripMessage msg = findByIdAndTrip(messageId, tripId);
        if (msg == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this trip");
        }

        // cannot edit deleted messages
        if (msg.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Message is deleted");
        }

        // only sender can edit
        if (msg.getSenderId() == null || !msg.getSenderId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the sender can edit this message");
        }

        // optimistic lock check (extra safety in addition to @Version on entity)
        Long currentVersion = msg.getVersion();
        if (currentVersion == null || !currentVersion.equals(in.version())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Version mismatch");
        }

        // apply edit
        Instant now = Instant.now();
        mapper.applyEdit(msg, in, now);

        // save
        TripMessage saved = messages.save(msg);

        // reactions unchanged
        Map<String, Integer> counts = recountFor(saved.getId());
        Set<String> mine = myReactionsFor(saved.getId(), me);
        TripMessageResponseDTO out = mapper.toResponse(saved, counts, mine);
        chatBroadcaster.tripEdit(tripId, out);   // for ws
        return out;

    }


    @Transactional
    public TripMessageResponseDTO delete(Long me, Long tripId, Long messageId, Long version) {

        if (!membershipGate.isMember(tripId, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this trip");
        }
        if (version == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "version is required");
        }

        // load
        TripMessage msg = findByIdAndTrip(messageId, tripId);
        if (msg == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this trip");
        }

        // only sender can delete
        if (msg.getSenderId() == null || !msg.getSenderId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the sender can delete this message");
        }

        // already deleted
        if (msg.isDeleted()) {
            return mapper.toResponse(msg, Collections.<String, Integer>emptyMap(), Collections.<String>emptySet());
        }


        Long currentVersion = msg.getVersion();
        if (currentVersion == null || !currentVersion.equals(version)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Version mismatch");
        }

        // soft delete
        msg.setDeleted(true);
        msg.setDeletedAt(Instant.now());
        msg.setDeletedBy(me);

        TripMessage saved = messages.save(msg);

        // clear reactions without repo changes
        java.util.List<MessageReaction> rs = reactions.findByMessageId(messageId);
        int i = 0;
        while (i < rs.size()) {
            reactions.delete(rs.get(i));
            i = i + 1;
        }

        // return with empty counts/myReactions
        TripMessageResponseDTO out = mapper.toResponse(saved, Collections.<String, Integer>emptyMap(), Collections.<String>emptySet());
        chatBroadcaster.tripDelete(tripId, out);  // for ws
        return out;
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

        chatBroadcaster.tripReactionToggle(tripId, messageId, trimmed);

        return recountFor(messageId);
    }

    // Read reciepts
    @Transactional
    public int markReadProgress(Long me, Long tripId, Long upToMessageId) {
        if (!membershipGate.isMember(tripId, me)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Not a member of this trip");
        }
        if (upToMessageId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "messageId is required");
        }

        // validate the pointer belongs to this trip
        TripMessage last = findByIdAndTrip(upToMessageId, tripId);
        if (last == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "messageId not in this trip");
        }

        java.time.Instant now = java.time.Instant.now();
        TripReadProgress pr = readProgress.findByTripIdAndUserId(tripId, me).orElse(null);

        if (pr == null) {
            pr = new TripReadProgress();
            pr.setTripId(tripId);
            pr.setUserId(me);
            pr.setLastReadMessageId(upToMessageId);
            pr.setLastReadAt(now);
            readProgress.save(pr);
        } else if (pr.getLastReadMessageId() == null || pr.getLastReadMessageId() < upToMessageId) {
            pr.setLastReadMessageId(upToMessageId);
            pr.setLastReadAt(now);
            readProgress.save(pr);
        } // else: no advance; keep previous pointer

        int unread = (int) messages.countByTripIdAndIdGreaterThanAndSenderIdNotAndIsDeletedFalse(tripId, pr.getLastReadMessageId(), me);
        // broadcast to the trip (small payload; no message content)
        chatBroadcaster.tripReadReceipt(tripId, me, pr.getLastReadMessageId(), unread);
        return unread;
    }
}