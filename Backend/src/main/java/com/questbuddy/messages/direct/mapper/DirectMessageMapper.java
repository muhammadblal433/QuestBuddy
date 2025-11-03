package com.questbuddy.messages.direct.mapper;

import com.questbuddy.messages.direct.dto.*;
import com.questbuddy.messages.direct.model.DirectMessage;
import org.springframework.stereotype.Component;


import java.time.Instant;
import java.util.*;

/**
 * This class does 3 things:
 *
 * 1. Create entity from DTO
 *
 * 2. Edit a message by edit DTO
 *
 * 3. Create a response DTO from an entity
 */
@Component
public class DirectMessageMapper {

    // 1
    public DirectMessage toEntity(Long me, Long peerId, DirectMessageCreateDTO in, Instant savedAt) {
        DirectMessage m = new DirectMessage();
        m.setSenderId(me);
        m.setRecipientId(peerId);
        m.setContent(in.content());
        m.setParentMessageId(in.parentMessageId());
        m.setForwardFromMessageId(in.forwardFromMessageId());
        m.setClientMessageId(in.clientMessageId());
        m.setSentAt(in.sentAt());
        m.setSavedAt(savedAt);
        m.setEdited(false);
        m.setVersion(1L);
        m.setDeleted(false);
        return m;
    }


    // 2
    public void applyEdit(DirectMessage target, DirectMessageEditDTO in, Instant editedAt) {
        target.setContent(in.content());
        target.setEdited(true);
        target.setEditedAt(editedAt);
        target.setVersion(target.getVersion() == null ? 1L : target.getVersion() + 1L);
    }


    // 3
    public DirectMessageResponseDTO toResponse(DirectMessage m,
                                               Map<String, Integer> reactionCounts,
                                               Set<String> myReactions) {
        Map<String, Integer> countsView = (reactionCounts == null || reactionCounts.isEmpty())
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(reactionCounts));
        Set<String> mineView = (myReactions == null || myReactions.isEmpty())
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(myReactions));


        boolean readByRecipient = (m.getReadByUserId() != null) && m.getReadByUserId().equals(m.getRecipientId());


        return new DirectMessageResponseDTO(
                m.getId(),
                m.getSenderId(),
                m.getRecipientId(),
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
                m.getDeletedBy(),
                m.getReadAt(),
                readByRecipient
        );
    }


    // Batch
    public List<DirectMessageResponseDTO> toResponses(List<DirectMessage> messages,
                                                      Map<Long, Map<String, Integer>> countsByMsgId,
                                                      Map<Long, Set<String>> myReactionsByMsgId) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        List<DirectMessageResponseDTO> out = new ArrayList<>(messages.size());
        for (DirectMessage m : messages) {
            Map<String, Integer> counts = countsByMsgId == null ? null : countsByMsgId.get(m.getId());
            Set<String> mine = myReactionsByMsgId == null ? null : myReactionsByMsgId.get(m.getId());
            out.add(toResponse(m, counts, mine));
        }
        return Collections.unmodifiableList(out);
    }
}