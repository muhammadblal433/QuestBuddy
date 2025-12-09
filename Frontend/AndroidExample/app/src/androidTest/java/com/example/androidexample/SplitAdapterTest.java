package com.example.androidexample.budget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SplitAdapterTest {

    private SplitAdapter adapter;
    private List<Split> splits;

    @Before
    public void setUp() {
        splits = new ArrayList<>();
        splits.add(new Split("alice", 50.0, 30.0, -20.0));
        splits.add(new Split("bob", 50.0, 40.0, -10.0));
        splits.add(new Split("charlie", 50.0, 50.0, 0.0));
    }

    private ViewGroup createMockParent() {
        return new FrameLayout(ApplicationProvider.getApplicationContext());
    }

    private SplitAdapter.ViewHolder createViewHolder() {
        ViewGroup parent = createMockParent();
        return adapter.onCreateViewHolder(parent, 0);
    }

    @Test
    public void testConstructor_WithValidData() {
        adapter = new SplitAdapter(splits);
        assertNotNull(adapter);
    }

    @Test
    public void testConstructor_WithEmptyList() {
        adapter = new SplitAdapter(new ArrayList<>());
        assertNotNull(adapter);
    }

    @Test
    public void testGetItemCount_ReturnsCorrectCount() {
        adapter = new SplitAdapter(splits);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_WithEmptyList() {
        adapter = new SplitAdapter(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_WithSingleItem() {
        List<Split> singleSplit = new ArrayList<>();
        singleSplit.add(new Split("user1", 100.0, 50.0, -50.0));
        adapter = new SplitAdapter(singleSplit);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_WithMultipleItems() {
        List<Split> multipleSplits = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            multipleSplits.add(new Split("user" + i, 10.0, 5.0, -5.0));
        }
        adapter = new SplitAdapter(multipleSplits);
        assertEquals(10, adapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder_CreatesViewHolder() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder);
        assertNotNull(holder.itemView);
    }

    @Test
    public void testOnCreateViewHolder_HasAllViews() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder.tvUsername);
        assertNotNull(holder.tvShare);
        assertNotNull(holder.tvPaid);
        assertNotNull(holder.tvBalance);
    }

    @Test
    public void testOnBindViewHolder_SetsUsernameText() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("User: alice", holder.tvUsername.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_SetsShareAmountText() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Share: $50.0", holder.tvShare.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_SetsPaidAmountText() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Paid: $30.0", holder.tvPaid.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_SetsBalanceText() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Balance: $-20.0", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_FirstPosition() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("User: alice", holder.tvUsername.getText().toString());
        assertEquals("Share: $50.0", holder.tvShare.getText().toString());
        assertEquals("Paid: $30.0", holder.tvPaid.getText().toString());
        assertEquals("Balance: $-20.0", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_SecondPosition() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 1);

        assertEquals("User: bob", holder.tvUsername.getText().toString());
        assertEquals("Share: $50.0", holder.tvShare.getText().toString());
        assertEquals("Paid: $40.0", holder.tvPaid.getText().toString());
        assertEquals("Balance: $-10.0", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_ThirdPosition() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 2);

        assertEquals("User: charlie", holder.tvUsername.getText().toString());
        assertEquals("Share: $50.0", holder.tvShare.getText().toString());
        assertEquals("Paid: $50.0", holder.tvPaid.getText().toString());
        assertEquals("Balance: $0.0", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithZeroBalance() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 2);

        assertEquals("Balance: $0.0", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithPositiveBalance() {
        List<Split> positiveSplits = new ArrayList<>();
        positiveSplits.add(new Split("david", 50.0, 60.0, 10.0));
        adapter = new SplitAdapter(positiveSplits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Balance: $10.0", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithNegativeBalance() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Balance: $-20.0", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithZeroAmounts() {
        List<Split> zeroSplits = new ArrayList<>();
        zeroSplits.add(new Split("user1", 0.0, 0.0, 0.0));
        adapter = new SplitAdapter(zeroSplits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Share: $0.0", holder.tvShare.getText().toString());
        assertEquals("Paid: $0.0", holder.tvPaid.getText().toString());
        assertEquals("Balance: $0.0", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithLargeAmounts() {
        List<Split> largeSplits = new ArrayList<>();
        largeSplits.add(new Split("user1", 9999.99, 8888.88, -1111.11));
        adapter = new SplitAdapter(largeSplits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Share: $9999.99", holder.tvShare.getText().toString());
        assertEquals("Paid: $8888.88", holder.tvPaid.getText().toString());
        assertEquals("Balance: $-1111.11", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithDecimalAmounts() {
        List<Split> decimalSplits = new ArrayList<>();
        decimalSplits.add(new Split("user1", 33.33, 16.67, -16.66));
        adapter = new SplitAdapter(decimalSplits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Share: $33.33", holder.tvShare.getText().toString());
        assertEquals("Paid: $16.67", holder.tvPaid.getText().toString());
        assertEquals("Balance: $-16.66", holder.tvBalance.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_MultipleBindsOnSameHolder() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);
        assertEquals("User: alice", holder.tvUsername.getText().toString());

        adapter.onBindViewHolder(holder, 1);
        assertEquals("User: bob", holder.tvUsername.getText().toString());

        adapter.onBindViewHolder(holder, 2);
        assertEquals("User: charlie", holder.tvUsername.getText().toString());
    }

    @Test
    public void testViewHolder_Constructor() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder);
        assertNotNull(holder.itemView);
        assertNotNull(holder.tvUsername);
        assertNotNull(holder.tvShare);
        assertNotNull(holder.tvPaid);
        assertNotNull(holder.tvBalance);
    }

    @Test
    public void testViewHolder_UsernameIsTextView() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertTrue(holder.tvUsername instanceof TextView);
    }

    @Test
    public void testViewHolder_ShareIsTextView() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertTrue(holder.tvShare instanceof TextView);
    }

    @Test
    public void testViewHolder_PaidIsTextView() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertTrue(holder.tvPaid instanceof TextView);
    }

    @Test
    public void testViewHolder_BalanceIsTextView() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertTrue(holder.tvBalance instanceof TextView);
    }

    @Test
    public void testViewHolder_AllFieldsInitializedCorrectly() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertEquals(holder.tvUsername, holder.itemView.findViewById(R.id.tvUsername));
        assertEquals(holder.tvShare, holder.itemView.findViewById(R.id.tvShare));
        assertEquals(holder.tvPaid, holder.itemView.findViewById(R.id.tvPaid));
        assertEquals(holder.tvBalance, holder.itemView.findViewById(R.id.tvBalance));
    }

    @Test
    public void testViewHolder_MultipleInstances() {
        adapter = new SplitAdapter(splits);
        ViewGroup parent = createMockParent();

        SplitAdapter.ViewHolder holder1 = adapter.onCreateViewHolder(parent, 0);
        SplitAdapter.ViewHolder holder2 = adapter.onCreateViewHolder(parent, 0);
        SplitAdapter.ViewHolder holder3 = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder1);
        assertNotNull(holder2);
        assertNotNull(holder3);

        assertFalse(holder1 == holder2);
        assertFalse(holder2 == holder3);
        assertFalse(holder1 == holder3);
    }

    @Test
    public void testGetItemCount_AfterModification() {
        adapter = new SplitAdapter(splits);
        int initialCount = adapter.getItemCount();

        splits.add(new Split("newuser", 25.0, 15.0, -10.0));

        assertEquals(initialCount + 1, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_ConsistentResults() {
        adapter = new SplitAdapter(splits);

        int count1 = adapter.getItemCount();
        int count2 = adapter.getItemCount();
        int count3 = adapter.getItemCount();

        assertEquals(count1, count2);
        assertEquals(count2, count3);
    }

    @Test
    public void testAdapter_WithSpecialCharactersInUsername() {
        List<Split> specialSplits = new ArrayList<>();
        specialSplits.add(new Split("user@#$%", 50.0, 30.0, -20.0));

        adapter = new SplitAdapter(specialSplits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("User: user@#$%", holder.tvUsername.getText().toString());
    }

    @Test
    public void testAdapter_WithLongUsername() {
        String longUsername = "verylongusernamethatexceedsnormallength1234567890";
        List<Split> longSplits = new ArrayList<>();
        longSplits.add(new Split(longUsername, 50.0, 30.0, -20.0));

        adapter = new SplitAdapter(longSplits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("User: " + longUsername, holder.tvUsername.getText().toString());
    }

    @Test
    public void testAdapter_WithEmptyUsername() {
        List<Split> emptySplits = new ArrayList<>();
        emptySplits.add(new Split("", 50.0, 30.0, -20.0));

        adapter = new SplitAdapter(emptySplits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("User: ", holder.tvUsername.getText().toString());
    }

    @Test
    public void testConstructor_WithLargeList() {
        List<Split> largeSplits = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeSplits.add(new Split("user" + i, 10.0, 5.0, -5.0));
        }

        adapter = new SplitAdapter(largeSplits);
        assertEquals(100, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_NeverNegative() {
        adapter = new SplitAdapter(splits);
        assertTrue(adapter.getItemCount() >= 0);

        adapter = new SplitAdapter(new ArrayList<>());
        assertTrue(adapter.getItemCount() >= 0);
    }

    @Test
    public void testOnBindViewHolder_DataIntegrity() {
        adapter = new SplitAdapter(splits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        for (int i = 0; i < splits.size(); i++) {
            adapter.onBindViewHolder(holder, i);
            Split split = splits.get(i);

            assertEquals("User: " + split.getUsername(), holder.tvUsername.getText().toString());
            assertEquals("Share: $" + split.getShareAmount(), holder.tvShare.getText().toString());
            assertEquals("Paid: $" + split.getPaidAmount(), holder.tvPaid.getText().toString());
            assertEquals("Balance: $" + split.getBalance(), holder.tvBalance.getText().toString());
        }
    }

    @Test
    public void testAdapter_WithNegativeAmounts() {
        List<Split> negativeSplits = new ArrayList<>();
        negativeSplits.add(new Split("user1", -50.0, -30.0, -20.0));

        adapter = new SplitAdapter(negativeSplits);
        SplitAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Share: $-50.0", holder.tvShare.getText().toString());
        assertEquals("Paid: $-30.0", holder.tvPaid.getText().toString());
        assertEquals("Balance: $-20.0", holder.tvBalance.getText().toString());
    }
}