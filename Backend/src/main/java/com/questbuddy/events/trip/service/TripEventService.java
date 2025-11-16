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

@Service
public class TripEventService {

    private final TripEventRepository repo;
    private final TripMembershipService membership; // <-- real bean

    public TripEventService(TripEventRepository repo, TripMembershipService membership) {
        this.repo = repo;
        this.membership = membership;
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
        // TODO: notification ITINERARY_UPDATE (created)
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
        // TODO: notification ITINERARY_UPDATE (edited)
        return TripEventMapper.toDTO(e);
    }

    @Transactional
    public void delete(Long me, Long tripId, Long eventId) {
        membership.ensureMember(me, tripId);

        TripEvent e = repo.findByIdAndTripIdAndDeletedAtIsNull(eventId, tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "event"));

        ensureOwnerOrCreator(me, tripId, e);

        e.setDeletedAt(Instant.now());
        // TODO: notification ITINERARY_UPDATE (deleted)
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
