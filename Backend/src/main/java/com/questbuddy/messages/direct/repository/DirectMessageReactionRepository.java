package com.questbuddy.messages.direct.repository;


import com.questbuddy.messages.direct.model.DirectMessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;


import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface DirectMessageReactionRepository extends JpaRepository<DirectMessageReaction, Long> {
    // used to check if this user already reacted with this emoji to this message
    Optional<DirectMessageReaction> findByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);


    // list all reactions to a list of messages
    List<DirectMessageReaction> findByMessageIdIn(Collection<Long> messageIds);

    // list all reactions to a message
    List<DirectMessageReaction> findByMessageId(Long messageId);

    // remove a reaction
    @Modifying
    int deleteByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);
}