package com.questbuddy.messages.trip.repository;

import com.questbuddy.messages.trip.model.TripMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripMessageRepository extends JpaRepository<TripMessage, Long> {

    // Idempotency lookup
    Optional<TripMessage> findBySenderIdAndClientMessageId(Long senderId, String clientMessageId);

    // List by Id - most recent 50 messages (as messages grow large in terms o fnumber - nt practical to list more)
    List<TripMessage> findTop50ByTripIdOrderByIdDesc(Long tripId);

    // List by Id - the 50 messages right before the given beforeId
    List<TripMessage> findTop50ByTripIdAndIdLessThanOrderByIdDesc(Long tripId, Long beforeId);

    // helpers
    boolean existsByIdAndTripId(Long id, Long tripId);
    Optional<TripMessage> findByIdAndTripId(Long id, Long tripId);
}
