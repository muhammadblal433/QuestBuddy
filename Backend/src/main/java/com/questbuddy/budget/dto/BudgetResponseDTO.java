package com.questbuddy.budget.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BudgetResponseDTO(
        Long id,
        String name,
        Long ownerId,
        String ownerUsername,
        BigDecimal totalShare,
        BigDecimal totalPaid,
        Instant createdAt,
        List<BudgetSplitDTO> splits
) {}