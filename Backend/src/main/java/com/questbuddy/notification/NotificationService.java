package com.questbuddy.notification;

import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.NotificationMapper;
import com.questbuddy.notification.Notification;
import com.questbuddy.notification.NotificationType;

import com.questbuddy.repository.NotificationRepository;
import com.questbuddy.repository.UserRepository;
import com.questbuddy.repository.TaskRepository;
import com.questbuddy.trip.TripRepository;
import com.questbuddy.calendar.EventRepository;

import com.questbuddy.model.User;
import com.questbuddy.model.Task;
import com.questbuddy.trip.Trip;
import com.questbuddy.calendar.Event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

// Logic of notifications
@Service
public class NotificationService {
    // Unlike Trip and event, we need multiple repos as our notification makes references to different fields like Users, Events, Trips, Tasks, etc.
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final TripRepository trips;
    private final EventRepository events;
    private final TaskRepository tasks;

    // mapper
    private final NotificationMapper mapper;

    /**
     * Constructor for Notification Service class
     */
    public NotificationService(NotificationRepository notifications,
                               UserRepository users,
                               TripRepository trips,
                               EventRepository events,
                               TaskRepository tasks,
                               NotificationMapper mapper) {
        this.notifications = notifications;
        this.users = users;
        this.trips = trips;
        this.events = events;
        this.tasks = tasks;
        this.mapper = mapper;
    }

    /**
     * Create a notification based on a CreateDTO
     *
     * @param - CreateDTO
     */
    @Transactional
    public Notification create(NotificationCreateDTO dto) {
        User recipient = null;
        Trip trip = null;
        Event event = null;
        Task task = null;

        // Reassigning variables and handling null errors
        // recipient (required)
        Optional<User> recOpt = users.findById(dto.recipientId());
        if (recOpt.isEmpty()) {
            throw new IllegalArgumentException("recipient_not_found");
        }
        User recipient = recOpt.get();

        // trip (optional)
        if (dto.tripId() != null) {
            Optional<Trip> tripOpt = trips.findById(dto.tripId());
            if (tripOpt.isEmpty()) {
                throw new IllegalArgumentException("trip_not_found");
            }
            trip = tripOpt.get();
        }

        // event (optional)
        if (dto.eventId() != null) {
            Optional<Event> evOpt = events.findById(dto.eventId());
            if (evOpt.isEmpty()) {
                throw new IllegalArgumentException("event_not_found");
            }
            event = evOpt.get();
        }

        // task (optional)
        if (dto.taskId() != null) {
            Optional<Task> tOpt = tasks.findById(dto.taskId());
            if (tOpt.isEmpty()) {
                throw new IllegalArgumentException("task_not_found");
            }
            task = tOpt.get();
        }

        Notification n = mapper.fromCreate(dto, recipient, trip, event, task);
        return notifications.save(n);
    }

    /**
     * List notifications for a user (optionally unread-only), newest first.
     *
     * @param recipientId - person receiving noti
     * @param unreadOnly - boolean for if we want all notis listed or only unread ones
     */
    @Transactional(readOnly = true)
    public List<Notification> listForUser(Long recipientId, Boolean unreadOnly) {
        if (Boolean.TRUE.equals(unreadOnly)) {
            return notifications.findAllByRecipient_IdAndIsReadOrderByCreatedAtDesc(recipientId, false);
        }
        return notifications.findAllByRecipient_IdOrderByCreatedAtDesc(recipientId);
    }

    /**
     * Mark a notification as read & enforce ownership of noti
     *
     * @param id - noti id
     * @param recipientId - user id of person recieving
     */
    public Notification markRead(Long id, Long recipientId) {
        Notification noti = null;
        Optional<Notification> notiOpt = notifications.findById(id);
        if (notiOpt.isEmpty()) {
            throw new IllegalArgumentException("notification_not_found");
        }
        noti = notiOpt.get();

        if (!noti.getRecipient().getId().equals(recipientId)) {
            throw new SecurityException("forbidden");
        }
        if (!noti.isRead()) {
            noti.setRead(true);
        }

        return noti;
    }

    /**
     * Unread badge count for a user.
     *
     * @param recipientId - user id we want count for
     */
    @Transactional(readOnly = true)
    public long countUnread(Long recipientId) {
        return notifications.countByRecipient_IdAndIsReadFalse(recipientId);
    }

    /**
     * Optional delete function
     */
    @Transactional
    public boolean deleteForOwner(Long id, Long recipientId) {
        Notification noti = null;
        Optional<Notification> notiOpt = notifications.findById(id);
        if (notiOpt.isEmpty()) {
            throw new IllegalArgumentException("notification_not_found");
        }
        noti = notiOpt.get();

        if (!noti.getRecipient().getId().equals(recipientId)) {
            throw new SecurityException("forbidden");
        }
        notifications.delete(noti);
        return true;
    }
}