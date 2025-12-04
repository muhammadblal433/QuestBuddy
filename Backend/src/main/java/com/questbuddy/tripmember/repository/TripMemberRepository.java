package com.questbuddy.tripmember.repository;

import com.questbuddy.tripmember.model.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripMemberRepository extends JpaRepository<TripMember, Long> {
    Optional<TripMember> findByTrip_IdAndUser_Id(Long tripId, Long userId);

    boolean existsByTrip_IdAndUser_IdAndStatus(Long tripId, Long userId, TripMember.Status status);

    List<TripMember> findAllByTrip_IdAndStatusOrderByUser_IdAsc(Long tripId, TripMember.Status status);

    void deleteByTrip_IdAndUser_Id(Long tripId, Long userId);

    List<TripMember> findAllByUser_IdAndStatusOrderByTrip_IdAsc(Long userId, TripMember.Status status);
}
