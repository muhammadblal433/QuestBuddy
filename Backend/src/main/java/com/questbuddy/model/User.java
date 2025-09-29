package com.questbuddy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role = "MEMBER";  // default

    // Constructors
    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters & Setters
    public Long getUserId() {
        return userId; }

    public void setUserId(Long userId) {
        this.userId = userId; }

    public String getUsername() {
        return username; }

    public void setUsername(String username) {
        this.username = username; }

    public String getEmail() {
        return email; }

    public void setEmail(String email) {
        this.email = email; }

    public String getPassword() {
        return password; }

    public void setPassword(String password) {
        this.password = password; }

    public String getRole() {
        return role; }

    public void setRole(String role) {
        this.role = role; }
}
