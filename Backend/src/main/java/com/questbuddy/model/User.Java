package com.questbuddy.model;

import java.time.Instant;
import java.util.Objects;

/**
 * User Class for app
 * In-memory User Model
 */

package com.questbuddy.model;

import java.time.Instant;
import java.util.Objects;

/**
 * In-memory User model for Issue C (Login & Edit).
 * Mirrors Demo1 style: plain POJO with simple validation in setters.
 */
public class User {

    // Minimal Variables - cannot be empty
    private Long id;                // server-assigned
    private String email;           // required, unique (case-insensitive)
    private String username;        // required, unique (case-insensitive), 3-32
    private String passwordHash;    // need to use something called BCrypt hash for passwords?

    // Optional Variables - can have no value
    private String firstName;
    private String lastName;
    private String avatarUrl;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    /** Default constructor. */
    public User() {}

    /** Updates the last-modified timestamp for this user. */
    private void touch() {
        this.updatedAt = Instant.now();
    }

    /** @return the server-assigned unique id of the user. */
    public Long getId() {
        return id;
    }

    /**
     * Sets the server-assigned unique id of the user.
     * Updates the last-modified timestamp.
     * @param id the new id value
     */
    public void setId(Long id) {
        this.id = id;
        touch();
    }

    /** @return the user's email address (required, unique). */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address. Must be non-blank and contain '@'
     * not at the start or end. Trims surrounding whitespace.
     * Also updates the last-modified timestamp.
     * @param email the email to set
     * @throws IllegalArgumentException if invalid
     */
    public void setEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email required");
        } else if (email.isBlank()) {
            throw new IllegalArgumentException("email required");
        } else if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new IllegalArgumentException("email format invalid");
        } else {
            this.email = email.trim();
            touch();
        }
    }

    /** @return the username (required, unique, length 3..32). */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username. Must be non-blank and length 3..32 after trim.
     * Also updates the last-modified timestamp.
     * @param username the username to set
     * @throws IllegalArgumentException if invalid
     */
    public void setUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("username required");
        } else if (username.isBlank()) {
            throw new IllegalArgumentException("username required");
        } else {
            String u = username.trim();
            if (u.length() < 3 || u.length() > 32) {
                throw new IllegalArgumentException("username length must be 3-32 characters long");
            } else {
                this.username = u;
                touch();
            }
        }
    }

    /** @return the stored BCrypt password hash */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the stored BCrypt password hash. Must be non-blank.
     * Also updates the last-modified timestamp.
     * @param passwordHash the BCrypt hash to store
     * @throws IllegalArgumentException if blank or null
     */
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null) {
            throw new IllegalArgumentException("passwordHash required");
        } else if (passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash required");
        } else {
            this.passwordHash = passwordHash;
            touch();
        }
    }

    /** @return the user's first name (optional). */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name; trims whitespace.
     * If blank or null, clears the value (sets to null).
     * Also updates the last-modified timestamp.
     * @param firstName the first name, or null/blank to clear
     */
    public void setFirstName(String firstName) {
        if (firstName == null) {
            this.firstName = null;
        } else if (firstName.isBlank()) {
            this.firstName = null;
        } else {
            this.firstName = firstName.trim();
        }
        touch();
    }

    /** @return the user's last name (optional). */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name; trims whitespace.
     * If blank or null, clears the value (sets to null).
     * Also updates the last-modified timestamp.
     * @param lastName the last name, or null/blank to clear
     */
    public void setLastName(String lastName) {
        if (lastName == null) {
            this.lastName = null;
        } else if (lastName.isBlank()) {
            this.lastName = null;
        } else {
            this.lastName = lastName.trim();
        }
        touch();
    }

    /** @return the avatar URL (optional). */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Sets the avatar URL; trims whitespace.
     * If blank or null, clears the value (sets to null).
     * Also updates the last-modified timestamp.
     * @param avatarUrl the URL, or null/blank to clear
     */
    public void setAvatarUrl(String avatarUrl) {
        if (avatarUrl == null) {
            this.avatarUrl = null;
        } else if (avatarUrl.isBlank()) {
            this.avatarUrl = null;
        } else {
            this.avatarUrl = avatarUrl.trim();
        }
        touch();
    }

    /** @return the creation timestamp. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp. If null, sets to now.
     * Also updates the last-modified timestamp.
     * @param createdAt the timestamp to set, or null for now
     */
    public void setCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            this.createdAt = Instant.now();
        } else {
            this.createdAt = createdAt;
        }
        touch();
    }

    /** @return the last-modified timestamp. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last-modified timestamp. If null, sets to now.
     * (Usually you don't set this manually; setters call touch().)
     * @param updatedAt the timestamp to set, or null for now
     */
    public void setUpdatedAt(Instant updatedAt) {
        if (updatedAt == null) {
            this.updatedAt = Instant.now();
        } else {
            this.updatedAt = updatedAt;
        }
    }
}
