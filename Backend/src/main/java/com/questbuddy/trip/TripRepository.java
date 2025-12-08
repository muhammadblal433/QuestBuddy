package com.questbuddy.trip;

import com.questbuddy.tripmember.model.TripMember;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    // list all trips owned by a user, sorted based on whatever ordering is in the parameter
    List<Trip> findAllByOwnerId(Long ownerId, Sort sort);

    // get a singular trip only if the caller is the owner
    Optional<Trip> findByIdAndOwnerId(Long id, Long ownerId);

    // delete only if the caller is the owner; return number of trips deleted (0/1)
    long deleteByIdAndOwnerId(Long id, Long ownerId);

    // Trips where the user is an ACCEPTED MEMBER
    @Query("""
    select distinct t
    from Trip t
    join TripMember tm on tm.trip = t
    where tm.user.id = :userId
      and tm.status = :status
""")
    List<Trip> findAllForUserAsMemberWithStatus(
            @Param("userId") Long userId,
            @Param("status") TripMember.Status status
    );
}