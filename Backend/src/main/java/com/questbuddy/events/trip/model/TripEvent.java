package com.questbuddy.events.trip.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "trip_events",
        indexes = {
                @Index(name = "ix_trip_events_trip_starts", columnList = "tripId,startsAt,id"),
                @Index(name = "ix_trip_events_trip_created", columnList = "tripId,createdAt")
        })
/**
 * This entity is to handle special trip events "like shared between users on a trip"
 */
public class TripEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tripId;

    @Column(nullable = false)
    private Long creatorId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false)
    private Instant startsAt;

    @Column(nullable = false)
    private Instant endsAt;

    @Column(length = 160)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private Integer position;

    // Any additional attachments to this event
    @Column(columnDefinition = "TEXT")
    private String attachmentRefsJson;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // delete means we just "hide it" in our frontend, but we still store it
    @Column
    private Instant deletedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // function to return bool for if tripEvent is present or deleted
    public boolean isDeleted() { return deletedAt != null; }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }

    public Instant getEndsAt() { return endsAt; }
    public void setEndsAt(Instant endsAt) { this.endsAt = endsAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public String getAttachmentRefsJson() { return attachmentRefsJson; }
    public void setAttachmentRefsJson(String attachmentRefsJson) { this.attachmentRefsJson = attachmentRefsJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}