package com.questbuddy.budget.service;

import com.questbuddy.budget.dto.*;

import java.util.List;

public interface BudgetService {
    BudgetResponseDTO create(Long ownerId, BudgetCreateDTO body);
    List<BudgetResponseDTO> list(Long ownerId);
    BudgetResponseDTO get(Long ownerId, Long budgetId);
    BudgetResponseDTO update(Long ownerId, Long budgetId, BudgetUpdateDTO body);
    void delete(Long ownerId, Long budgetId);
    List<BudgetBalanceDTO> balances(Long ownerId, Long budgetId);
}