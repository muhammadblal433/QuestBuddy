package com.questbuddy.notification;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import com.questbuddy.model.User;
import com.questbuddy.calendar.Event;
import com.questbuddy.trip.Trip;
import com.questbuddy.model.Task;

import java.time.Instant;

@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_created", columnList = "recipient_id,created_at"),
                @Index(name = "idx_notifications_is_read", columnList = "is_read")
        })
public class Notification{

    // real primary key so Hibernate can create the table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient; // reciever of notification

    @Column(name = "title", nullable = false, length = 140)
    private String title; // title of notification (bold/heading text)

    @Column(name = "message", nullable = false, length = 2000)
    private String message; // description of notification

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 24)
    private NotificationType type; // is the notification an invite? a reminder? etc..

    // Optional references (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // when notification was made

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false; // status of if notification is read

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
