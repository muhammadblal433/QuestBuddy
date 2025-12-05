package com.questbuddy.user.controller;

import com.questbuddy.user.model.User;
import com.questbuddy.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.net.URI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1")
@Tag(
        name = "Users",
        description = "User profile operations, including fetching and updating user information."
)
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    // Make Data Transfer Object - excluding password (due to security reasons)
    public record UserDto(Long id, String email, String username,
                          String firstName, String lastName, String avatarUrl,
                          String role, boolean active, boolean premium,
                          java.time.Instant createdAt, java.time.Instant updatedAt) {}

    private static UserDto toDto(User u) {
        return new UserDto(
                u.getId(), u.getEmail(), u.getUsername(),
                u.getFirstName(), u.getLastName(), u.getAvatarUrl(),
                u.getRole().name(), u.isActive(), u.isPremiumUser(),
                u.getCreatedAt(), u.getUpdatedAt()
        );
    }

    public record SignupRequest(String email, String username, String password,
                                String firstName, String lastName) {}

    public record UpdateProfileRequest(String email, String username,
                                       String firstName, String lastName,
                                       String avatarUrl, String newPassword) {}

    // PUT (edit user profile (path version; forbid if header doesn’t match)
    @PutMapping(value = "/users/{id}", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Update a user profile by ID",
            description = "Updates the profile of the user identified by the path ID. "
                    + "The X-User-Id header must match the path ID, otherwise the request is forbidden."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing X-User-Id header",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "X-User-Id does not match the path ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<?> update(
            @Parameter(
                    description = "ID of the user whose profile is being updated",
                    example = "5"
            )
            @PathVariable Long id,
            @Parameter(
                    description = "ID of the currently authenticated user",
                    example = "5",
                    required = false
            )
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

    // PUT - update currently logged in profile: header-based auth for mini-assignment
    @PutMapping(value = "/users/me", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Update the current user's profile",
            description = "Updates the profile of the currently authenticated user, identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user profile updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing X-User-Id header",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<?> updateMe(
            @Parameter(
                    description = "ID of the currently authenticated user",
                    example = "5",
                    required = false
            )
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
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

    // POST - for testing purposes - add users in batches
    @PostMapping(value = "/auth/signup/batch", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Batch signup users (testing)",
            description = "Creates multiple users in a single request for testing purposes. "
                    + "Only entries with all required fields (email, username, password) are created."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Users created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            )
    })
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

    // GET (Fetch user by id)
    @GetMapping(value = "/users/{id}", produces = "application/json")
    @Operation(
            summary = "Get user by ID",
            description = "Fetches a user's public profile details by their ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<UserDto> get(
            @Parameter(
                    description = "ID of the user to fetch",
                    example = "5"
            )
            @PathVariable Long id) {
        Optional<User> opt = users.getById(id);
        if (opt.isPresent()) {
            User u = opt.get();
            UserDto dto = toDto(u);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET - current profile (that is logged in): header-based auth for mini-assignment
    @GetMapping(value = "/users/me", produces = "application/json")
    @Operation(
            summary = "Get current user's profile",
            description = "Returns the profile of the currently authenticated user, identified by the X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing X-User-Id header",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<?> me(
            @Parameter(
                    description = "ID of the currently authenticated user",
                    example = "5",
                    required = false
            )
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

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

    // GET – list all premium users
    @GetMapping(value = "/users/premium", produces = "application/json")
    public ResponseEntity<List<UserDto>> listPremiumUsers() {
        List<User> premiumUsers = users.findPremiumUsers();
        List<UserDto> result = new ArrayList<>();

        for (User u : premiumUsers) {
            result.add(toDto(u));
        }

        return ResponseEntity.ok(result);
    }
}
