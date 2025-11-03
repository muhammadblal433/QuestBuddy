package com.questbuddy.messages.direct.repository;


import com.questbuddy.messages.direct.model.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    // Regular lookup
    Optional<DirectMessage> findBySenderIdAndClientMessageId(Long senderId, String clientMessageId);


    /**
     * Below are custom queries made
     */

    // latest messages between two users in either direction
    @Query("""
        select m from DirectMessage m
        where ((m.senderId = :a and m.recipientId = :b) or (m.senderId = :b and m.recipientId = :a))
        and (:beforeId is null or m.id < :beforeId)
        order by m.id desc
        """)
    List<DirectMessage> pageConversation(Long a, Long b, Long beforeId, Pageable pageable);

    // fetch a msg iff it belongs to this conversation
    @Query("""
        select m from DirectMessage m
        where m.id = :id and ((m.senderId = :a and m.recipientId = :b) or (m.senderId = :b and m.recipientId = :a))
        """)
    Optional<DirectMessage> findByIdInConversation(Long id, Long a, Long b);
}