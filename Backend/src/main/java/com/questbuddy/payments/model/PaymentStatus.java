package com.questbuddy.payments.model;

public enum PaymentStatus {
    CREATED,
    REQUIRES_PAYMENT_METHOD,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

