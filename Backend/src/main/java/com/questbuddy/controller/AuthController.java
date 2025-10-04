package com.questbuddy.controller;

import com.questbuddy.model.User;
import com.questbuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder; // <-- add
import java.util.Map;
import java.util.Optional;

/**
 * Controller specifically for login - to deal with credentials and tokens
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService users;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public record LoginRequest(String email, String password) {}
    public record SignupRequest(String email, String username, String password,
                                String firstName, String lastName, String avatarUrl) {}

    // POST /api/v1/auth/login  -> { "userId": 1 }
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        if (body == null || body.email() == null || body.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }

        // If your UserService already checks BCrypt, keep using it:
        Optional<User> opt = users.login(body.email(), body.password());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }

        User u = opt.get();
        // Return userId so the client can identify itself on subsequent calls
        return ResponseEntity.ok(Map.of("userId", u.getId()));
    }

    // POST /api/v1/auth/signup  -> { "userId": 1 }
    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signup(@RequestBody SignupRequest body) {
        if (body == null || body.email() == null || body.username() == null || body.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }

        // Duplicate checks -> 409 (avoid 500 on unique constraint)
        if (users.findByEmail(body.email()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "email_exists"));
        }
        if (users.findByUsername(body.username()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "username_exists"));
        }

        // Map request -> entity (hash the password)
        User u = new User();
        u.setEmail(body.email());
        u.setUsername(body.username());
        u.setFirstName(body.firstName());
        u.setLastName(body.lastName());
        u.setAvatarUrl(body.avatarUrl());

        // CRITICAL: set BOTH columns because DB has both NOT NULL
        String hash = passwordEncoder.encode(body.password());
        u.setPasswordHash(hash);  // maps to password_hash
        u.setPassword(hash);      // maps to password (legacy NOT NULL column)

        User saved = users.save(u);
        return ResponseEntity.status(201).body(Map.of("userId", saved.getId()));
    }
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> onIntegrity(org.springframework.dao.DataIntegrityViolationException e) {
        Throwable root = (e.getMostSpecificCause() != null) ? e.getMostSpecificCause() : e;
        return ResponseEntity.status(409).body(java.util.Map.of(
                "error", "constraint_violation",
                "message", root.getMessage()  // helps us see the exact column/constraint
        ));
    }
}
