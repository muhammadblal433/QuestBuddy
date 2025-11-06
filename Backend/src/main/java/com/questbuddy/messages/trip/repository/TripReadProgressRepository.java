package com.questbuddy.messages.trip.repository;

import com.questbuddy.messages.trip.model.TripReadProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** This repo is for per-user read receipts progress in a trip. */
public interface TripReadProgressRepository extends JpaRepository<TripReadProgress, Long> {
    Optional<TripReadProgress> findByTripIdAndUserId(Long tripId, Long userId);
}
