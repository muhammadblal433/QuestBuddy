package com.questbuddy.budget;

import com.questbuddy.budget.dto.*;
import com.questbuddy.budget.model.Budget;
import com.questbuddy.budget.model.BudgetSplit;

import java.math.BigDecimal;
import java.util.List;

public class BudgetMapper {

    public BudgetResponseDTO toResponse(Budget b) {
        BigDecimal totalShare = BigDecimal.ZERO;
        BigDecimal totalPaid  = BigDecimal.ZERO;

        List<BudgetSplitDTO> splitDtos = b.getSplits().stream().map(s -> {
            BigDecimal share = nz(s.getShareAmount());
            BigDecimal paid  = nz(s.getPaidAmount());
            return new BudgetSplitDTO(
                    s.getUser().getId(),
                    s.getUser().getUsername(),
                    share,
                    paid,
                    paid.subtract(share)
            );
        }).toList();

        for (BudgetSplitDTO s : splitDtos) {
            totalShare = totalShare.add(nz(s.shareAmount()));
            totalPaid  = totalPaid.add(nz(s.paidAmount()));
        }

        return new BudgetResponseDTO(
                b.getId(),
                b.getName(),
                b.getOwner().getId(),
                b.getOwner().getUsername(),
                totalShare,
                totalPaid,
                b.getCreatedAt(),
                splitDtos
        );
    }

    public List<BudgetBalanceDTO> toBalances(Budget b) {
        return b.getSplits().stream()
                .map(s -> new BudgetBalanceDTO(
                        s.getUser().getId(),
                        s.getUser().getUsername(),
                        nz(s.getPaidAmount()).subtract(nz(s.getShareAmount()))
                ))
                .toList();
    }

    private static java.math.BigDecimal nz(java.math.BigDecimal x) {
        return x == null ? java.math.BigDecimal.ZERO : x;
    }
}