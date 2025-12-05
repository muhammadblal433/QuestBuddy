package com.questbuddy.payments.controller;

import com.questbuddy.payments.dto.PaymentCreateDTO;
import com.questbuddy.payments.dto.PaymentReceiptDTO;
import com.questbuddy.payments.dto.PaymentResponseDTO;
import com.questbuddy.payments.service.StripePaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v14/payments")
public class PaymentController {

    private final StripePaymentService service;

    public PaymentController(StripePaymentService service) {
        this.service = service;
    }

    private Long requireUserId(String header) {
        if (header == null || header.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing_header_X-User-Id");
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_X-User-Id");
        }
    }

    /** Create PaymentIntent (client gets clientSecret). */
    @PostMapping("/intents")
    public PaymentResponseDTO create(@RequestHeader("X-User-Id") String userHeader,
                                     @Valid @RequestBody PaymentCreateDTO body) {
        Long me = requireUserId(userHeader);
        return service.createPaymentIntent(me, body);
    }

    /** Stripe webhook endpoint (no auth). */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String sig) {
        service.handleWebhook(payload, sig);
        return ResponseEntity.ok().build();
    }

    /** List receipts (mine by default, or by tripId if provided). */
    @GetMapping("/receipts")
    public Page<PaymentReceiptDTO> list(@RequestHeader("X-User-Id") String userHeader,
                                        @RequestParam(value = "tripId", required = false) Long tripId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "50") int size) {
        Long me = requireUserId(userHeader);
        if (tripId != null) {
            return service.listTrip(me, tripId, PageRequest.of(page, size));
        }
        return service.listMine(me, PageRequest.of(page, size));
    }

    /** Get single receipt. */
    @GetMapping("/receipts/{id}")
    public PaymentReceiptDTO get(@RequestHeader("X-User-Id") String userHeader,
                                 @PathVariable Long id) {
        Long me = requireUserId(userHeader);
        return service.getReceipt(me, id);
    }
}
