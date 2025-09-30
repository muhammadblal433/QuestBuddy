package com.questbuddy.controller;

import com.questbuddy.model.User;
import com.questbuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    // POST -> create new user
    @PostMapping(value = "/auth/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String username = body.get("username");
        String password = body.get("password");       // raw; encryption alr handled in signup mtd
        String firstName = body.get("firstName");
        String lastName  = body.get("lastName");

        if (email == null || username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }

        User u = users.signup(email, username, password, firstName, lastName);
        return ResponseEntity.created(URI.create("/api/v1/users/" + u.getId())).body(u);
    }

    // GET -> fetch user by id
    @GetMapping(value = "/users/{id}", produces = "application/json")
    public ResponseEntity<User> get(@PathVariable Long id) {
        Optional<User> u = users.getById(id);
        return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    // PUT -> edit user profile
    @PutMapping(value = "/users/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String email     = body.get("email");
        String username  = body.get("username");
        String firstName = body.get("firstName");
        String lastName  = body.get("lastName");
        String avatarUrl = body.get("avatarUrl");

        User updated = users.updateProfile(id, email, username, firstName, lastName, avatarUrl);
        return ResponseEntity.ok(updated);
    }

    // GET - for testing purposes - add users in batches
    @PostMapping(value = "/auth/signup/batch", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signupBatch(@RequestBody java.util.List<java.util.Map<String, String>> bodies) {
        java.util.List<User> created = new java.util.ArrayList<>();
        for (var body : bodies) {
            String email = body.get("email");
            String username = body.get("username");
            String password = body.get("password");
            String firstName = body.get("firstName");
            String lastName  = body.get("lastName");
            if (email == null || username == null || password == null) {
                continue; // skip bad rows; keep it simple for dev
            }
            created.add(users.signup(email, username, password, firstName, lastName));
        }
        return ResponseEntity.created(URI.create("/api/v1/users")).body(created);
    }

}