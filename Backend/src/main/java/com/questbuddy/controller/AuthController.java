package com.questbuddy.controller;

import com.questbuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller specifically for login - to deal with credentials and tokens
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService users;
    public AuthController(UserService users) { this.users = users; }

    // POST /api/v1/auth/login  -> { "token": "..." }
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String,String>> login(@RequestBody LoginRequest body) {
        String token = users.login(body.email(), body.password()); // throw 401 inside on bad creds
        return ResponseEntity.ok(Map.of("token", token));
    }

    public record LoginRequest(String email, String password) {}
}