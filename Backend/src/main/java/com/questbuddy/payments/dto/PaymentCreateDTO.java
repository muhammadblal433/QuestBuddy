package com.questbuddy.payments.dto;

import java.math.BigDecimal;

import com.stripe.model.PaymentIntent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// create a PaymentIntent .
public class PaymentCreateDTO {

    /** Dollar amount like 10.99 (server converts to minor units). */
    @NotNull @Positive
    private BigDecimal amount;

    private String currency;

    private Long tripId;

    private String description;

    // getters & setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
