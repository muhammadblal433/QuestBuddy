package com.questbuddy.payments.repository;

import com.questbuddy.payments.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Find payment by payment id
    Optional<Payment> findByStripePaymentIntentId(String piId);

    // List all payments related to a user id
    Page<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // List all payments related to a trip id
    Page<Payment> findAllByTripIdOrderByCreatedAtDesc(Long tripId, Pageable pageable);
}