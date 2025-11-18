package com.questbuddy.events.trip.service;

import com.questbuddy.events.trip.dto.TripEventCreateDTO;
import com.questbuddy.events.trip.dto.TripEventEditDTO;
import com.questbuddy.events.trip.mapper.TripEventMapper;
import com.questbuddy.events.trip.dto.TripEventResponseDTO;
import com.questbuddy.events.trip.mapper.TripEventMapper;
import com.questbuddy.events.trip.model.TripEvent;
import com.questbuddy.events.trip.repository.TripEventRepository;
import com.questbuddy.tripmember.service.TripMembershipService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import com.questbuddy.notification.NotificationService;
import com.questbuddy.notification.NotificationType;
import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.model.User;

@Service
public class TripEventService {

    private final TripEventRepository repo;
    private final TripMembershipService membership; // <-- real bean

    private final NotificationService notifications;


    public TripEventService(TripEventRepository repo, TripMembershipService membership, NotificationService notifications) {
        this.repo = repo;
        this.membership = membership;
        this.notifications = notifications;
    }

    // helper for notis
    private void notifyTripMembers(Long actorId, Long tripId, Long eventId,
                                   String title, String message, NotificationType type) {
        try {
            for (User u : membership.listAccepted(actorId, tripId)) {
                if (u.getId().equals(actorId)) continue; // don't notify self
                notifications.create(new NotificationCreateDTO(
                        u.getId(), title, message, type, eventId, tripId, null
                ));
            }
        } catch (Exception ex) {
            System.err.println("[TripEventService] notify failed: " + ex.getMessage());
        }
    }

    private static String shortIso(java.time.Instant t) {
        return t == null ? "" : t.toString();
    }

    @Transactional(readOnly = true)
    public Page<TripEventResponseDTO> list(Long me, Long tripId, Instant from, Instant to, int page, int size) {
        membership.ensureMember(me, tripId);
        Pageable pageable = PageRequest.of(page, size);

        Page<TripEvent> p = (from != null && to != null)
                ? repo.findByTripIdAndDeletedAtIsNullAndStartsAtBetweenOrderByStartsAtAscIdAsc(tripId, from, to, pageable)
                : repo.findByTripIdAndDeletedAtIsNullOrderByStartsAtAscIdAsc(tripId, pageable);

        return p.map(TripEventMapper::toDTO);
    }

    @Transactional
    public TripEventResponseDTO create(Long me, Long tripId, TripEventCreateDTO in) {
        membership.ensureMember(me, tripId);
        validateDates(in.startsAt(), in.endsAt());

        TripEvent e = TripEventMapper.fromCreate(tripId, me, in);
        TripEvent saved = repo.save(e);

        notifyTripMembers(me, tripId, saved.getId(),
                "New itinerary event",
                saved.getName() + " • " + shortIso(saved.getStartsAt()),
                NotificationType.EVENT_CREATED);

        return TripEventMapper.toDTO(saved);
    }

    @Transactional
    public TripEventResponseDTO edit(Long me, Long tripId, Long eventId, TripEventEditDTO in) {
        membership.ensureMember(me, tripId);

        TripEvent e = repo.findByIdAndTripIdAndDeletedAtIsNull(eventId, tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "event"));

        ensureOwnerOrCreator(me, tripId, e);

        TripEventMapper.applyEdit(e, in);

        if (e.getStartsAt() != null && e.getEndsAt() != null) {
            validateDates(e.getStartsAt(), e.getEndsAt());
        }
        notifyTripMembers(me, tripId, e.getId(),
                "Itinerary updated",
                e.getName() + " • " + shortIso(e.getStartsAt()),
                NotificationType.EVENT_UPDATED);

        return TripEventMapper.toDTO(e);
    }

    @Transactional
    public void delete(Long me, Long tripId, Long eventId) {
        membership.ensureMember(me, tripId);

        TripEvent e = repo.findByIdAndTripIdAndDeletedAtIsNull(eventId, tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "event"));

        ensureOwnerOrCreator(me, tripId, e);

        e.setDeletedAt(Instant.now());

        notifyTripMembers(me, tripId, e.getId(),
                "Itinerary removed",
                e.getName(),
                NotificationType.EVENT_CANCELLED);
    }

    private void validateDates(Instant start, Instant end) {
        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt must be >= startsAt");
        }
    }

    private void ensureOwnerOrCreator(Long me, Long tripId, TripEvent e) {
        if (e.getCreatorId() != null && e.getCreatorId().equals(me)) return;
        if (membership.isOwner(me, tripId)) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only owner or creator can modify");
    }
}
