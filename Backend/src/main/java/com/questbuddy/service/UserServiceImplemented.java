package com.questbuddy.service;

import com.questbuddy.model.Role;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public User signup(String email, String username, String password, String firstName, String lastName) {
        String e = email == null ? null : email.trim();
        String u = username == null ? null : username.trim();
        if (e == null || e.isBlank()) throw new IllegalArgumentException("email required");
        if (u == null || u.isBlank()) throw new IllegalArgumentException("username required");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password required");

        if (user_repo.existsByEmail(e)) {
            throw new IllegalArgumentException("User with email " + e + " already exists");
        } else if (user_repo.existsByUsername(u)) {
            throw new IllegalArgumentException("User with username " + u + " already exists");
        }

        User x = new User();
        x.setEmail(e);
        x.setUsername(u);
        String hash = encoder.encode(password);
        x.setPasswordHash(hash);          // <-- required by DB
        x.setPassword(hash);              // <-- required by DB (legacy NOT NULL column)

        if (firstName != null) x.setFirstName(firstName);
        if (lastName  != null) x.setLastName(lastName);
        x.setRole(Role.TRIP_MEMBER);
        x.setActive(true);
        return user_repo.save(x);
    }


    @Override
    public Optional<User> getById(Long id) {
        return user_repo.findById(id);
    }

    @Override
    @Transactional
    public User updateProfile(Long id, String email, String username, String firstName, String lastName, String avatarUrl) {
        User x = user_repo.findById(id).orElseThrow(() -> new NoSuchElementException("No such user found."));

        if (email != null) {
            String e = email.trim();
            if (!e.equalsIgnoreCase(x.getEmail())) {
                if (user_repo.existsByEmail(e)) throw new IllegalArgumentException("This email has been taken.");
                x.setEmail(e);
            }
        }
        if (username != null) {
            String u = username.trim();
            if (!u.equalsIgnoreCase(x.getUsername())) {
                if (user_repo.existsByUsername(u)) throw new IllegalArgumentException("This username has been taken.");
                x.setUsername(u);
            }
        }
        if (firstName != null) x.setFirstName(firstName);
        if (lastName  != null) x.setLastName(lastName);
        if (avatarUrl != null) x.setAvatarUrl(avatarUrl);

        return user_repo.save(x);
    }

    @Override
    public Optional<User> login(String email, String rawPassword) {
        if (email == null || rawPassword == null) return Optional.empty();
        return user_repo.findByEmailIgnoreCase(email.trim())
                .filter(u -> encoder.matches(rawPassword, u.getPasswordHash()));
    }

    // ---- Methods required by your UserService interface ----

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return user_repo.findByEmailIgnoreCase(email.trim());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return user_repo.findByUsernameIgnoreCase(username.trim());
    }

    @Override
    @Transactional
    public User save(User user) {
        return user_repo.save(user);
    }
}
