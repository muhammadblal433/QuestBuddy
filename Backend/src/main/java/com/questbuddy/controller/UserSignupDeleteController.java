package com.questbuddy.controller;

import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles basic signup, delete, and CRUD operations for testing/demo purposes.
 */
@RestController
@RequestMapping("/api/v2/users") // Different base path to avoid route conflicts
public class UserSignupDeleteController {

    private final UserRepository userRepo;

    public UserSignupDeleteController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // POST - Signup user
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User newUser) {
        if (userRepo.existsByEmail(newUser.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use!");
        }
        if (userRepo.existsByUsername(newUser.getUsername())) {
            return ResponseEntity.badRequest().body("Username already in use!");
        }

        // Autofill passwordHash if it is missing
        if (newUser.getPassword() != null && newUser.getPasswordHash() == null) {
            newUser.setPasswordHash(newUser.getPassword()); // temp placeholder
        }

        User savedUser = userRepo.save(newUser);
        return ResponseEntity.ok(savedUser);
    }

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
        try {
            List<User> users = userRepo.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace(); // logs the issue
            return ResponseEntity.internalServerError().body("Error fetching users: " + e.getMessage());
        }
    }

    // GET - User by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT - Update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userRepo.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    user.setPassword(updatedUser.getPassword());
                    user.setRole(updatedUser.getRole());
                    userRepo.save(user);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Health/test check to make sure that the file is being read
    @GetMapping("/ping")
    public String ping() {
        return "UserSignupDeleteController is alive!";
    }
}
