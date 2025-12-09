package com.example.androidexample.budget;

import static org.junit.Assert.*;

import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BudgetAdapterTest {

    @Test
    public void testItemCount() {
        List<Budget> list = new ArrayList<>();
        list.add(new Budget(1, "Trip 1", "john", 100, 50, "2024"));
        list.add(new Budget(2, "Trip 2", "anna", 200, 100, "2024"));

        BudgetAdapter adapter = new BudgetAdapter(list, b -> {});
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testOnClickListenerCalled() throws Exception {
        List<Budget> list = new ArrayList<>();
        Budget budget = new Budget(1, "Trip", "john", 10, 5, "2024");
        list.add(budget);

        final Budget[] clicked = new Budget[1];
        BudgetAdapter adapter = new BudgetAdapter(list, b -> clicked[0] = b);

        java.lang.reflect.Field f = BudgetAdapter.class.getDeclaredField("listener");
        f.setAccessible(true);
        BudgetAdapter.OnBudgetClickListener listener =
                (BudgetAdapter.OnBudgetClickListener) f.get(adapter);

        listener.onClick(budget);

        assertNotNull(clicked[0]);
        assertEquals(budget, clicked[0]);
    }

    @Test
    public void testBindViewHolderContent() {
        List<Budget> list = new ArrayList<>();
        Budget b = new Budget(1, "Hawaii Trip", "john", 300, 150, "2024-01-01");
        list.add(b);

        BudgetAdapter adapter = new BudgetAdapter(list, budgets -> {});

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(com.example.androidexample.R.layout.item_budget, null, false);

        BudgetAdapter.ViewHolder vh = new BudgetAdapter.ViewHolder(view);
        adapter.onBindViewHolder(vh, 0);

        assertEquals("Hawaii Trip", vh.tvName.getText().toString());
        assertEquals("Owner: john", vh.tvOwner.getText().toString());
        assertEquals("Paid: $150.0 | Share: $300.0", vh.tvTotals.getText().toString());
        assertEquals("Created: 2024-01-01", vh.tvCreated.getText().toString());
    }

    @Test
    public void testOwnerDisplayedAsYou() throws Exception {
        List<Budget> list = new ArrayList<>();
        Budget b = new Budget(1, "Trip", "meUser", 10, 5, "2024");
        list.add(b);

        BudgetAdapter adapter = new BudgetAdapter(list, bud -> {});

        java.lang.reflect.Field f = BudgetAdapter.class.getDeclaredField("currentUsername");
        f.setAccessible(true);
        f.set(adapter, "meUser");

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(com.example.androidexample.R.layout.item_budget, null, false);

        BudgetAdapter.ViewHolder vh = new BudgetAdapter.ViewHolder(view);
        adapter.onBindViewHolder(vh, 0);

        assertEquals("Owner: You", vh.tvOwner.getText().toString());
    }

    @Test
    public void testEmptyList() {
        List<Budget> list = new ArrayList<>();
        BudgetAdapter adapter = new BudgetAdapter(list, b -> {});
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testConstructorSetup() throws Exception {
        List<Budget> list = new ArrayList<>();
        Budget b = new Budget(1, "Trip", "john", 10, 5, "2024");
        list.add(b);

        final boolean[] called = {false};
        BudgetAdapter adapter = new BudgetAdapter(list, bud -> called[0] = true);

        java.lang.reflect.Field f = BudgetAdapter.class.getDeclaredField("listener");
        f.setAccessible(true);
        BudgetAdapter.OnBudgetClickListener listener =
                (BudgetAdapter.OnBudgetClickListener) f.get(adapter);

        listener.onClick(b);

        assertTrue(called[0]);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testNullUsername() throws Exception {
        List<Budget> list = new ArrayList<>();
        Budget b = new Budget(1, "Trip", "owner", 10, 5, "2024");
        list.add(b);

        BudgetAdapter adapter = new BudgetAdapter(list, bud -> {});

        java.lang.reflect.Field f = BudgetAdapter.class.getDeclaredField("currentUsername");
        f.setAccessible(true);
        f.set(adapter, null);

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(com.example.androidexample.R.layout.item_budget, null, false);

        BudgetAdapter.ViewHolder vh = new BudgetAdapter.ViewHolder(view);
        adapter.onBindViewHolder(vh, 0);

        assertEquals("Owner: owner", vh.tvOwner.getText().toString());
    }
}