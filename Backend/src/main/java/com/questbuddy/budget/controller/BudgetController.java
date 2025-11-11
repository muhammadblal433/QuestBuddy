package com.questbuddy.budget.controller;

import com.questbuddy.budget.dto.*;
import com.questbuddy.budget.service.BudgetService;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v11")
public class BudgetController {

    private final BudgetService service;
    private final UserRepository users;

    public BudgetController(BudgetService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    // Health/test check
    @GetMapping("/budgets/ping")
    public String ping() { return "BudgetController v11 is alive!"; }

    // Create budget for a user (username-based)
    @PostMapping("/users/{ownerUsername}/budgets")
    public BudgetResponseDTO create(@PathVariable String ownerUsername,
                                    @RequestBody @Valid BudgetCreateDTO body) {
        return service.create(idOf(ownerUsername), body);
    }

    // List budgets for a user
    // NOTE: if X-Username is provided and is NOT the owner, only budgets that include that user are returned
    @GetMapping("/users/{ownerUsername}/budgets")
    public List<BudgetResponseDTO> list(@PathVariable String ownerUsername,
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

    // Paged + searchable budgets list (keeps existing endpoints unchanged)
    @GetMapping("/users/{ownerUsername}/budgets/paged")
    public Page<BudgetResponseDTO> listPaged(@PathVariable String ownerUsername,
                                             @RequestHeader(value = "X-Username", required = false) String requesterUsername,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String q) {
        Long ownerId = idOf(ownerUsername);
        List<BudgetResponseDTO> all;
        if (requesterUsername == null || requesterUsername.isBlank()
                || ownerUsername.equalsIgnoreCase(requesterUsername)) {
            all = service.list(ownerId);
        } else {
            all = service.list(ownerId, requesterUsername);
        }

        if (q != null && !q.isBlank()) {
            String needle = q.trim().toLowerCase();
            all = all.stream()
                    .filter(b -> b.name() != null && b.name().toLowerCase().contains(needle))
                    .collect(Collectors.toList());
        }

        all.sort(Comparator.comparing(BudgetResponseDTO::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        var pageable = PageRequest.of(page, size);
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<BudgetResponseDTO> slice = all.subList(from, to);

        return new PageImpl<>(slice, pageable, all.size());
    }

    // Get a single budget (with splits + totals)
    // NOTE: owner always allowed; participant allowed if included in splits via X-Username header
    @GetMapping("/users/{ownerUsername}/budgets/{budgetId}")
    public BudgetResponseDTO get(@PathVariable String ownerUsername,
                                 @PathVariable Long budgetId,
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

    // Update a budget (optional name; optional full replace of splits)
    // NOTE: owner can update all fields; a participant (X-Username != owner) can only update their own split amounts
    @PutMapping("/users/{ownerUsername}/budgets/{budgetId}")
    public BudgetResponseDTO update(@PathVariable String ownerUsername,
                                    @PathVariable Long budgetId,
                                    @RequestBody @Valid BudgetUpdateDTO body,
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

    // Delete a budget
    @DeleteMapping("/users/{ownerUsername}/budgets/{budgetId}")
    public ResponseEntity<ApiMessage> delete(@PathVariable String ownerUsername,
                                             @PathVariable Long budgetId) {
        service.delete(idOf(ownerUsername), budgetId);
        return ResponseEntity.ok(new ApiMessage("Budget deleted"));
    }

    // Compute per-user balances for a budget
    // NOTE: owner always allowed; participant allowed if included in splits via X-Username header
    @GetMapping("/users/{ownerUsername}/budgets/{budgetId}/balances")
    public List<BudgetBalanceDTO> balances(@PathVariable String ownerUsername,
                                           @PathVariable Long budgetId,
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

    private Long idOf(String username) {
        User u = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user_not_found: " + username));
        return u.getId();
    }
}