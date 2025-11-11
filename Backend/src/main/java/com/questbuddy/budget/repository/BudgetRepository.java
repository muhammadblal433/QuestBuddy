package com.questbuddy.budget.repository;

import com.questbuddy.budget.model.Budget;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByOwner_Id(Long ownerId, Sort sort);
    Optional<Budget> findByIdAndOwner_Id(Long id, Long ownerId);
    long deleteByIdAndOwner_Id(Long id, Long ownerId);

    Page<Budget> findByOwner_IdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);
    Page<Budget> findByOwner_IdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(Long ownerId, String name, Pageable pageable);
}