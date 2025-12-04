package com.questbuddy.user.controller;

import com.questbuddy.user.model.User;
import com.questbuddy.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Handles basic signup, delete, and CRUD operations for testing/demo purposes.
 */
@RestController
@RequestMapping("/api/v2/users") // separate base path to avoid conflicts with /api/v1 auth/profile routes
@Tag(
        name = "Users Admin",
        description = "Admin/test endpoints for listing and deleting users (no passwords exposed)."
)
public class UserSignupDeleteController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder; // this is to properly encypt passwrd

    public UserSignupDeleteController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    // Data Transfer Object - hides raw password from being transferred
    public record UserDto(
            Long id, String email, String username,
            String firstName, String lastName, String avatarUrl,
            String role, boolean active, Instant createdAt, Instant updatedAt
    ) {}

    // Centralized mapping from entity, goes to DTO (keeps controller responses consistent)
    private static UserDto toDto(User u) {
        return new UserDto(
                u.getId(), u.getEmail(), u.getUsername(),
                u.getFirstName(), u.getLastName(), u.getAvatarUrl(),
                u.getRole() == null ? null : u.getRole().name(),
                Boolean.TRUE.equals(u.isActive()),
                u.getCreatedAt(), u.getUpdatedAt()
        );
    }

    // POST - Signup user - refer to AuthController.java. // POST /api/v1/auth/signup

    // GET - All users
    @GetMapping
    @Operation(
            summary = "List all users",
            description = "Returns a list of all users, mapped to a safe DTO that does not expose passwords."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            )
    })
    public ResponseEntity<?> getAllUsers() {
        // List users w/o leaking their passwords
        List<UserDto> out = userRepo.findAll().stream().map(UserSignupDeleteController::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    // GET - User by ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID (admin/test)",
            description = "Returns a single user by ID as a DTO. Intended for testing/demo and admin-style operations."
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
    public ResponseEntity<?> getUserById(
            @Parameter(
                    description = "ID of the user to fetch",
                    example = "5"
            )
            @PathVariable Long id) {
        // Get just the user as a DTO
        return userRepo.findById(id).map(UserSignupDeleteController::toDto)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // GET - User by username
    @GetMapping("/by-username/{username}")
    @Operation(
            summary = "Get user by username",
            description = "Returns a single user by username (case-insensitive) as a DTO."
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
    public ResponseEntity<?> getByUsername(
            @Parameter(
                    description = "Username to search for (case-insensitive)",
                    example = "ayaan"
            )
            @PathVariable String username) {
        return userRepo.findByUsernameIgnoreCase(username)
                .map(UserSignupDeleteController::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Health/test check to make sure that the file is being read
    @GetMapping("/ping")
    @Operation(
            summary = "User admin service health check",
            description = "Simple endpoint to verify that UserSignupDeleteController is alive and reachable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Controller is alive"
            )
    })
    public String ping() {
        return "UserSignupDeleteController is alive!";
    }

    // DELETE - Delete user
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user by ID",
            description = "Deletes a user by ID if they exist. Intended for testing/demo/admin purposes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<?> deleteUser(
            @Parameter(
                    description = "ID of the user to delete",
                    example = "5"
            )
            @PathVariable Long id) {
        if (!userRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepo.deleteById(id);
        return ResponseEntity.ok("User deleted successfully!");
    }
}
