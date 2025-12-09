package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.friends.Friend;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FriendTest {

    private Friend friend;

    @Before
    public void setUp() {
        friend = new Friend();
    }

    @Test
    public void getId_setId_returnsCorrectValue() {
        friend.setId(123L);
        assertEquals(123L, friend.getId());
    }

    @Test
    public void getCurrentId_setCurrentUserID_returnsCorrectValue() {
        friend.setCurrentUserID(456L);
        assertEquals(456L, friend.getCurrentId());
    }

    @Test
    public void getDisplayName_setDisplayName_returnsCorrectValue() {
        friend.setDisplayName("John Doe");
        assertEquals("John Doe", friend.getDisplayName());
    }

    @Test
    public void getUsername_setUsername_returnsCorrectValue() {
        friend.setUsername("johndoe");
        assertEquals("johndoe", friend.getUsername());
    }

    @Test
    public void getStatus_setStatus_returnsCorrectValue() {
        friend.setStatus("ONLINE");
        assertEquals("ONLINE", friend.getStatus());
    }

    @Test
    public void getEmail_setEmail_returnsCorrectValue() {
        friend.setEmail("john@example.com");
        assertEquals("john@example.com", friend.getEmail());
    }

    @Test
    public void getMutualCount_setMutualCount_returnsCorrectValue() {
        friend.setMutualCount(5);
        assertEquals(5, friend.getMutualCount());
    }

    @Test
    public void isIncoming_setIncoming_returnsCorrectValue() {
        friend.setIncoming(true);
        assertTrue(friend.isIncoming());

        friend.setIncoming(false);
        assertFalse(friend.isIncoming());
    }

    @Test
    public void friend_defaultValues() {
        Friend newFriend = new Friend();
        assertEquals(0L, newFriend.getId());
        assertEquals(0L, newFriend.getCurrentId());
        assertEquals(0, newFriend.getMutualCount());
        assertFalse(newFriend.isIncoming());
    }

    @Test
    public void setId_withZero_stores() {
        friend.setId(0L);
        assertEquals(0L, friend.getId());
    }

    @Test
    public void setId_withNegative_stores() {
        friend.setId(-1L);
        assertEquals(-1L, friend.getId());
    }

    @Test
    public void setId_withLargeValue_stores() {
        friend.setId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, friend.getId());
    }

    @Test
    public void setCurrentUserID_withZero_stores() {
        friend.setCurrentUserID(0L);
        assertEquals(0L, friend.getCurrentId());
    }

    @Test
    public void setCurrentUserID_withNegative_stores() {
        friend.setCurrentUserID(-1L);
        assertEquals(-1L, friend.getCurrentId());
    }

    @Test
    public void setDisplayName_withEmptyString_stores() {
        friend.setDisplayName("");
        assertEquals("", friend.getDisplayName());
    }

    @Test
    public void setDisplayName_withNull_stores() {
        friend.setDisplayName(null);
        assertEquals(null, friend.getDisplayName());
    }

    @Test
    public void setUsername_withEmptyString_stores() {
        friend.setUsername("");
        assertEquals("", friend.getUsername());
    }

    @Test
    public void setUsername_withSpecialCharacters_stores() {
        friend.setUsername("user_name-123");
        assertEquals("user_name-123", friend.getUsername());
    }

    @Test
    public void setStatus_withDifferentStatuses() {
        String[] statuses = {"ONLINE", "OFFLINE", "PENDING", "ACCEPTED", "BLOCKED"};
        for (String status : statuses) {
            friend.setStatus(status);
            assertEquals(status, friend.getStatus());
        }
    }

    @Test
    public void setEmail_withValidEmail_stores() {
        friend.setEmail("test@example.com");
        assertEquals("test@example.com", friend.getEmail());
    }

    @Test
    public void setEmail_withEmptyString_stores() {
        friend.setEmail("");
        assertEquals("", friend.getEmail());
    }

    @Test
    public void setMutualCount_withZero_stores() {
        friend.setMutualCount(0);
        assertEquals(0, friend.getMutualCount());
    }

    @Test
    public void setMutualCount_withLargeNumber_stores() {
        friend.setMutualCount(1000);
        assertEquals(1000, friend.getMutualCount());
    }

    @Test
    public void setMutualCount_withNegative_stores() {
        friend.setMutualCount(-1);
        assertEquals(-1, friend.getMutualCount());
    }

    @Test
    public void isIncoming_toggleMultipleTimes() {
        friend.setIncoming(true);
        assertTrue(friend.isIncoming());

        friend.setIncoming(false);
        assertFalse(friend.isIncoming());

        friend.setIncoming(true);
        assertTrue(friend.isIncoming());
    }

    @Test
    public void allFields_setAndGet() {
        friend.setId(100L);
        friend.setCurrentUserID(200L);
        friend.setDisplayName("Alice Smith");
        friend.setUsername("alice123");
        friend.setStatus("ACCEPTED");
        friend.setEmail("alice@test.com");
        friend.setMutualCount(3);
        friend.setIncoming(true);

        assertEquals(100L, friend.getId());
        assertEquals(200L, friend.getCurrentId());
        assertEquals("Alice Smith", friend.getDisplayName());
        assertEquals("alice123", friend.getUsername());
        assertEquals("ACCEPTED", friend.getStatus());
        assertEquals("alice@test.com", friend.getEmail());
        assertEquals(3, friend.getMutualCount());
        assertTrue(friend.isIncoming());
    }

    @Test
    public void multipleFriendInstances_independent() {
        Friend friend1 = new Friend();
        Friend friend2 = new Friend();

        friend1.setId(1L);
        friend1.setUsername("user1");

        friend2.setId(2L);
        friend2.setUsername("user2");

        assertEquals(1L, friend1.getId());
        assertEquals("user1", friend1.getUsername());
        assertEquals(2L, friend2.getId());
        assertEquals("user2", friend2.getUsername());
    }
}