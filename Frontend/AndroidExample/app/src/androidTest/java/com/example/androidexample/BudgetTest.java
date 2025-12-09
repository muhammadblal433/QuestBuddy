package com.example.androidexample;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.androidexample.budget.Budget;

public class BudgetTest {

    @Test
    public void testBudgetConstructorAndGetters() {
        long id = 1L;
        String name = "Road Trip Budget";
        String ownerUsername = "john_doe";
        double totalShare = 250.0;
        double totalPaid = 100.0;
        String createdAt = "2025-01-01T12:00:00";

        Budget budget = new Budget(id, name, ownerUsername, totalShare, totalPaid, createdAt);

        assertEquals(id, budget.getId());
        assertEquals(name, budget.getName());
        assertEquals(ownerUsername, budget.getOwnerUsername());
        assertEquals(totalShare, budget.getTotalShare(), 0.001);
        assertEquals(totalPaid, budget.getTotalPaid(), 0.001);
        assertEquals(createdAt, budget.getCreatedAt());
    }

    @Test
    public void testBudgetValuesAreStoredCorrectly() {
        Budget b = new Budget(5, "Group Budget", "alice", 500.50, 120.75, "2025-02-05");

        assertTrue(b.getId() > 0);
        assertNotNull(b.getName());
        assertNotNull(b.getOwnerUsername());
        assertTrue(b.getTotalShare() >= 0);
        assertTrue(b.getTotalPaid() >= 0);
    }
}

