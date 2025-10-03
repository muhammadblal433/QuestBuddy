package com.questbuddy.controller;

import com.questbuddy.model.User;
import com.questbuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller specifically for login - to deal with credentials and tokens
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService users;
    public AuthController(UserService users) { this.users = users; }

    public record LoginRequest(String email, String password) {}

    // POST /api/v1/auth/login  -> { "userId": 1 }
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        if (body == null || body.email() == null || body.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }

        Optional<User> opt = users.login(body.email(), body.password());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }

        User u = opt.get();
        // Return userId so the client can identify itself on subsequent calls
        return ResponseEntity.ok(Map.of("userId", u.getId()));
    }
}