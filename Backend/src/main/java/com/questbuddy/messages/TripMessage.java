package com.questbuddy.messages;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages",
        uniqueConstraints = @UniqueConstraint(name="uq_msg_sender_client",
                columnNames = {"sender_id","client_message_id"}),
        indexes = {
                @Index(name="idx_msg_trip_saved", columnList="trip_id,saved_at DESC"),
                @Index(name="idx_msg_trip_id", columnList="trip_id,id DESC")
        })
public class TripMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id of message

    @Column(name="trip_id", nullable=false)
    private Long tripId; // id of trip associated w/ message

    @Column(name="sender_id", nullable=false)
    private Long senderId; // id of person who sent this message

    @Column(nullable=false, length=2000)
    private String content; // what the message says

    // reply / forward (nullable)
    @Column(name="parent_message_id")
    private Long parentMessageId; // the id of the message we might be replying to

    @Column(name="forward_from_message_id")
    private Long forwardFromMessageId; // the id of the message we are forwarding from

    // client vs server times
    @Column(name="sent_at", nullable=false)
    private Instant sentAt;

    @Column(name="saved_at", nullable=false)
    private Instant savedAt;

    @Column(name="is_edited", nullable=false)
    private boolean edited = false;

    // for idempotency
    @Column(name="client_message_id", length=64, nullable=false)
    private String clientMessageId;

    // to handle the "last edit wins" cases
    @Version
    private Long version;

    // getters/setters
    public Long getId() { return id; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getParentMessageId() { return parentMessageId; }
    public void setParentMessageId(Long parentMessageId) { this.parentMessageId = parentMessageId; }

    public Long getForwardFromMessageId() { return forwardFromMessageId; }
    public void setForwardFromMessageId(Long forwardFromMessageId) { this.forwardFromMessageId = forwardFromMessageId; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getSavedAt() { return savedAt; }
    public void setSavedAt(Instant savedAt) { this.savedAt = savedAt; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    public String getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(String clientMessageId) { this.clientMessageId = clientMessageId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
