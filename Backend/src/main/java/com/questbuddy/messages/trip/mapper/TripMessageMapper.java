package com.questbuddy.messages.trip.mapper;

import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.trip.model.TripMessage;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class does 3 things:
 *
 * 1) Creates an entity based on a CreateDTO
 *
 * 2) Create a ResponseDTO based on an entity
 *
 * 3) Apply an edit
 */
@Component
public class TripMessageMapper {

    /**
     * 1) CreateDTO -> TripMessage Entity
     *
     * @param in               TripMessageCreateDTO from client
     * @param tripId           from URL path
     * @param senderId         from auth (current user)
     * @param sentAtFallback   use if DTO.sentAt is null
     * @param savedAt          server timestamp (now)
     */
    public TripMessage toEntity(TripMessageCreateDTO in,
                                long tripId,
                                long senderId,
                                Instant sentAtFallback,
                                Instant savedAt) {

        TripMessage m = new TripMessage();
        m.setTripId(tripId);
        m.setSenderId(senderId);
        m.setContent(in.content());
        m.setParentMessageId(in.parentMessageId());
        m.setForwardFromMessageId(in.forwardFromMessageId());
        m.setClientMessageId(in.clientMessageId());

        if (in.sentAt() != null) {
            m.setSentAt(in.sentAt());
        } else {
            m.setSentAt(sentAtFallback);
        }

        m.setSavedAt(savedAt);

        // new flags default for fresh message
        m.setEdited(false);
        m.setEditedAt(null);
        m.setDeleted(false);
        m.setDeletedAt(null);
        m.setDeletedBy(null);

        return m;
    }


    /**
     * TripMessage Entity -> ResponseDTO
     *
     * @param m - entity
     * @param reactionCounts - map of reaction to their count
     * @param myReactions - user's reactions
     * @return - ResponseDTO
     */
    public TripMessageResponseDTO toResponse(TripMessage m,
                                             Map<String, Integer> reactionCounts,
                                             Set<String> myReactions) {

        Map<String, Integer> countsView;
        if (reactionCounts == null || reactionCounts.isEmpty()) {
            countsView = Collections.emptyMap();
        } else {
            countsView = Collections.unmodifiableMap(new HashMap<String, Integer>(reactionCounts));
        }

        Set<String> mineView;
        if (myReactions == null || myReactions.isEmpty()) {
            mineView = Collections.emptySet();
        } else {
            mineView = Collections.unmodifiableSet(new HashSet<String>(myReactions));
        }

        // NOTE: constructor order must match TripMessageResponseDTO record/class
        return new TripMessageResponseDTO(
                m.getId(),
                m.getTripId(),
                m.getSenderId(),
                m.getContent(),
                m.getParentMessageId(),
                m.getForwardFromMessageId(),
                m.getSentAt(),
                m.getSavedAt(),
                m.isEdited(),
                m.getVersion(),
                countsView,
                mineView,
                m.getEditedAt(),
                m.isDeleted(),
                m.getDeletedAt(),
                m.getDeletedBy()
        );
    }


    /**
     * Apply an edit (pure field updates).
     */
    public void applyEdit(TripMessage target,
                          TripMessageEditDTO in,
                          Instant editedAt) {

        // content
        target.setContent(in.content());

        // flags
        target.setEdited(true);
        target.setEditedAt(editedAt);
    }


    /**
     * Batch mapping: keeps input order. countsByMsgId/myReactionsByMsgId may be null.
     */
    public List<TripMessageResponseDTO> toResponses(List<TripMessage> messages,
                                                    Map<Long, Map<String, Integer>> countsByMsgId,
                                                    Map<Long, Set<String>> myReactionsByMsgId) {

        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        List<TripMessageResponseDTO> out = new ArrayList<TripMessageResponseDTO>(messages.size());

        int i = 0;
        while (i < messages.size()) {
            TripMessage m = messages.get(i);

            Map<String, Integer> counts = null;
            if (countsByMsgId != null) {
                counts = countsByMsgId.get(m.getId());
            }

            Set<String> mine = null;
            if (myReactionsByMsgId != null) {
                mine = myReactionsByMsgId.get(m.getId());
            }

            TripMessageResponseDTO dto = toResponse(m, counts, mine);
            out.add(dto);
            i = i + 1;
        }

        return out;
    }
}
