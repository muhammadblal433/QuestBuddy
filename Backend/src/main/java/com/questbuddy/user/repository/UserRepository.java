package com.questbuddy.user.repository;

import com.questbuddy.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for User entities providing CRUD operations and common lookups.
 * Exposes finders by email and username (case-sensitive and case-insensitive variants).
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<User> findAllByIsPremiumTrueOrderByIdAsc();
}
