package com.questbuddy.messages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    // Batch-load all reactions for a page of messages
    List<MessageReaction> findByMessageIdIn(Collection<Long> messageIds);

    List<MessageReaction> findByMessageId(Long messageId);

    @Modifying
    int deleteByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);
}
