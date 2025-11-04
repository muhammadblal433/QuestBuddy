package com.questbuddy.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // list all notifications based on recipient - ordered by createdAt
    List<Notification> findAllByRecipient_IdOrderByCreatedAtDesc(Long recipientId);

    // list all notifications that are unread based on recipient - ordered by createdAt
    List<Notification> findAllByRecipient_IdAndIsReadOrderByCreatedAtDesc(Long recipientId, boolean isRead);

    // return a numerical value that represents the count of how many unread notifications there are for user
    long countByRecipient_IdAndIsReadFalse(Long recipientId);

    // Helpers that treat NULL as unread (optional, use if there are legacy NULLs)
    @Query("""
           select n from Notification n
           where n.recipient.id = :recipientId
             and (n.isRead = false or n.isRead is null)
           order by n.createdAt desc
           """)
    List<Notification> findUnreadIncludingNull(@Param("recipientId") Long recipientId);

    @Query("""
           select count(n) from Notification n
           where n.recipient.id = :recipientId
             and (n.isRead = false or n.isRead is null)
           """)
    long countUnreadIncludingNull(@Param("recipientId") Long recipientId);

    // Lookup constrained to the intended recipient (useful for mark-as-read authorization)
    Optional<Notification> findByIdAndRecipient_Id(Long id, Long recipientId);

    // Existence check constrained to recipient (quick authorization guard)
    boolean existsByIdAndRecipient_Id(Long id, Long recipientId);
}