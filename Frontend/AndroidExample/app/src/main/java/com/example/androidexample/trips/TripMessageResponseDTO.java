package com.example.androidexample.trips;

import java.util.Map;
import java.util.Set;


// Mirrors backend; timestamps kept as String for simplicity
public class TripMessageResponseDTO {
    private long id;
    private long tripId;
    private long senderId;
    private String content;
    private Long parentMessageId;
    private Long forwardFromMessageId;
    private String sentAt;
    private String savedAt;
    private boolean edited;
    private Long version;
    private Map<String, Integer> reactions;
    private Set<String> myReactions;
    private String editedAt;
    private boolean deleted;
    private String deletedAt;
    private Long deletedBy;


    public TripMessageResponseDTO(long id, long tripId, long senderId, String content,
                                  Long parentMessageId, Long forwardFromMessageId,
                                  String sentAt, String savedAt, boolean edited, Long version,
                                  Map<String, Integer> reactions, Set<String> myReactions,
                                  String editedAt, boolean deleted, String deletedAt, Long deletedBy) {
        this.id = id; this.tripId = tripId; this.senderId = senderId; this.content = content;
        this.parentMessageId = parentMessageId; this.forwardFromMessageId = forwardFromMessageId;
        this.sentAt = sentAt; this.savedAt = savedAt; this.edited = edited; this.version = version;
        this.reactions = reactions; this.myReactions = myReactions;
        this.editedAt = editedAt; this.deleted = deleted; this.deletedAt = deletedAt; this.deletedBy = deletedBy;
    }


    // Unique ID for this message in the system (handy for diffing, updates, etc.)
    public long getId() { return id; }

    // Which trip/conversation this message belongs to.
    public long getTripId() { return tripId; }

    // Who sent it (the user ID of the author).
    public long getSenderId() { return senderId; }

    // The actual text the user typed. Could be empty or null if the message was deleted.
    public String getContent() { return content; }

    // If this is a reply, this points to the message we’re replying to. Otherwise null.
    public Long getParentMessageId() { return parentMessageId; }

    // If this was forwarded, this points to the original message we forwarded. Otherwise null.
    public Long getForwardFromMessageId() { return forwardFromMessageId; }

    // When the sender hit “send” (client or server timestamp string).
    public String getSentAt() { return sentAt; }

    // When the server finally saved it (useful if you care about persistence timing).
    public String getSavedAt() { return savedAt; }

    // True if the message text was edited after it was first sent.
    public boolean isEdited() { return edited; }

    // Incremented every time the message changes—helps with optimistic updates and concurrency.
    public Long getVersion() { return version; }

    // Emoji (or reaction) counts
    public Map<String, Integer> getReactions() { return reactions; }

    // Which reactions the current user have added to this message.
    public Set<String> getMyReactions() { return myReactions; }

    // When it was last edited (null if never edited).
    public String getEditedAt() { return editedAt; }

    // True if the message was deleted (we usually keep a tombstone instead of hard-removing it).
    public boolean isDeleted() { return deleted; }

    // When the delete happened (if it did).
    public String getDeletedAt() { return deletedAt; }

    // Who deleted it (could be the author or a moderator/admin).
    public Long getDeletedBy() { return deletedBy; }
}