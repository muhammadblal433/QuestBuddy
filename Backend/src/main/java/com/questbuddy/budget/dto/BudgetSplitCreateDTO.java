package com.questbuddy.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record BudgetSplitCreateDTO(
        @NotBlank String username,
        @DecimalMin("0.00") BigDecimal shareAmount,
        @DecimalMin("0.00") BigDecimal paidAmount
) {}