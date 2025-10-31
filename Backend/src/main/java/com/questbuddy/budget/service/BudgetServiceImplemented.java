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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

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
    public BudgetResponseDTO get(Long ownerId, Long budgetId) {
        Budget b = budgets.findByIdAndOwner_Id(budgetId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "budget_not_found"));
        return mapper.toResponse(b);
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

    // helpers
    private User requireUser(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user_not_found: " + id));
    }
    private static String requireNonBlank(String s) {
        if (s == null || s.trim().isEmpty()) throw new ValidationException("name required");
        return s.trim();
    }
    private static BigDecimal nz(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }
}