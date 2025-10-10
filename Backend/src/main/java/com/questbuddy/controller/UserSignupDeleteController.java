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
@RequestMapping("/api/v2/users") // separate base path to avoid conflicts with /api/v1 auth/profile routes
public class UserSignupDeleteController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder; // this is to properly encypt passwrd

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

    // Centralized mapping from entity, goes to DTO (keeps controller responses consistent)
    private static UserDto toDto(User u) {
        return new UserDto(
                u.getId(), u.getEmail(), u.getUsername(),
                u.getFirstName(), u.getLastName(), u.getAvatarUrl(),
                u.getRole() == null ? null : u.getRole().name(),
                Boolean.TRUE.equals(u.isActive()),
                u.getCreatedAt(), u.getUpdatedAt()
        );
    }

    // POST - Signup user - refer to AuthController.java. // POST /api/v1/auth/signup

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

    // Health/test check to make sure that the file is being read
    @GetMapping("/ping")
    public String ping() {
        return "UserSignupDeleteController is alive!";
    }
}
