package com.questbuddy.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BudgetCreateDTO(
        @NotBlank @Size(max = 120) String name,
        @NotNull List<BudgetSplitCreateDTO> splits
) {}