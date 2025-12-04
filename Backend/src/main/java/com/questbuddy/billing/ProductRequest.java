package com.questbuddy.billing;

/**
 * Simple request to configure a Stripe Checkout session.
 */
public class ProductRequest {

    private Long amount;
    private Long quantity;
    private String productName;
    private String currency;

    public ProductRequest() {}

    public ProductRequest(Long amount, Long quantity, String productName, String currency) {
        this.amount = amount;
        this.quantity = quantity;
        this.productName = productName;
        this.currency = currency;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
