package com.questbuddy.budget.controller;

import com.questbuddy.budget.dto.*;
import com.questbuddy.budget.service.BudgetService;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v11")
@Tag(
        name = "Budgets",
        description = "Create, view, update, delete budgets and compute per-user balances for an owner and participants."
)
public class BudgetController {

    private final BudgetService service;
    private final UserRepository users;

    public BudgetController(BudgetService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    // Create budget for a user (username-based)
    @PostMapping("/users/{ownerUsername}/budgets")
    @Operation(
            summary = "Create a budget",
            description = "Creates a new budget for the given owner username. The owner is identified by the path variable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Budget created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BudgetResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation failure)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Owner user not found",
                    content = @Content
            )
    })
    public BudgetResponseDTO create(
            @Parameter(
                    description = "Username of the budget owner",
                    example = "ayaan"
            )
            @PathVariable String ownerUsername,
            @RequestBody @Valid BudgetCreateDTO body) {
        return service.create(idOf(ownerUsername), body);
    }


    // Update a budget (optional name; optional full replace of splits)
    // NOTE: owner can update all fields; a participant (X-Username != owner) can only update their own split amounts
    @PutMapping("/users/{ownerUsername}/budgets/{budgetId}")
    @Operation(
            summary = "Update a budget",
            description = "Updates a budget's name and/or splits. The owner can update all fields. "
                    + "A participant identified via X-Username can only update their own split amounts."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Budget updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BudgetResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation failure)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Requester is not allowed to modify this budget or splits",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Owner user or budget not found",
                    content = @Content
            )
    })
    public BudgetResponseDTO update(
            @Parameter(
                    description = "Username of the budget owner",
                    example = "ayaan"
            )
            @PathVariable String ownerUsername,
            @Parameter(
                    description = "ID of the budget to update",
                    example = "10"
            )
            @PathVariable Long budgetId,
            @RequestBody @Valid BudgetUpdateDTO body,
            @Parameter(
                    description = "Optional requester username. If absent or equal to the owner username, "
                            + "the request is treated as an owner update. Otherwise, only the participant's split may be updated.",
                    example = "friend_user",
                    required = false
            )
            @RequestHeader(value = "X-Username", required = false) String requesterUsername) {
        Long ownerId = idOf(ownerUsername);
        if (requesterUsername == null || requesterUsername.isBlank()
                || ownerUsername.equalsIgnoreCase(requesterUsername)) {
            // owner path (existing behavior)
            return service.update(ownerId, budgetId, body);
        }
        // participant path: only modify own split
        return service.update(ownerId, budgetId, body, requesterUsername);
    }

    // Health/test check
    @GetMapping("/budgets/ping")
    @Operation(
            summary = "Budget service health check",
            description = "Simple endpoint to verify that BudgetController v11 is alive and reachable."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget controller is alive")
    })
    public String ping() { return "BudgetController v11 is alive!"; }

    // List budgets for a user
    // NOTE: if X-Username is provided and is NOT the owner, only budgets that include that user are returned
    @GetMapping("/users/{ownerUsername}/budgets")
    @Operation(
            summary = "List budgets for owner/participant",
            description = "Returns budgets created by the owner. If X-Username is provided and is different from the owner, "
                    + "only budgets that include that user as a participant are returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Budgets returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BudgetResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Owner user not found",
                    content = @Content
            )
    })
    public List<BudgetResponseDTO> list(
            @Parameter(
                    description = "Username of the budget owner whose budgets are being queried",
                    example = "ayaan"
            )
            @PathVariable String ownerUsername,
            @Parameter(
                    description = "Optional requester username for participant-filtered view. "
                            + "If absent or equal to the owner username, returns the full owner view.",
                    example = "friend_user",
                    required = false
            )
            @RequestHeader(value = "X-Username", required = false) String requesterUsername) {
        Long ownerId = idOf(ownerUsername);
        if (requesterUsername == null || requesterUsername.isBlank()
                || ownerUsername.equalsIgnoreCase(requesterUsername)) {
            // original owner behavior (unchanged)
            return service.list(ownerId);
        }
        // participant-filtered view
        return service.list(ownerId, requesterUsername);
    }

    // Get a single budget (with splits + totals)
    // NOTE: owner always allowed; participant allowed if included in splits via X-Username header
    @GetMapping("/users/{ownerUsername}/budgets/{budgetId}")
    @Operation(
            summary = "Get a single budget",
            description = "Retrieves a single budget including splits and totals. The owner is always allowed; "
                    + "a participant may access the budget if included in splits and identified via the X-Username header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Budget returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BudgetResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Requester is not allowed to view this budget",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Owner user or budget not found",
                    content = @Content
            )
    })
    public BudgetResponseDTO get(
            @Parameter(
                    description = "Username of the budget owner",
                    example = "ayaan"
            )
            @PathVariable String ownerUsername,
            @Parameter(
                    description = "ID of the budget to fetch",
                    example = "10"
            )
            @PathVariable Long budgetId,
            @Parameter(
                    description = "Optional requester username for participant view. "
                            + "If absent or equal to the owner username, owner view is used.",
                    example = "friend_user",
                    required = false
            )
            @RequestHeader(value = "X-Username", required = false) String requesterUsername) {
        Long ownerId = idOf(ownerUsername);
        if (requesterUsername == null || requesterUsername.isBlank()
                || ownerUsername.equalsIgnoreCase(requesterUsername)) {
            // original owner behavior
            return service.get(ownerId, budgetId);
        }
        // participant view
        return service.get(ownerId, budgetId, requesterUsername);
    }

    // Compute per-user balances for a budget
    // NOTE: owner always allowed; participant allowed if included in splits via X-Username header
    @GetMapping("/users/{ownerUsername}/budgets/{budgetId}/balances")
    @Operation(
            summary = "Compute per-user balances for a budget",
            description = "Computes per-user balances for a budget. The owner can always access balances. "
                    + "A participant identified via X-Username can view balances if they are included in the budget splits."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balances calculated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BudgetBalanceDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Requester is not allowed to view balances for this budget",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Owner user or budget not found",
                    content = @Content
            )
    })
    public List<BudgetBalanceDTO> balances(
            @Parameter(
                    description = "Username of the budget owner",
                    example = "ayaan"
            )
            @PathVariable String ownerUsername,
            @Parameter(
                    description = "ID of the budget whose balances are requested",
                    example = "10"
            )
            @PathVariable Long budgetId,
            @Parameter(
                    description = "Optional requester username for participant view. "
                            + "If absent or equal to the owner username, owner view is used.",
                    example = "friend_user",
                    required = false
            )
            @RequestHeader(value = "X-Username", required = false) String requesterUsername) {
        Long ownerId = idOf(ownerUsername);
        if (requesterUsername == null || requesterUsername.isBlank()
                || ownerUsername.equalsIgnoreCase(requesterUsername)) {
            // original owner behavior
            return service.balances(ownerId, budgetId);
        }
        // participant view
        return service.balances(ownerId, budgetId, requesterUsername);
    }

    // Delete a budget
    @DeleteMapping("/users/{ownerUsername}/budgets/{budgetId}")
    @Operation(
            summary = "Delete a budget",
            description = "Deletes a budget owned by the given user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Budget deleted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Owner user or budget not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiMessage> delete(
            @Parameter(
                    description = "Username of the budget owner",
                    example = "ayaan"
            )
            @PathVariable String ownerUsername,
            @Parameter(
                    description = "ID of the budget to delete",
                    example = "10"
            )
            @PathVariable Long budgetId) {
        service.delete(idOf(ownerUsername), budgetId);
        return ResponseEntity.ok(new ApiMessage("Budget deleted"));
    }

    private Long idOf(String username) {
        User u = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user_not_found: " + username));
        return u.getId();
    }
}
