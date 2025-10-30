package com.questbuddy.budget.dto;

import java.math.BigDecimal;

public record BudgetSplitDTO(
        Long userId,
        String username,
        BigDecimal shareAmount,
        BigDecimal paidAmount,
        BigDecimal balance // paid - share
) {}