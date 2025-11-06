package com.questbuddy.budget.service;

import com.questbuddy.budget.BudgetMapper;
import com.questbuddy.budget.dto.*;
import com.questbuddy.budget.model.Budget;
import com.questbuddy.budget.model.BudgetSplit;
import com.questbuddy.budget.repository.BudgetRepository;
import com.questbuddy.budget.repository.BudgetSplitRepository;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BudgetServiceImplemented implements BudgetService {

    private final BudgetRepository budgets;
    private final BudgetSplitRepository splits;
    private final UserRepository users;
    private final BudgetMapper mapper = new BudgetMapper();

    public BudgetServiceImplemented(BudgetRepository budgets,
                                    BudgetSplitRepository splits,
                                    UserRepository users) {
        this.budgets = budgets;
        this.splits = splits;
        this.users = users;
    }

    @Override
    @Transactional
    public BudgetResponseDTO create(Long ownerId, BudgetCreateDTO body) {
        if (body == null || body.splits() == null || body.splits().isEmpty()) {
            throw new ValidationException("splits required");
        }

        Budget b = new Budget();
        b.setOwner(requireUser(ownerId));
        b.setName(requireNonBlank(body.name()));

        // build splits
        Map<Long, Boolean> seen = new HashMap<>();
        List<BudgetSplit> splitEntities = new ArrayList<>();
        for (BudgetSplitCreateDTO s : body.splits()) {
            User u = users.findByUsernameIgnoreCase(s.username())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user_not_found: " + s.username()));
            if (seen.putIfAbsent(u.getId(), true) != null) {
                throw new ValidationException("duplicate participant: " + s.username());
            }

            BudgetSplit be = new BudgetSplit();
            be.setBudget(b);
            be.setUser(u);
            be.setShareAmount(nz(s.shareAmount()));
            be.setPaidAmount(nz(s.paidAmount()));
            splitEntities.add(be);
        }
        b.setSplits(splitEntities);

        Budget saved = budgets.save(b);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponseDTO> list(Long ownerId) {
        return budgets.findAllByOwner_Id(ownerId, Sort.by("createdAt").descending())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponseDTO> list(Long ownerId, String requesterUsername) {
        // If requester is null/blank, treat as not authorized (should be handled by controller)
        if (requesterUsername == null || requesterUsername.isBlank()) {
            return List.of();
        }
        // Load owner's budgets and filter to those where requester appears in splits
        return budgets.findAllByOwner_Id(ownerId, Sort.by("createdAt").descending())
                .stream()
                .filter(b -> canViewAsParticipant(b, requesterUsername))
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponseDTO get(Long ownerId, Long budgetId) {
        Budget b = budgets.findByIdAndOwner_Id(budgetId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "budget_not_found"));
        return mapper.toResponse(b);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponseDTO get(Long ownerId, Long budgetId, String requesterUsername) {
        Budget b = budgets.findByIdAndOwner_Id(budgetId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "budget_not_found"));
        if (!canViewAsParticipant(b, requesterUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        return mapper.toResponse(b);
    }

    @Override
    @Transactional
    public BudgetResponseDTO update(Long ownerId, Long budgetId, BudgetUpdateDTO body) {
        Budget b = budgets.findByIdAndOwner_Id(budgetId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "budget_not_found"));

        // Optional: rename
        if (body.name() != null) {
            String nm = body.name().trim();
            if (nm.isEmpty()) throw new ValidationException("name cannot be blank");
            b.setName(nm);
        }

        // Optional: replace splits (upsert listed, prune unlisted)
        if (body.splits() != null) {
            // resolve desired participants (username -> userId), enforce no duplicates
            Set<String> seenUsernames = new HashSet<>();
            Map<Long, BudgetSplitCreateDTO> desiredByUserId = new LinkedHashMap<>();
            for (BudgetSplitCreateDTO s : body.splits()) {
                String uname = s.username();
                if (uname == null || uname.isBlank())
                    throw new ValidationException("username required in split");

                String key = uname.toLowerCase();
                if (!seenUsernames.add(key))
                    throw new ValidationException("duplicate participant: " + uname);

                User u = users.findByUsernameIgnoreCase(uname)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user_not_found: " + uname));
                desiredByUserId.put(u.getId(), s);
            }

            // index existing splits by userId
            Map<Long, BudgetSplit> existingByUserId = b.getSplits().stream()
                    .collect(Collectors.toMap(s -> s.getUser().getId(), s -> s));

            // upsert/update for each desired participant
            Set<Long> keepUserIds = new HashSet<>();
            for (Map.Entry<Long, BudgetSplitCreateDTO> e : desiredByUserId.entrySet()) {
                Long userId = e.getKey();
                BudgetSplitCreateDTO dto = e.getValue();

                BigDecimal share = nz(dto.shareAmount());
                BigDecimal paid  = nz(dto.paidAmount());
                if (share.signum() < 0 || paid.signum() < 0)
                    throw new ValidationException("amounts cannot be negative");

                BudgetSplit exist = existingByUserId.get(userId);
                if (exist == null) {
                    // create new split
                    User u = users.findById(userId).orElseThrow();
                    BudgetSplit ns = new BudgetSplit();
                    ns.setBudget(b);
                    ns.setUser(u);
                    ns.setShareAmount(share);
                    ns.setPaidAmount(paid);
                    b.getSplits().add(ns);
                } else {
                    // update existing
                    exist.setShareAmount(share);
                    exist.setPaidAmount(paid);
                }
                keepUserIds.add(userId);
            }

            // prune participants not listed in the update
            b.getSplits().removeIf(s -> !keepUserIds.contains(s.getUser().getId()));
        }

        Budget saved = budgets.save(b);
        return mapper.toResponse(saved);
    }

    // participant update - requesterUsername is not the owner; can only edit their own split amounts
    @Override
    @Transactional
    public BudgetResponseDTO update(Long ownerId, Long budgetId, BudgetUpdateDTO body, String requesterUsername) {
        if (requesterUsername == null || requesterUsername.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        Budget b = budgets.findByIdAndOwner_Id(budgetId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "budget_not_found"));

        // Must be a listed participant
        BudgetSplit self = findSplitByUsername(b, requesterUsername);
        if (self == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        // Participants cannot rename the budget
        if (body.name() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "participants_cannot_rename");
        }

        // Participants can only modify their own split amounts.
        if (body.splits() == null || body.splits().isEmpty()) {
            // No changes; return current state
            return mapper.toResponse(b);
        }

        // Only accept a single entry that matches the requester
        BudgetSplitCreateDTO mine = null;
        for (BudgetSplitCreateDTO s : body.splits()) {
            if (s.username() == null || s.username().isBlank()) {
                throw new ValidationException("username required in split");
            }
            String uname = s.username().trim();
            if (uname.equalsIgnoreCase(requesterUsername)) {
                if (mine != null) {
                    throw new ValidationException("duplicate self entry");
                }
                mine = s;
            } else {
                // participants cannot modify others
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "participants_can_only_update_self");
            }
        }

        if (mine == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing_self_split_update");
        }

        // Apply changes to own split (only fields provided)
        if (mine.shareAmount() != null) {
            if (mine.shareAmount().signum() < 0) throw new ValidationException("amounts cannot be negative");
            self.setShareAmount(mine.shareAmount());
        }
        if (mine.paidAmount() != null) {
            if (mine.paidAmount().signum() < 0) throw new ValidationException("amounts cannot be negative");
            self.setPaidAmount(mine.paidAmount());
        }

        Budget saved = budgets.save(b);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long ownerId, Long budgetId) {
        long n = budgets.deleteByIdAndOwner_Id(budgetId, ownerId);
        if (n == 0) throw new ResponseStatusException(NOT_FOUND, "budget_not_found");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetBalanceDTO> balances(Long ownerId, Long budgetId) {
        Budget b = budgets.findByIdAndOwner_Id(budgetId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "budget_not_found"));
        return mapper.toBalances(b);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetBalanceDTO> balances(Long ownerId, Long budgetId, String requesterUsername) {
        Budget b = budgets.findByIdAndOwner_Id(budgetId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "budget_not_found"));
        if (!canViewAsParticipant(b, requesterUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        return mapper.toBalances(b);
    }

    // helpers
    private User requireUser(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user_not_found: " + id));
    }
    private static String requireNonBlank(String s) {
        if (s == null || s.trim().isEmpty()) throw new ValidationException("name required");
        return s.trim();
    }
    private static BigDecimal nz(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }

    /** True if requesterUsername matches any split participant (by username, case-insensitive). */
    private static boolean canViewAsParticipant(Budget b, String requesterUsername) {
        if (requesterUsername == null || requesterUsername.isBlank()) return false;
        String req = requesterUsername.trim().toLowerCase();
        for (BudgetSplit s : b.getSplits()) {
            String uname = s.getUser() != null && s.getUser().getUsername() != null
                    ? s.getUser().getUsername().trim().toLowerCase() : null;
            if (uname != null && uname.equals(req)) return true;
        }
        return false;
    }

    private static BudgetSplit findSplitByUsername(Budget b, String username) {
        if (username == null) return null;
        String req = username.trim().toLowerCase();
        for (BudgetSplit s : b.getSplits()) {
            String uname = s.getUser() != null && s.getUser().getUsername() != null
                    ? s.getUser().getUsername().trim().toLowerCase() : null;
            if (uname != null && uname.equals(req)) return s;
        }
        return null;
    }
}