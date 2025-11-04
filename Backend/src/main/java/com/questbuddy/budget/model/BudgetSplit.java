package com.questbuddy.budget.model;

import com.questbuddy.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "budget_splits",
        uniqueConstraints = @UniqueConstraint(name = "uq_budget_user", columnNames = {"budget_id","user_id"}),
        indexes = {
                @Index(name = "idx_budget_splits_budget", columnList = "budget_id"),
                @Index(name = "idx_budget_splits_user", columnList = "user_id")
        })
public class BudgetSplit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "budget_id")
    private Budget budget;

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;

    // How much this user is responsible for (owed / fair share)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shareAmount = BigDecimal.ZERO;

    // How much this user actually paid
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    // convenience
    public BigDecimal balance() { // positive --> overpaid (gets back), negative --> owes
        return paidAmount.subtract(shareAmount);
    }

    // getters/setters
    public Long getId() {
        return id;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    public void setShareAmount(BigDecimal shareAmount) {
        this.shareAmount = shareAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }
}
