package com.questbuddy.billing;

import com.questbuddy.user.model.User;
import com.questbuddy.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller that:
 *  - creates a Stripe Checkout session for Premium
 *  - marks a user as premium after success redirect
 */
@RestController
@RequestMapping("/api/v15/payments")
public class ProductCheckoutController {

    private final UserRepository userRepository;
    private final StripeService stripeService;

    private static final String SUCCESS_JSON = "{\"message\":\"success\"}";

    public ProductCheckoutController(UserRepository userRepository,
                                     StripeService stripeService) {
        this.userRepository = userRepository;
        this.stripeService = stripeService;
    }


    @GetMapping("/success/{userId}")
    public ResponseEntity<String> successEndpoint(@PathVariable("userId") long userId) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User id does not exist: " + userId));

        if (!existing.isPremiumUser()) {
            existing.setPremiumUser(true);
            userRepository.save(existing);
        }

        return ResponseEntity.ok(SUCCESS_JSON);
    }

    // Optional: cancel callback (Stripe cancelUrl)
    @GetMapping("/cancel")
    public ResponseEntity<String> cancelEndpoint() {
        return ResponseEntity.status(HttpStatus.OK)
                .body("{\"message\":\"payment_canceled\"}");
    }

    @PostMapping("/checkout/premium/{userId}")
    public ResponseEntity<StripeResponse> checkoutProducts(
            @PathVariable("userId") long userId,
            @RequestBody ProductRequest productRequest) {

        StripeResponse stripeResponse = stripeService.checkout(productRequest, userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }
}
