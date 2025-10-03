package com.questbuddy.controller;

import com.questbuddy.model.User;
import com.questbuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.net.URI;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    // Make Data Transfer Object  - excluding password
    public record UserDto(Long id, String email, String username,
                          String firstName, String lastName, String avatarUrl,
                          String role, boolean active,
                          java.time.Instant createdAt, java.time.Instant updatedAt) {}

    private static UserDto toDto(User u) {
        return new UserDto(
                u.getId(), u.getEmail(), u.getUsername(),
                u.getFirstName(), u.getLastName(), u.getAvatarUrl(),
                u.getRole().name(), u.isActive(),
                u.getCreatedAt(), u.getUpdatedAt()
        );
    }

    public record SignupRequest(String email, String username, String password,
                                String firstName, String lastName) {}

    public record UpdateProfileRequest(String email, String username,
                                       String firstName, String lastName,
                                       String avatarUrl, String newPassword) {}

    // POST -> create new user  (dev helper; OK to remove when teammate owns signup)
    @PostMapping(value = "/auth/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String username = body.get("username");
        String password = body.get("password");       // raw; encryption handled in service
        String firstName = body.get("firstName");
        String lastName  = body.get("lastName");

        if (email == null || username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }

        User u = users.signup(email, username, password, firstName, lastName);
        return ResponseEntity.created(URI.create("/api/v1/users/" + u.getId())).body(toDto(u));
    }

    // GET -> fetch user by id
    @GetMapping(value = "/users/{id}", produces = "application/json")
    public ResponseEntity<UserDto> get(@PathVariable Long id) {
        Optional<User> opt = users.getById(id);
        if (opt.isPresent()) {
            User u = opt.get();
            UserDto dto = toDto(u);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT -> edit user profile (path version; forbid if header doesn’t match)
    @PutMapping(value = "/users/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @RequestBody UpdateProfileRequest body) {

        if (userId == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "missing_user");
            return ResponseEntity.status(401).body(err);
        }
        if (!id.equals(userId)) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "forbidden");
            return ResponseEntity.status(403).body(err);
        }

        User updated = users.updateProfile(
                id,
                body.email(),
                body.username(),
                body.firstName(),
                body.lastName(),
                body.avatarUrl()
        );

        UserDto dto = toDto(updated);
        return ResponseEntity.ok(dto);
    }

    // POST - for testing purposes - add users in batches
    @PostMapping(value = "/auth/signup/batch", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<UserDto>> signupBatch(@RequestBody List<SignupRequest> bodies) {

        List<UserDto> created = new ArrayList<>();

        for (SignupRequest b : bodies) {
            boolean hasRequired = b != null
                    && b.email() != null
                    && b.username() != null
                    && b.password() != null;

            if (hasRequired) {
                User u = users.signup(
                        b.email(),
                        b.username(),
                        b.password(),
                        b.firstName(),
                        b.lastName()
                );
                UserDto dto = toDto(u);
                created.add(dto);
            }
        }

        URI location = URI.create("/api/v1/users");
        return ResponseEntity.created(location).body(created);
    }

    // GET - current profile (that is logged in)  — header-based auth for mini-assignment
    @GetMapping(value = "/users/me", produces = "application/json")
    public ResponseEntity<?> me(@RequestHeader(value = "X-User-Id", required = false) Long userId) {

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error","missing_user"));
        }

        Optional<User> opt = users.getById(userId);
        if (opt.isPresent()) {
            User u = opt.get();
            UserDto dto = toDto(u);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT -> update currently logged in profile  — header-based auth for mini-assignment
    @PutMapping(value = "/users/me", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateMe(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                      @RequestBody UpdateProfileRequest body) {

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error","missing_user"));
        }

        User updated = users.updateProfile(
                userId,
                body.email(),
                body.username(),
                body.firstName(),
                body.lastName(),
                body.avatarUrl()
        );

        UserDto dto = toDto(updated);
        return ResponseEntity.ok(dto);
    }
}
