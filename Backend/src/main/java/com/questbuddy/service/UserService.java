package com.questbuddy.service;

import com.questbuddy.model.User;

import java.util.Optional;

public interface UserService {
    User signup(String email, String username, String password, String firstName, String lastName);

    Optional<User> getById(Long id);

    User updateProfile(Long id, String email, String username, String firstName, String lastName, String avatarUrl);

    Optional<User> login(String email, String rawPassword);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    User save(User user);
}