package com.questbuddy.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    // mtd to find all events by UserId between times from and to (ascending order)
    List<Event> findAllByUserIdAndStartAtBetween(
            Long userId, Instant from, Instant to, Sort sort);

    // mtd to find all events by UserId (ascending order)
    List<Event> findAllByUserId(Long userId, Sort sort);

    Optional<Event> findByIdAndUserId(Long id, Long userId);

    long deleteByIdAndUserId(Long id, Long userId);
}