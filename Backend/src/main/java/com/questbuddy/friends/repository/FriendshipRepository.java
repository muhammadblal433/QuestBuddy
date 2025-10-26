package com.questbuddy.friends.repository;

import com.questbuddy.friends.model.Friendship;
import com.questbuddy.friends.model.FriendshipStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUser_IdAndFriend_Id(Long userId, Long friendId);

    @Query("""
           select f from Friendship f
           where (f.user.id = :userId or f.friend.id = :userId)
             and f.status = 'ACCEPTED'
           """)
    List<Friendship> findAllAcceptedForUser(@Param("userId") Long userId);

    @Query("""
           select f from Friendship f
           where f.friend.id = :userId and f.status = 'PENDING'
           """)
    List<Friendship> findIncomingRequests(@Param("userId") Long userId);

    @Query("""
           select f from Friendship f
           where f.user.id = :userId and f.status = 'PENDING'
           """)
    List<Friendship> findOutgoingRequests(@Param("userId") Long userId);

    @Query("""
           select f from Friendship f
           where (f.user.id = :a and f.friend.id = :b)
              or (f.user.id = :b and f.friend.id = :a)
           """)
    Optional<Friendship> findEitherDirection(@Param("a") Long a, @Param("b") Long b);

    @Query("""
           select case when count(f) > 0 then true else false end
           from Friendship f
           where ((f.user.id = :a and f.friend.id = :b)
               or (f.user.id = :b and f.friend.id = :a))
             and f.status = 'BLOCKED'
           """)
    boolean existsBlockedEitherDirection(@Param("a") Long a, @Param("b") Long b);

    List<Friendship> findByUser_IdInAndStatus(Collection<Long> userIds, FriendshipStatus status);

    // -------- Added for symmetric suggestions (no hard-coded users) --------

    /** All ACCEPTED friendships where either endpoint is in the provided set (symmetric). */
    @Query("""
           select f from Friendship f
           where (f.user.id in :userIds or f.friend.id in :userIds)
             and f.status = 'ACCEPTED'
           """)
    List<Friendship> findAllAcceptedForUsers(@Param("userIds") Collection<Long> userIds);

    /** All relations (ANY status) that involve this user, either direction (used for exclusions). */
    @Query("""
           select f from Friendship f
           where (f.user.id = :meId or f.friend.id = :meId)
           """)
    List<Friendship> findAllRelationsAnyStatus(@Param("meId") Long meId);
}
