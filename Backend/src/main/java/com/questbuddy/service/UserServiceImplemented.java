package com.questbuddy.service;

import com.questbuddy.model.Role;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserServiceImplemented implements UserService {

    private final UserRepository user_repo;
    private final PasswordEncoder encoder;

    public UserServiceImplemented(UserRepository users, PasswordEncoder encoder) {
        this.user_repo = users;
        this.encoder = encoder;
    }

    @Override
    public User signup(String email, String username, String password, String firstName, String lastName) {
        if (user_repo.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        } else if (user_repo.existsByUsername(username)) {
            throw new IllegalArgumentException("User with username " + username + " already exists");
        }

        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        if (firstName != null) {
            u.setFirstName(firstName);
        }
        if (lastName  != null) {
            u.setLastName(lastName);
        }
        u.setRole(Role.TRIP_MEMBER);
        u.setActive(true);
        return user_repo.save(u);
    }

    @Override
    public Optional<User> getById(Long id) {
        return user_repo.findById(id);
    }

    @Override
    public User updateProfile(Long id, String email, String username, String firstName, String lastName, String avatarUrl) {
        User u = user_repo.findById(id).orElseThrow(() -> new NoSuchElementException("No such user found."));

        if (email != null && !email.equalsIgnoreCase(u.getEmail())) {
            if (user_repo.existsByEmail(email)) throw new IllegalArgumentException("This email has been taken.");
            u.setEmail(email);
        }
        if (username != null && !username.equalsIgnoreCase(u.getUsername())) {
            if (user_repo.existsByUsername(username)) throw new IllegalArgumentException("This username has been taken.");
            u.setUsername(username);
        }
        if (firstName != null) {
            u.setFirstName(firstName);
        }
        if (lastName != null) {
            u.setLastName(lastName);
        }
        if (avatarUrl != null) {
            u.setAvatarUrl(avatarUrl);
        }

        return user_repo.save(u);
    }

    @Override
    public Optional<User> login(String email, String rawPassword) {
        return user_repo.findByEmailIgnoreCase(email).filter(u -> encoder.matches(rawPassword, u.getPasswordHash()));
    }
}
