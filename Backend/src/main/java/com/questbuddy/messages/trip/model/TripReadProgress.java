package com.questbuddy.messages.trip.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Made this entity to help out with read reciepts for group messages
 *
 * Tracks per-user read progress in a trip chat.
 * One row per (tripId,userId) with the highest message id they've read.
 */
@Entity
@Table(name = "trip_read_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id","user_id"}))
public class TripReadProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id for this specific progress

    @Column(name = "trip_id", nullable = false)
    private Long tripId; // id for the trip msg is realted to

    @Column(name = "user_id", nullable = false)
    private Long userId; // id of user reading the msg

    @Column(name = "last_read_message_id", nullable = false)
    private Long lastReadMessageId;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    // getters & setters

    public Long getId() { return id; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(Long lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }

    public Instant getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Instant lastReadAt) { this.lastReadAt = lastReadAt; }
}
