package com.questbuddy.messages.direct.model;


import jakarta.persistence.*;
import java.time.Instant;

/**
 * A message reaction is like when you hold down on a message in Whatsapp/Telegram and you click an emoji
 */
@Entity
@Table(name = "direct_message_reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dm_reaction_one_per_user",
                columnNames = {"message_id", "user_id", "emoji"}
        ),
        indexes = {
                @Index(name = "idx_dm_reaction_message", columnList = "message_id"),
                @Index(name = "idx_dm_reaction_user", columnList = "user_id")
        })
public class DirectMessageReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "message_id", nullable = false)
    private Long messageId;


    @Column(name = "user_id", nullable = false)
    private Long userId;


    @Column(name = "emoji", nullable = false, length = 32)
    private String emoji;


    @Column(name = "reacted_at", nullable = false)
    private Instant reactedAt = Instant.now();


    public Long getId() { return id; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public Instant getReactedAt() { return reactedAt; }
    public void setReactedAt(Instant reactedAt) { this.reactedAt = reactedAt; }
}