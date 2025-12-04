package com.questbuddy.label.controller;

import com.questbuddy.label.model.Label;
import com.questbuddy.user.model.User;
import com.questbuddy.label.repository.LabelRepository;
import com.questbuddy.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v5/labels")
@Tag(
        name = "Labels",
        description = "Endpoints for creating, listing, and deleting labels associated with users."
)
public class LabelController {

    private final LabelRepository labels;
    private final UserRepository users;

    public LabelController(LabelRepository labels, UserRepository users) {
        this.labels = labels;
        this.users = users;
    }

    // Uses small DTOs to avoid leaking user passwords/personally identifiable information.
    public record CreateLabelReq(Long userId, String name, String color) {}
    public record LabelRes(Long id, Long userId, String name, String color) {}

    // Maps a Label entity to the outward-facing DTO
    private LabelRes toRes(Label l) {
        return new LabelRes(l.getId(), l.getUser().getId(), l.getName(), l.getColor());
    }

    private User requireUser(Long id) {
        return users.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    // POST request that validates input, ensures the user exists, and prevents duplicate names per user (case-insensitive).
    // Returns 201 with the created label DTO.
    // Error cases are 400: missing userId/name, 404: user not found, 409: label with same name already exists for that user
    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Create a label",
            description = "Validates input, ensures the user exists, and creates a new label for that user. "
                    + "Prevents duplicate label names per user (case-insensitive)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Label created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LabelRes.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid fields in request body",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Label with same name already exists for that user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<?> create(@RequestBody CreateLabelReq body) {
        if (body == null || body.userId() == null || body.name() == null || body.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }

        var user = requireUser(body.userId());

        // prevent duplicate label names per user
        if (labels.existsByUser_IdAndNameIgnoreCase(user.getId(), body.name().trim())) {
            return ResponseEntity.status(409).body(Map.of("error", "label_exists"));
        }

        var l = new Label();
        l.setUser(user);
        l.setName(body.name().trim());
        l.setColor(body.color());
        var saved = labels.save(l);

        return ResponseEntity.status(HttpStatus.CREATED).body(toRes(saved));
    }

    // LIST by user
    @GetMapping(value = "/user/{userId}", produces = "application/json")
    @Operation(
            summary = "List labels for a user",
            description = "Returns all labels belonging to the specified user. "
                    + "If the user does not exist, a 404 is returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Labels returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LabelRes.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<List<LabelRes>> listByUser(
            @Parameter(
                    description = "ID of the user whose labels are being listed",
                    example = "5"
            )
            @PathVariable Long userId) {
        // optional: 404 if user missing
        requireUser(userId);
        var out = labels.findByUser_Id(userId).stream().map(this::toRes).toList();
        return ResponseEntity.ok(out);
    }

    // DELETE by id. Returns 404 if it doesn't exist.
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a label by ID",
            description = "Deletes a label by its ID. Returns 404 if the label does not exist."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Label deleted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Label not found",
                    content = @Content
            )
    })
    public ResponseEntity<?> delete(
            @Parameter(
                    description = "ID of the label to delete",
                    example = "10"
            )
            @PathVariable Long id) {
        if (!labels.existsById(id)) return ResponseEntity.notFound().build();
        labels.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Label deleted"));
    }

    // Health check just to make sure file is actually being read
    @GetMapping("/ping")
    @Operation(
            summary = "Label service health check",
            description = "Simple endpoint to verify that LabelController is alive and reachable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Label controller is alive"
            )
    })
    public String ping() { return "LabelController is alive!"; }

    // Converts NoSuchElementException thrown inside handlers (e.g., requireUser) into a 404 JSON response.
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(Map.of("error", "not_found", "message", e.getMessage()));
    }
}
