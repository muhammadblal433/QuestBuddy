package com.questbuddy.payments.dto;

public class PaymentResponseDTO {
    private Long paymentId;
    private String paymentIntentId;
    private String clientSecret;

    public PaymentResponseDTO() {}
    public PaymentResponseDTO(Long pid, String pi, String secret) {
        this.paymentId = pid;
        this.paymentIntentId = pi;
        this.clientSecret = secret;
    }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
}
