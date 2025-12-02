package com.questbuddy.billing;


import com.questbuddy.billing.ProductRequest;
import com.questbuddy.billing.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    private static final String SERVER_BASE =
            "http://coms-3090-026.class.las.iastate.edu:8080";

    private static final String SUCCESS_URL_BASE =
            SERVER_BASE + "/api/v15/payments/success/";

    private static final String CANCEL_URL =
            SERVER_BASE + "/api/v15/payments/cancel";

    public StripeResponse checkout(ProductRequest productRequest, long userId) {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(
                                productRequest.getProductName() == null
                                        ? "QuestBuddy Premium"
                                        : productRequest.getProductName()
                        )
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(
                                productRequest.getCurrency() == null
                                        ? "usd"
                                        : productRequest.getCurrency()
                        )
                        .setUnitAmount(
                                productRequest.getAmount() == null
                                        ? 399L    // $3.99 default
                                        : productRequest.getAmount()
                        )
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(
                                productRequest.getQuantity() == null
                                        ? 1L
                                        : productRequest.getQuantity()
                        )
                        .setPriceData(priceData)
                        .build();

        String successUrlForUser = SUCCESS_URL_BASE + userId;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrlForUser)
                .setCancelUrl(CANCEL_URL)
                .addLineItem(lineItem)
                .build();

        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            e.printStackTrace();
            return new StripeResponse(
                    "error",
                    "Failed to create Stripe session: " + e.getMessage(),
                    null,
                    null
            );
        }
        return new StripeResponse(
                "success",
                "Payment session created.",
                session.getId(),
                session.getUrl()
        );
    }
}
