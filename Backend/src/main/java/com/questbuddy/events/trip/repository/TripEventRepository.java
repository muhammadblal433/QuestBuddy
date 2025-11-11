package com.questbuddy.events.trip.repository;

import com.questbuddy.events.trip.model.TripEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface TripEventRepository extends JpaRepository<TripEvent, Long> {

    Page<TripEvent> findByTripIdAndDeletedAtIsNullOrderByStartsAtAscIdAsc(
            Long tripId, Pageable pageable
    );

    // between two pointers
    Page<TripEvent> findByTripIdAndDeletedAtIsNullAndStartsAtBetweenOrderByStartsAtAscIdAsc(
            Long tripId, Instant from, Instant to, Pageable pageable
    );

    // List of deleted events
    Optional<TripEvent> findByIdAndTripIdAndDeletedAtIsNull(Long id, Long tripId);
}
