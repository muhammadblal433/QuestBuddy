package com.questbuddy.payments.dto;

import com.questbuddy.payments.model.PaymentStatus;
import java.time.Instant;

// Used w/ GET req. to "see" transaction
public class PaymentReceiptDTO {
    private Long id;

    private Long amountCents;

    private String amount;

    private String currency;
    private String description;
    private Long userId;
    private Long tripId;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public PaymentReceiptDTO() {}

    public PaymentReceiptDTO(Long id, Long amountCents, String amount, String currency, String description,
                             Long userId, Long tripId, PaymentStatus status,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.amountCents = amountCents;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.userId = userId;
        this.tripId = tripId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
