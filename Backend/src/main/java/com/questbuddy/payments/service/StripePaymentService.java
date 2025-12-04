package com.questbuddy.payments.service;

import com.questbuddy.payments.config.StripeProperties;
import com.questbuddy.payments.dto.PaymentCreateDTO;
import com.questbuddy.payments.dto.PaymentResponseDTO;
import com.questbuddy.payments.dto.PaymentReceiptDTO;
import com.questbuddy.payments.model.Payment;
import com.questbuddy.payments.model.PaymentStatus;
import com.questbuddy.payments.repository.PaymentRepository;
import com.questbuddy.tripmember.security.TripMembershipGate;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

/**
 *  Handling main logic of payments
 *
 *  Note: Keeping track of all payments in DB even if stripe fails
 */
@Service
public class StripePaymentService {

    private final PaymentRepository payments;
    private final TripMembershipGate membership;
    private final StripeProperties props;

    public StripePaymentService(PaymentRepository payments,
                                TripMembershipGate membership,
                                StripeProperties props) {
        this.payments = payments;
        this.membership = membership;
        this.props = props;
    }

    private static final Map<String, Integer> MINOR_UNITS = Map.ofEntries(
            Map.entry("usd", 2), Map.entry("eur", 2), Map.entry("gbp", 2),
            Map.entry("cad", 2), Map.entry("aud", 2),
            Map.entry("jpy", 0), Map.entry("krw", 0),
            Map.entry("bhd", 3), Map.entry("jod", 3), Map.entry("kwd", 3), Map.entry("omr", 3)
    );

    private long toMinor(String currency, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_amount");
        }
        String cur = (currency == null || currency.isBlank()) ? "usd" : currency.toLowerCase();
        int scale = MINOR_UNITS.getOrDefault(cur, 2);
        BigDecimal normalized = amount.setScale(scale, RoundingMode.HALF_UP);
        return normalized.movePointRight(scale).longValueExact();
    }

    private String prettyAmount(String currency, long minor) {
        String cur = (currency == null || currency.isBlank()) ? "usd" : currency.toLowerCase();
        int scale = MINOR_UNITS.getOrDefault(cur, 2);
        BigDecimal dec = new BigDecimal(minor).movePointLeft(scale);
        return dec.setScale(scale, RoundingMode.UNNECESSARY).toPlainString();
    }

    private PaymentStatus toStatus(String stripeStatus) {
        if (stripeStatus == null) return PaymentStatus.CREATED;
        return switch (stripeStatus) {
            case "requires_payment_method" -> PaymentStatus.REQUIRES_PAYMENT_METHOD;
            case "processing" -> PaymentStatus.PROCESSING;
            case "succeeded" -> PaymentStatus.SUCCEEDED;
            case "canceled" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.CREATED;
        };
    }

    private PaymentReceiptDTO toReceipt(Payment p) {
        return new PaymentReceiptDTO(
                p.getId(),
                p.getAmountCents(),
                prettyAmount(p.getCurrency(), p.getAmountCents()),
                p.getCurrency(),
                p.getDescription(),
                p.getUserId(),
                p.getTripId(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    /** Create PaymentIntent (client sends dollars) */
    @Transactional
    public PaymentResponseDTO createPaymentIntent(Long me, PaymentCreateDTO req) {
        String currency = (req.getCurrency() == null || req.getCurrency().isBlank())
                ? "usd" : req.getCurrency().toLowerCase();

        if (req.getTripId() != null && !membership.isMember(req.getTripId(), me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not_member");
        }

        long minor = toMinor(currency, req.getAmount());

        // Persist first (so if Stripe call fails we still have a record)
        Payment p = new Payment();
        p.setAmountCents(minor);
        p.setCurrency(currency);
        p.setDescription(req.getDescription());
        p.setUserId(me);
        p.setTripId(req.getTripId());
        p = payments.save(p);

        // Stripe call
        Stripe.apiKey = props.getSecretKey();
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(minor)
                .setCurrency(currency)
                .setDescription(Optional.ofNullable(req.getDescription()).orElse("QuestBuddy payment"))
                .putAllMetadata(Map.of(
                        "paymentId", String.valueOf(p.getId()),
                        "userId", String.valueOf(me),
                        "tripId", String.valueOf(req.getTripId() == null ? 0 : req.getTripId())
                ))
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();

        try {
            PaymentIntent pi = PaymentIntent.create(params);
            p.setStripePaymentIntentId(pi.getId());
            p.setStatus(toStatus(pi.getStatus()));
            payments.save(p);
            return new PaymentResponseDTO(p.getId(), pi.getId(), pi.getClientSecret());
        } catch (StripeException e) {
            p.setStatus(PaymentStatus.FAILED);
            p.setLastErrorMessage(e.getMessage());
            payments.save(p);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "stripe_error");
        }
    }

    /** update local Payment status based on PaymentIntent events. */
    @Transactional
    public void handleWebhook(String payload, String stripeSigHeader) {
        if (props.getWebhookSecret() == null || props.getWebhookSecret().isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "webhook_not_configured");
        }
        final Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSigHeader, props.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad_signature");
        }

        String type = event.getType();
        var deser = event.getDataObjectDeserializer();

        deser.getObject().ifPresent(obj -> {
            if (obj instanceof PaymentIntent pi) {
                payments.findByStripePaymentIntentId(pi.getId()).ifPresent(p -> {
                    switch (type) {
                        case "payment_intent.succeeded" -> p.setStatus(PaymentStatus.SUCCEEDED);
                        case "payment_intent.payment_failed" -> {
                            p.setStatus(PaymentStatus.FAILED);
                            if (pi.getLastPaymentError() != null) {
                                p.setLastErrorCode(pi.getLastPaymentError().getCode());
                                p.setLastErrorMessage(pi.getLastPaymentError().getMessage());
                            }
                        }
                        case "payment_intent.canceled" -> p.setStatus(PaymentStatus.CANCELED);
                        default -> { /* ignore other events */ }
                    }
                    payments.save(p);
                });
            }
        });
    }

    @Transactional(readOnly = true)
    public Page<PaymentReceiptDTO> listMine(Long me, Pageable pageable) {
        return payments.findAllByUserIdOrderByCreatedAtDesc(me, pageable).map(this::toReceipt);
    }

    @Transactional(readOnly = true)
    public Page<PaymentReceiptDTO> listTrip(Long me, Long tripId, Pageable pageable) {
        if (!membership.isMember(tripId, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not_member");
        }
        return payments.findAllByTripIdOrderByCreatedAtDesc(tripId, pageable).map(this::toReceipt);
    }

    @Transactional(readOnly = true)
    public PaymentReceiptDTO getReceipt(Long me, Long id) {
        Payment p = payments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not_found"));
        boolean allowed = p.getUserId().equals(me)
                || (p.getTripId() != null && membership.isMember(p.getTripId(), me));
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        return toReceipt(p);
    }
}
