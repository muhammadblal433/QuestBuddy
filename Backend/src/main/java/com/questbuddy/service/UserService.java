package com.questbuddy.service;

import com.questbuddy.model.User;

import java.util.Optional;

/**
 * Service boundary for user-related operations.
 * Defines registration, authentication, profile updates, lookups, and persistence helpers.
 * Implementations should handle validation, password hashing, and transactional consistency.
 */
public interface UserService {
    User signup(String email, String username, String password, String firstName, String lastName);

    Optional<User> getById(Long id);

    User updateProfile(Long id, String email, String username, String firstName, String lastName, String avatarUrl);

    Optional<User> login(String email, String rawPassword);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    User save(User user);
}