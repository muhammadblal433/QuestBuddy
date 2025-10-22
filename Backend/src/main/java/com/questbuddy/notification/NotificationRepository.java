package com.questbuddy.repository;

import com.questbuddy.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // list all notifications based on recipient - ordered by createdAt
    List<Notification> findAllByRecipient_IdOrderByCreatedAtDesc(Long recipientId);

    // list all notifications that are unread based on recipient - ordered by createdAt
    List<Notification> findAllByRecipient_IdAndIsReadOrderByCreatedAtDesc(Long recipientId, boolean isRead);

    // return a numerical value that represents the count of how many unread notifications there are for user
    long countByRecipient_IdAndIsReadFalse(Long recipientId);
}