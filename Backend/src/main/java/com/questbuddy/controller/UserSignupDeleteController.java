package com.questbuddy.controller;

import com.questbuddy.model.Role;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles basic signup, delete, and CRUD operations for testing/demo purposes.
 */
@RestController
@RequestMapping("/api/v2/users") // Different base path to avoid route conflicts
public class UserSignupDeleteController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder; // add this to properly encypt passwrd

    public UserSignupDeleteController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    // Data Transfer Object - hides raw password from being transferred
    public record UserDto(
            Long id, String email, String username,
            String firstName, String lastName, String avatarUrl,
            String role, boolean active, Instant createdAt, Instant updatedAt
    ) {}

    private static UserDto toDto(User u) {
        return new UserDto(
                u.getId(), u.getEmail(), u.getUsername(),
                u.getFirstName(), u.getLastName(), u.getAvatarUrl(),
                u.getRole() == null ? null : u.getRole().name(),
                Boolean.TRUE.equals(u.getActive()),
                u.getCreatedAt(), u.getUpdatedAt()
        );
    }

//    // POST - Signup user
//    // POST /api/v2/auth/signup  -> { "userId": 1 }
//    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
//    public ResponseEntity<?> signup(@RequestBody SignupRequest body) {
//        if (body == null || body.email() == null || body.username() == null || body.password() == null) {
//            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
//        }
//
//        // Duplicate checks -> 409 (avoid 500 on unique constraint)
//        if (users.findByEmail(body.email()).isPresent()) {
//            return ResponseEntity.status(409).body(Map.of("error", "email_exists"));
//        }
//        if (users.findByUsername(body.username()).isPresent()) {
//            return ResponseEntity.status(409).body(Map.of("error", "username_exists"));
//        }
//
//        // Map request -> entity (hash the password)
//        User u = new User();
//        u.setEmail(body.email());
//        u.setUsername(body.username());
//        u.setFirstName(body.firstName());
//        u.setLastName(body.lastName());
//        u.setAvatarUrl(body.avatarUrl());
//
//        // CRITICAL: set BOTH columns because DB has both NOT NULL
//        String hash = passwordEncoder.encode(body.password());
//        u.setPasswordHash(hash);  // maps to password_hash
//        u.setPassword(hash);      // maps to password (legacy NOT NULL column)
//
//        User saved = users.save(u);
//        return ResponseEntity.status(201).body(Map.of("userId", saved.getId()));
//    }

    // DELETE - Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepo.deleteById(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    // GET - All users
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        // List users w/o leaking their passwords
        List<UserDto> out = userRepo.findAll().stream().map(UserSignupDeleteController::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    // GET - User by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        // Get just the user as a DTO
        return userRepo.findById(id).map(UserSignupDeleteController::toDto)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // PUT - Update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        // return a new map of details (if update param is null -> dont do anything --> allows for partial update)
        return userRepo.findById(id).map(user -> {
            if (updated.getUsername() != null) user.setUsername(updated.getUsername());
            if (updated.getEmail() != null)    user.setEmail(updated.getEmail());
            if (updated.getFirstName() != null) user.setFirstName(updated.getFirstName());
            if (updated.getLastName() != null)  user.setLastName(updated.getLastName());
            if (updated.getAvatarUrl() != null) user.setAvatarUrl(updated.getAvatarUrl());
            if (updated.getRole() != null)      user.setRole(updated.getRole());
            if (updated.getActive() != null)    user.setActive(updated.getActive());

            if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
                String hash = encoder.encode(updated.getPassword());
                user.setPasswordHash(hash);
                user.setPassword(hash);
            }
            return ResponseEntity.ok(toDto(userRepo.save(user)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Health/test check to make sure that the file is being read
    @GetMapping("/ping")
    public String ping() {
        return "UserSignupDeleteController is alive!";
    }
}
