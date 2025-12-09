package com.questbuddy.user.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserTest {
    @Test
    public void setEmail_valid_trimsAndSets() {
        User u = new User();
        u.setEmail("  test@example.com  ");

        assertEquals("test@example.com", u.getEmail());
        assertNotNull(u.getUpdatedAt());
    }

    @Test
    public void setEmail_null_throws() {
        User u = new User();
        try {
            u.setEmail(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("email required", e.getMessage());
        }
    }

    @Test
    public void setEmail_blank_throws() {
        User u = new User();
        try {
            u.setEmail("   ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("email required", e.getMessage());
        }
    }

    @Test
    public void setEmail_invalidFormat_throws() {
        User u = new User();
        try {
            u.setEmail("not-an-email");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("email format invalid", e.getMessage());
        }
    }

    // -------- username --------

    @Test
    public void setUsername_valid_trimsAndSets() {
        User u = new User();
        u.setUsername("  ayaan  ");

        assertEquals("ayaan", u.getUsername());
    }

    @Test
    public void setUsername_null_throws() {
        User u = new User();
        try {
            u.setUsername(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("username required", e.getMessage());
        }
    }

    @Test
    public void setUsername_blank_throws() {
        User u = new User();
        try {
            u.setUsername("   ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("username required", e.getMessage());
        }
    }

    @Test
    public void setUsername_tooShort_throws() {
        User u = new User();
        try {
            u.setUsername("ab"); // length 2 < 3
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("username length must be 3-32 characters long", e.getMessage());
        }
    }

    @Test
    public void setUsername_tooLong_throws() {
        User u = new User();
        String longName = "a".repeat(33); // length 33 > 32
        try {
            u.setUsername(longName);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("username length must be 3-32 characters long", e.getMessage());
        }
    }

    // -------- password hash / password --------

    @Test
    public void setPasswordHash_null_throws() {
        User u = new User();
        try {
            u.setPasswordHash(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("passwordHash required", e.getMessage());
        }
    }

    @Test
    public void setPasswordHash_blank_throws() {
        User u = new User();
        try {
            u.setPasswordHash("   ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("passwordHash required", e.getMessage());
        }
    }

    @Test
    public void setPasswordHash_valid_setsValue() {
        User u = new User();
        u.setPasswordHash("HASHED");

        assertEquals("HASHED", u.getPasswordHash());
    }

    @Test
    public void setPassword_null_throws() {
        User u = new User();
        try {
            u.setPassword(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("password required", e.getMessage());
        }
    }

    @Test
    public void setPassword_blank_throws() {
        User u = new User();
        try {
            u.setPassword("  ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("password required", e.getMessage());
        }
    }

    @Test
    public void setPassword_valid_setsValue() {
        User u = new User();
        u.setPassword("secret");

        assertEquals("secret", u.getPassword());
    }

    // -------- names / avatar --------

    @Test
    public void setFirstName_blank_clearsValue() {
        User u = new User();
        u.setFirstName("Ayaan");
        u.setFirstName("  "); // should clear

        assertNull(u.getFirstName());
    }

    @Test
    public void setFirstName_nonBlank_trims() {
        User u = new User();
        u.setFirstName("  Ayaan  ");

        assertEquals("Ayaan", u.getFirstName());
    }

    @Test
    public void setLastName_blank_clearsValue() {
        User u = new User();
        u.setLastName("Syed");
        u.setLastName("  ");

        assertNull(u.getLastName());
    }

    @Test
    public void setLastName_nonBlank_trims() {
        User u = new User();
        u.setLastName("  Syed  ");

        assertEquals("Syed", u.getLastName());
    }

    @Test
    public void setAvatarUrl_blank_clearsValue() {
        User u = new User();
        u.setAvatarUrl("http://some/url");
        u.setAvatarUrl("   ");

        assertNull(u.getAvatarUrl());
    }

    @Test
    public void setAvatarUrl_nonBlank_trims() {
        User u = new User();
        u.setAvatarUrl("  http://img  ");

        assertEquals("http://img", u.getAvatarUrl());
    }

    // -------- timestamps / flags / role --------

    @Test
    public void setCreatedAt_null_setsNow() {
        User u = new User();
        u.setCreatedAt(null);

        assertNotNull(u.getCreatedAt());
    }

    @Test
    public void setCreatedAt_nonNull_setsGivenValue() {
        User u = new User();
        Instant now = Instant.now();
        u.setCreatedAt(now);

        assertEquals(now, u.getCreatedAt());
    }

    @Test
    public void setUpdatedAt_nonNull_setsGivenValue() {
        User u = new User();
        Instant now = Instant.now();
        u.setUpdatedAt(now);

        assertEquals(now, u.getUpdatedAt());
    }

    @Test
    public void setRole_setsGivenValue() {
        User u = new User();
        u.setRole(Role.ADMIN);

        assertEquals(Role.ADMIN, u.getRole());
    }

    @Test
    public void setActive_updatesFlag() {
        User u = new User();
        u.setActive(false);

        assertFalse(u.isActive());
        u.setActive(true);
        assertTrue(u.isActive());
    }

    @Test
    public void setPremiumUser_updatesFlag() {
        User u = new User();
        assertFalse(u.isPremiumUser());

        u.setPremiumUser(true);
        assertTrue(u.isPremiumUser());
    }

    @Test
    public void setId_setsId() {
        User u = new User();
        u.setId(42L);

        assertEquals(Long.valueOf(42L), u.getId());
    }
}
