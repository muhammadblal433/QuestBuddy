package com.questbuddy.budget.repository;

import com.questbuddy.budget.model.BudgetSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetSplitRepository extends JpaRepository<BudgetSplit, Long> {
    List<BudgetSplit> findAllByBudget_Id(Long budgetId);
}