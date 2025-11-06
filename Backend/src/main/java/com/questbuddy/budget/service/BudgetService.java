package com.questbuddy.budget.service;

import com.questbuddy.budget.dto.*;

import java.util.List;

public interface BudgetService {
    BudgetResponseDTO create(Long ownerId, BudgetCreateDTO body);

    // Owner view (original)
    List<BudgetResponseDTO> list(Long ownerId);

    // Participant-filtered view (requesterUsername is not the owner)
    List<BudgetResponseDTO> list(Long ownerId, String requesterUsername);

    // Owner view (original)
    BudgetResponseDTO get(Long ownerId, Long budgetId);

    // Participant view (requesterUsername is not the owner)
    BudgetResponseDTO get(Long ownerId, Long budgetId, String requesterUsername);

    BudgetResponseDTO update(Long ownerId, Long budgetId, BudgetUpdateDTO body);
    void delete(Long ownerId, Long budgetId);

    // Owner view (original)
    List<BudgetBalanceDTO> balances(Long ownerId, Long budgetId);

    // Participant view (requesterUsername is not the owner)
    List<BudgetBalanceDTO> balances(Long ownerId, Long budgetId, String requesterUsername);
}