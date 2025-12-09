package com.questbuddy.user.service;

import com.questbuddy.user.model.Role;
import com.questbuddy.user.model.User;
import com.questbuddy.user.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplementedTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserServiceImplemented service;

    @Test
    public void signup_success_createsUserWithHashedPasswordAndDefaults() {
        when(userRepo.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("ayaan")).thenReturn(false);
        when(encoder.encode("secret")).thenReturn("HASHED");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(42L);
            return u;
        });

        User result = service.signup("test@example.com", "ayaan", "secret", "Ayaan", "Syed");

        assertNotNull(result);
        assertEquals(Long.valueOf(42L), result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("ayaan", result.getUsername());
        assertEquals("HASHED", result.getPasswordHash());
        assertEquals("HASHED", result.getPassword());
        assertEquals(Role.TRIP_MEMBER, result.getRole());
        assertTrue(result.isActive());

        verify(userRepo).existsByEmail("test@example.com");
        verify(userRepo).existsByUsername("ayaan");
        verify(encoder).encode("secret");
        verify(userRepo).save(any(User.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void signup_throwsWhenEmailMissing() {
        service.signup(null, "user", "pw", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void signup_throwsWhenUsernameMissing() {
        service.signup("test@example.com", null, "pw", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void signup_throwsWhenPasswordMissing() {
        service.signup("test@example.com", "user", null, null, null);
    }

    @Test
    public void signup_throwsWhenEmailAlreadyExists() {
        when(userRepo.existsByEmail("dup@example.com")).thenReturn(true);

        try {
            service.signup("dup@example.com", "user", "pw", null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("dup@example.com"));
        }

        verify(userRepo).existsByEmail("dup@example.com");
        verify(userRepo, never()).existsByUsername(anyString());
    }

    @Test
    public void signup_throwsWhenUsernameAlreadyExists() {
        when(userRepo.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("dupuser")).thenReturn(true);

        try {
            service.signup("test@example.com", "dupuser", "pw", null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("dupuser"));
        }

        verify(userRepo).existsByEmail("test@example.com");
        verify(userRepo).existsByUsername("dupuser");
    }

    // ---------- getById ----------

    @Test
    public void getById_delegatesToRepository() {
        User u = new User();
        u.setId(10L);
        when(userRepo.findById(10L)).thenReturn(Optional.of(u));

        Optional<User> result = service.getById(10L);

        assertTrue(result.isPresent());
        assertEquals(Long.valueOf(10L), result.get().getId());
        verify(userRepo).findById(10L);
    }


    private User userWithIdEmailUsername(long id, String email, String username) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPasswordHash("HASH");
        u.setPassword("HASH");
        return u;
    }

    @Test
    public void updateProfile_throwsWhenUserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        try {
            service.updateProfile(1L, "new@example.com", null, null, null, null);
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertEquals("No such user found.", e.getMessage());
        }

        verify(userRepo).findById(1L);
    }

//    @Test
//    public void updateProfile_doesNotChangeEmailOrUsernameWhenSameIgnoreCase() {
//        User existing = userWithIdEmailUsername(5L, "user@example.com", "ayaan");
//        when(userRepo.findById(5L)).thenReturn(Optional.of(existing));
//        when(userRepo.save(existing)).thenReturn(existing);
//
//        User result = service.updateProfile(
//                5L,
//                "USER@example.com",
//                "AYAAN",
//                "NewFirst",
//                "NewLast",
//                "http://avatar"
//        );
//
//        assertEquals("user@example.com", result.getEmail());
//        assertEquals("ayaan", result.getUsername());
//        assertEquals("NewFirst", result.getFirstName());
//        assertEquals("NewLast", result.getLastName());
//        assertEquals("http://avatar", result.getAvatarUrl());
//
//        verify(userRepo, never()).existsByEmail(anyString());
//        verify(userRepo, never()).existsByUsername(anyString());
//        verify(userRepo).save(existing);
//    }

    @Test
    public void updateProfile_throwsWhenNewEmailAlreadyInUse() {
        User existing = userWithIdEmailUsername(5L, "old@example.com", "user");
        when(userRepo.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepo.existsByEmail("new@example.com")).thenReturn(true);

        try {
            service.updateProfile(5L, "new@example.com", null, null, null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("This email has been taken.", e.getMessage());
        }

        verify(userRepo).findById(5L);
        verify(userRepo).existsByEmail("new@example.com");
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    public void updateProfile_throwsWhenNewUsernameAlreadyInUse() {
        User existing = userWithIdEmailUsername(5L, "user@example.com", "olduser");
        when(userRepo.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepo.existsByUsername("newuser")).thenReturn(true);

        try {
            service.updateProfile(5L, null, "newuser", null, null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("This username has been taken.", e.getMessage());
        }

        verify(userRepo).findById(5L);
        verify(userRepo).existsByUsername("newuser");
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    public void updateProfile_updatesFieldsAndSaves() {
        User existing = userWithIdEmailUsername(5L, "old@example.com", "olduser");
        when(userRepo.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepo.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.save(existing)).thenReturn(existing);

        User result = service.updateProfile(
                5L,
                "new@example.com",
                "newuser",
                "First",
                "Last",
                "http://avatar"
        );

        assertEquals("new@example.com", result.getEmail());
        assertEquals("newuser", result.getUsername());
        assertEquals("First", result.getFirstName());
        assertEquals("Last", result.getLastName());
        assertEquals("http://avatar", result.getAvatarUrl());

        verify(userRepo).existsByEmail("new@example.com");
        verify(userRepo).existsByUsername("newuser");
        verify(userRepo).save(existing);
    }

    @Test
    public void login_returnsEmptyWhenEmailNull() {
        Optional<User> result = service.login(null, "pw");

        assertFalse(result.isPresent());
        verifyNoInteractions(userRepo);
    }

    @Test
    public void login_returnsEmptyWhenPasswordNull() {
        Optional<User> result = service.login("test@example.com", null);

        assertFalse(result.isPresent());
        verifyNoInteractions(userRepo);
    }

    @Test
    public void login_returnsEmptyWhenUserNotFound() {
        when(userRepo.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.empty());

        Optional<User> result = service.login("test@example.com", "pw");

        assertFalse(result.isPresent());
        verify(userRepo).findByEmailIgnoreCase("test@example.com");
    }

    @Test
    public void login_returnsEmptyWhenPasswordDoesNotMatch() {
        User u = new User();
        u.setEmail("test@example.com");
        u.setUsername("user");
        u.setPasswordHash("HASH");
        u.setPassword("HASH");
        when(userRepo.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("pw", "HASH")).thenReturn(false);

        Optional<User> result = service.login("test@example.com", "pw");

        assertFalse(result.isPresent());
        verify(userRepo).findByEmailIgnoreCase("test@example.com");
        verify(encoder).matches("pw", "HASH");
    }

    @Test
    public void login_success_returnsUser() {
        User u = new User();
        u.setEmail("test@example.com");
        u.setUsername("user");
        u.setPasswordHash("HASH");
        u.setPassword("HASH");
        when(userRepo.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("pw", "HASH")).thenReturn(true);

        Optional<User> result = service.login("test@example.com", "pw");

        assertTrue(result.isPresent());
        assertEquals(u, result.get());
        verify(userRepo).findByEmailIgnoreCase("test@example.com");
        verify(encoder).matches("pw", "HASH");
    }

    // ---------- findByEmail / findByUsername ----------

    @Test
    public void findByEmail_null_returnsEmpty() {
        Optional<User> result = service.findByEmail(null);

        assertFalse(result.isPresent());
        verifyNoInteractions(userRepo);
    }

    @Test
    public void findByEmail_nonNull_trimsAndDelegates() {
        User u = new User();
        u.setEmail("x@example.com");
        when(userRepo.findByEmailIgnoreCase("x@example.com")).thenReturn(Optional.of(u));

        Optional<User> result = service.findByEmail("  x@example.com  ");

        assertTrue(result.isPresent());
        assertEquals(u, result.get());
        verify(userRepo).findByEmailIgnoreCase("x@example.com");
    }

    @Test
    public void findByUsername_null_returnsEmpty() {
        Optional<User> result = service.findByUsername(null);

        assertFalse(result.isPresent());
        verifyNoInteractions(userRepo);
    }

    @Test
    public void findByUsername_nonNull_trimsAndDelegates() {
        User u = new User();
        u.setUsername("ayaan");
        when(userRepo.findByUsernameIgnoreCase("ayaan")).thenReturn(Optional.of(u));

        Optional<User> result = service.findByUsername("  ayaan  ");

        assertTrue(result.isPresent());
        assertEquals(u, result.get());
        verify(userRepo).findByUsernameIgnoreCase("ayaan");
    }

    @Test
    public void save_delegatesToRepository() {
        User u = new User();
        when(userRepo.save(u)).thenReturn(u);

        User result = service.save(u);

        assertEquals(u, result);
        verify(userRepo).save(u);
    }

    @Test
    public void findPremiumUsers_delegatesToRepository() {
        User u1 = new User();
        u1.setPremiumUser(true);
        User u2 = new User();
        u2.setPremiumUser(true);
        List<User> list = List.of(u1, u2);

        when(userRepo.findAllByIsPremiumTrueOrderByIdAsc()).thenReturn(list);

        List<User> result = service.findPremiumUsers();

        assertEquals(2, result.size());
        verify(userRepo).findAllByIsPremiumTrueOrderByIdAsc();
    }
}
