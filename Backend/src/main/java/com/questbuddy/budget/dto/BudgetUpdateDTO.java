package com.questbuddy.budget.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

// Optional update: if 'name' provided, rename; if 'splits' provided, replace (upsert + prune)
public record BudgetUpdateDTO(
        @Size(max = 120) String name,
        List<BudgetSplitCreateDTO> splits
) {}