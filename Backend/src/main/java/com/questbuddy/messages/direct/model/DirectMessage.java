package com.questbuddy.messages.direct.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "direct_messages",
        uniqueConstraints = @UniqueConstraint(name = "uq_dm_sender_client",
                columnNames = {"sender_id", "client_message_id"}),
        indexes = {
                @Index(name = "idx_dm_pair_saved", columnList = "sender_id,recipient_id,saved_at DESC"),
                @Index(name = "idx_dm_pair_id",    columnList = "sender_id,recipient_id,id DESC")
        })
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // participants
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    // content
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    // reply / forward
    @Column(name = "parent_message_id")
    private Long parentMessageId;

    @Column(name = "forward_from_message_id")
    private Long forwardFromMessageId;

    // idempotency + timing
    @Column(name = "client_message_id", nullable = false, length = 64)
    private String clientMessageId;

    @Column(name = "sent_at")
    private Instant sentAt; // optional client clock

    @Column(name = "saved_at", nullable = false, updatable = false)
    private Instant savedAt = Instant.now();

    // edits / versioning
    @Column(name = "edited", nullable = false)
    private boolean edited = false;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Version
    private Long version;

    // soft delete
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    // read receipts
    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "read_by_user_id")
    private Long readByUserId;

    // --- getters/setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getParentMessageId() { return parentMessageId; }
    public void setParentMessageId(Long parentMessageId) { this.parentMessageId = parentMessageId; }

    public Long getForwardFromMessageId() { return forwardFromMessageId; }
    public void setForwardFromMessageId(Long forwardFromMessageId) { this.forwardFromMessageId = forwardFromMessageId; }

    public String getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(String clientMessageId) { this.clientMessageId = clientMessageId; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getSavedAt() { return savedAt; }
    public void setSavedAt(Instant savedAt) { this.savedAt = savedAt; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    public Instant getEditedAt() { return editedAt; }
    public void setEditedAt(Instant editedAt) { this.editedAt = editedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public Long getReadByUserId() { return readByUserId; }
    public void setReadByUserId(Long readByUserId) { this.readByUserId = readByUserId; }
}
