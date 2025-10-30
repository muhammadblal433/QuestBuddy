package com.questbuddy.budget.dto;

import java.math.BigDecimal;

public record BudgetBalanceDTO(
        Long userId,
        String username,
        BigDecimal balance // positive: gets back; negative: owes
) {}