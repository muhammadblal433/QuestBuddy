package com.example.androidexample.budget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
public class SplitEditAdapterTest {

    private SplitEditAdapter adapter;
    private List<Split> splits;
    private static final String CURRENT_USER = "testuser";
    private static final String OTHER_USER = "otheruser";

    @Before
    public void setUp() {
        splits = new ArrayList<>();
        splits.add(new Split(CURRENT_USER, 50.0, 30.0, -20.0));
        splits.add(new Split(OTHER_USER, 50.0, 40.0, -10.0));
        splits.add(new Split("thirduser", 50.0, 50.0, 0.0));
    }

    private ViewGroup createMockParent() {
        return new FrameLayout(ApplicationProvider.getApplicationContext());
    }

    private SplitEditAdapter.ViewHolder createViewHolder() {
        ViewGroup parent = createMockParent();
        return adapter.onCreateViewHolder(parent, 0);
    }

    @Test
    public void testConstructor_WithValidData() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        assertNotNull(adapter);
    }

    @Test
    public void testConstructor_AsOwner() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        assertNotNull(adapter);
    }

    @Test
    public void testConstructor_AsNonOwner() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, false);
        assertNotNull(adapter);
    }

    @Test
    public void testGetItemCount_ReturnsCorrectCount() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_WithEmptyList() {
        adapter = new SplitEditAdapter(new ArrayList<>(), CURRENT_USER, true);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_WithSingleItem() {
        List<Split> singleSplit = new ArrayList<>();
        singleSplit.add(new Split(CURRENT_USER, 100.0, 50.0, -50.0));
        adapter = new SplitEditAdapter(singleSplit, CURRENT_USER, true);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testGetUpdatedSplits_ReturnsOriginalList() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        List<Split> updated = adapter.getUpdatedSplits();
        assertEquals(splits, updated);
        assertEquals(3, updated.size());
    }

    @Test
    public void testGetUpdatedSplits_SameReference() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        List<Split> updated = adapter.getUpdatedSplits();
        assertTrue(splits == updated);
    }

    @Test
    public void testGetUpdatedSplits_ContainsCorrectData() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        List<Split> updated = adapter.getUpdatedSplits();

        assertEquals(CURRENT_USER, updated.get(0).getUsername());
        assertEquals(50.0, updated.get(0).getShareAmount(), 0.01);
        assertEquals(30.0, updated.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testGetItemCount_AfterModification() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        int initialCount = adapter.getItemCount();

        splits.add(new Split("newuser", 25.0, 15.0, -10.0));

        assertEquals(initialCount + 1, adapter.getItemCount());
    }

    @Test
    public void testConstructor_WithNullUser() {
        adapter = new SplitEditAdapter(splits, null, true);
        assertNotNull(adapter);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testConstructor_WithEmptyUser() {
        adapter = new SplitEditAdapter(splits, "", true);
        assertNotNull(adapter);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testGetUpdatedSplits_WithZeroAmounts() {
        List<Split> zeroSplits = new ArrayList<>();
        zeroSplits.add(new Split(CURRENT_USER, 0.0, 0.0, 0.0));
        adapter = new SplitEditAdapter(zeroSplits, CURRENT_USER, true);

        List<Split> updated = adapter.getUpdatedSplits();
        assertEquals(0.0, updated.get(0).getShareAmount(), 0.01);
        assertEquals(0.0, updated.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testGetUpdatedSplits_WithNegativeAmounts() {
        List<Split> negativeSplits = new ArrayList<>();
        negativeSplits.add(new Split(CURRENT_USER, -50.0, -30.0, -20.0));
        adapter = new SplitEditAdapter(negativeSplits, CURRENT_USER, true);

        List<Split> updated = adapter.getUpdatedSplits();
        assertEquals(-50.0, updated.get(0).getShareAmount(), 0.01);
        assertEquals(-30.0, updated.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testGetUpdatedSplits_WithLargeAmounts() {
        List<Split> largeSplits = new ArrayList<>();
        largeSplits.add(new Split(CURRENT_USER, 9999.99, 8888.88, -1111.11));
        adapter = new SplitEditAdapter(largeSplits, CURRENT_USER, true);

        List<Split> updated = adapter.getUpdatedSplits();
        assertEquals(9999.99, updated.get(0).getShareAmount(), 0.01);
        assertEquals(8888.88, updated.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testGetUpdatedSplits_WithDecimalAmounts() {
        List<Split> decimalSplits = new ArrayList<>();
        decimalSplits.add(new Split(CURRENT_USER, 33.33, 16.67, -16.66));
        adapter = new SplitEditAdapter(decimalSplits, CURRENT_USER, true);

        List<Split> updated = adapter.getUpdatedSplits();
        assertEquals(33.33, updated.get(0).getShareAmount(), 0.01);
        assertEquals(16.67, updated.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testGetItemCount_WithMultipleUsers() {
        List<Split> multiUsers = new ArrayList<>();
        multiUsers.add(new Split("user1", 25.0, 10.0, -15.0));
        multiUsers.add(new Split("user2", 25.0, 15.0, -10.0));
        multiUsers.add(new Split("user3", 25.0, 20.0, -5.0));
        multiUsers.add(new Split("user4", 25.0, 25.0, 0.0));

        adapter = new SplitEditAdapter(multiUsers, "user1", true);
        assertEquals(4, adapter.getItemCount());
    }

    @Test
    public void testAdapter_OwnerFlag_True() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        assertNotNull(adapter);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testAdapter_OwnerFlag_False() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, false);
        assertNotNull(adapter);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testGetUpdatedSplits_MultipleUsers() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        List<Split> updated = adapter.getUpdatedSplits();

        assertEquals(CURRENT_USER, updated.get(0).getUsername());
        assertEquals(OTHER_USER, updated.get(1).getUsername());
        assertEquals("thirduser", updated.get(2).getUsername());
    }

    @Test
    public void testGetUpdatedSplits_PreservesBalance() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        List<Split> updated = adapter.getUpdatedSplits();

        assertEquals(-20.0, updated.get(0).getBalance(), 0.01);
        assertEquals(-10.0, updated.get(1).getBalance(), 0.01);
        assertEquals(0.0, updated.get(2).getBalance(), 0.01);
    }

    @Test
    public void testConstructor_WithLargeList() {
        List<Split> largeSplits = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeSplits.add(new Split("user" + i, 10.0, 5.0, -5.0));
        }

        adapter = new SplitEditAdapter(largeSplits, CURRENT_USER, true);
        assertEquals(100, adapter.getItemCount());
    }

    @Test
    public void testGetUpdatedSplits_EmptyList() {
        List<Split> emptySplits = new ArrayList<>();
        adapter = new SplitEditAdapter(emptySplits, CURRENT_USER, true);

        List<Split> updated = adapter.getUpdatedSplits();
        assertEquals(0, updated.size());
    }

    @Test
    public void testGetItemCount_ConsistentResults() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);

        int count1 = adapter.getItemCount();
        int count2 = adapter.getItemCount();
        int count3 = adapter.getItemCount();

        assertEquals(count1, count2);
        assertEquals(count2, count3);
    }

    @Test
    public void testGetUpdatedSplits_ConsistentResults() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);

        List<Split> updated1 = adapter.getUpdatedSplits();
        List<Split> updated2 = adapter.getUpdatedSplits();

        assertEquals(updated1, updated2);
        assertTrue(updated1 == updated2);
    }

    @Test
    public void testConstructor_DifferentUsernames() {
        adapter = new SplitEditAdapter(splits, "differentuser", false);
        assertNotNull(adapter);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testGetUpdatedSplits_DataIntegrity() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        List<Split> updated = adapter.getUpdatedSplits();

        for (int i = 0; i < splits.size(); i++) {
            assertEquals(splits.get(i).getUsername(), updated.get(i).getUsername());
            assertEquals(splits.get(i).getShareAmount(), updated.get(i).getShareAmount(), 0.01);
            assertEquals(splits.get(i).getPaidAmount(), updated.get(i).getPaidAmount(), 0.01);
            assertEquals(splits.get(i).getBalance(), updated.get(i).getBalance(), 0.01);
        }
    }

    @Test
    public void testAdapter_WithSpecialCharactersInUsername() {
        List<Split> specialSplits = new ArrayList<>();
        specialSplits.add(new Split("user@#$%", 50.0, 30.0, -20.0));

        adapter = new SplitEditAdapter(specialSplits, "user@#$%", true);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testAdapter_WithLongUsername() {
        String longUsername = "verylongusernamethatexceedsnormallength1234567890";
        List<Split> longSplits = new ArrayList<>();
        longSplits.add(new Split(longUsername, 50.0, 30.0, -20.0));

        adapter = new SplitEditAdapter(longSplits, longUsername, true);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testGetUpdatedSplits_AfterListModification() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);

        splits.get(0).setShareAmount(100.0);
        splits.get(0).setPaidAmount(75.0);

        List<Split> updated = adapter.getUpdatedSplits();
        assertEquals(100.0, updated.get(0).getShareAmount(), 0.01);
        assertEquals(75.0, updated.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testConstructor_AllPermutations() {
        SplitEditAdapter adapter1 = new SplitEditAdapter(splits, CURRENT_USER, true);
        assertNotNull(adapter1);

        SplitEditAdapter adapter2 = new SplitEditAdapter(splits, CURRENT_USER, false);
        assertNotNull(adapter2);

        SplitEditAdapter adapter3 = new SplitEditAdapter(splits, OTHER_USER, true);
        assertNotNull(adapter3);

        SplitEditAdapter adapter4 = new SplitEditAdapter(splits, OTHER_USER, false);
        assertNotNull(adapter4);
    }

    @Test
    public void testGetItemCount_NeverNegative() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        assertTrue(adapter.getItemCount() >= 0);

        adapter = new SplitEditAdapter(new ArrayList<>(), CURRENT_USER, true);
        assertTrue(adapter.getItemCount() >= 0);
    }

    @Test
    public void testOnCreateViewHolder_CreatesViewHolder() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        ViewGroup parent = createMockParent();

        SplitEditAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder);
        assertNotNull(holder.itemView);
    }

    @Test
    public void testOnCreateViewHolder_HasAllViews() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        ViewGroup parent = createMockParent();

        SplitEditAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder.tvUsername);
        assertNotNull(holder.etShare);
        assertNotNull(holder.etPaid);
    }

    @Test
    public void testOnBindViewHolder_SetsUsernameText() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("User: " + CURRENT_USER, holder.tvUsername.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_SetsShareAmountText() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("50.0", holder.etShare.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_SetsPaidAmountText() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("30.0", holder.etPaid.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_OwnerCanEditAllFields() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 1);

        assertTrue(holder.etShare.isEnabled());
        assertTrue(holder.etPaid.isEnabled());
    }

    @Test
    public void testOnBindViewHolder_NonOwnerCanEditOwnFields() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, false);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertTrue(holder.etShare.isEnabled());
        assertTrue(holder.etPaid.isEnabled());
    }

    @Test
    public void testOnBindViewHolder_NonOwnerCannotEditOthersFields() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, false);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 1);

        assertFalse(holder.etShare.isEnabled());
        assertFalse(holder.etPaid.isEnabled());
    }

    @Test
    public void testOnBindViewHolder_SecondPosition() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 1);

        assertEquals("User: " + OTHER_USER, holder.tvUsername.getText().toString());
        assertEquals("50.0", holder.etShare.getText().toString());
        assertEquals("40.0", holder.etPaid.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_ThirdPosition() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 2);

        assertEquals("User: thirduser", holder.tvUsername.getText().toString());
        assertEquals("50.0", holder.etShare.getText().toString());
        assertEquals("50.0", holder.etPaid.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithZeroAmounts() {
        List<Split> zeroSplits = new ArrayList<>();
        zeroSplits.add(new Split(CURRENT_USER, 0.0, 0.0, 0.0));
        adapter = new SplitEditAdapter(zeroSplits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("0.0", holder.etShare.getText().toString());
        assertEquals("0.0", holder.etPaid.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithNegativeAmounts() {
        List<Split> negativeSplits = new ArrayList<>();
        negativeSplits.add(new Split(CURRENT_USER, -50.0, -30.0, -20.0));
        adapter = new SplitEditAdapter(negativeSplits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("-50.0", holder.etShare.getText().toString());
        assertEquals("-30.0", holder.etPaid.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithLargeAmounts() {
        List<Split> largeSplits = new ArrayList<>();
        largeSplits.add(new Split(CURRENT_USER, 9999.99, 8888.88, -1111.11));
        adapter = new SplitEditAdapter(largeSplits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("9999.99", holder.etShare.getText().toString());
        assertEquals("8888.88", holder.etPaid.getText().toString());
    }

    @Test
    public void testOnBindViewHolder_WithDecimalAmounts() {
        List<Split> decimalSplits = new ArrayList<>();
        decimalSplits.add(new Split(CURRENT_USER, 33.33, 16.67, -16.66));
        adapter = new SplitEditAdapter(decimalSplits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertEquals("33.33", holder.etShare.getText().toString());
        assertEquals("16.67", holder.etPaid.getText().toString());
    }

    @Test
    public void testTextWatcher_ShareAmount_UpdatesSplit() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etShare.setText("75.5");

        assertEquals(75.5, splits.get(0).getShareAmount(), 0.01);
    }

    @Test
    public void testTextWatcher_PaidAmount_UpdatesSplit() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etPaid.setText("45.25");

        assertEquals(45.25, splits.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testTextWatcher_InvalidShareInput_SetsZero() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etShare.setText("invalid");

        assertEquals(0.0, splits.get(0).getShareAmount(), 0.01);
    }

    @Test
    public void testTextWatcher_InvalidPaidInput_SetsZero() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etPaid.setText("abc");

        assertEquals(0.0, splits.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testTextWatcher_EmptyShareInput_SetsZero() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etShare.setText("");

        assertEquals(0.0, splits.get(0).getShareAmount(), 0.01);
    }

    @Test
    public void testTextWatcher_EmptyPaidInput_SetsZero() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etPaid.setText("");

        assertEquals(0.0, splits.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testTextWatcher_NegativeShareInput_AcceptsValue() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etShare.setText("-25.0");

        assertEquals(-25.0, splits.get(0).getShareAmount(), 0.01);
    }

    @Test
    public void testTextWatcher_NegativePaidInput_AcceptsValue() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etPaid.setText("-15.5");

        assertEquals(-15.5, splits.get(0).getPaidAmount(), 0.01);
    }

    @Test
    public void testOnBindViewHolder_CaseInsensitiveUsernameMatch() {
        adapter = new SplitEditAdapter(splits, "TESTUSER", false);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);

        assertTrue(holder.etShare.isEnabled());
        assertTrue(holder.etPaid.isEnabled());
    }

    @Test
    public void testOnBindViewHolder_MultipleBindsOnSameHolder() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();

        adapter.onBindViewHolder(holder, 0);
        assertEquals("User: " + CURRENT_USER, holder.tvUsername.getText().toString());

        adapter.onBindViewHolder(holder, 1);
        assertEquals("User: " + OTHER_USER, holder.tvUsername.getText().toString());

        adapter.onBindViewHolder(holder, 2);
        assertEquals("User: thirduser", holder.tvUsername.getText().toString());
    }

    @Test
    public void testViewHolder_Constructor() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        ViewGroup parent = createMockParent();

        SplitEditAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder);
        assertNotNull(holder.itemView);
        assertNotNull(holder.tvUsername);
        assertNotNull(holder.etShare);
        assertNotNull(holder.etPaid);
    }

    @Test
    public void testTextWatcher_MultipleChanges() {
        adapter = new SplitEditAdapter(splits, CURRENT_USER, true);
        SplitEditAdapter.ViewHolder holder = createViewHolder();
        adapter.onBindViewHolder(holder, 0);

        holder.etShare.setText("100.0");
        assertEquals(100.0, splits.get(0).getShareAmount(), 0.01);

        holder.etShare.setText("200.0");
        assertEquals(200.0, splits.get(0).getShareAmount(), 0.01);

        holder.etShare.setText("300.5");
        assertEquals(300.5, splits.get(0).getShareAmount(), 0.01);
    }
}
