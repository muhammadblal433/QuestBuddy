package com.questbuddy.calendar;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity
@Table(name = "events",
        indexes = {
                @Index(name="idx_events_user_start", columnList="user_id,start_at")
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Id for event

    @Column(name = "user_id", nullable = false)
    private Long userId; // owner/creator of event (by id)

    @NotBlank
    @Size(max = 200)
    private String title; // name of event

    @Size(max = 2000)
    private String description; // desc. of event

    @Column(name = "start_at", nullable = false)
    private Instant startAt; // start time

    @Column(name = "end_at", nullable = false)
    private Instant endAt; // end time

    @Size(max = 300)
    private String location; // location of event

    @Column(name = "all_day", nullable = false)
    private boolean allDay = false; // sometimes, events don't have a definitive time

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // time made

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt; // most recent time updated

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {}

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getStartAt() {
        return startAt;
    }
    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }
    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAllDay() {
        return allDay;
    }
    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}