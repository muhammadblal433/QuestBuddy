package com.questbuddy.user.controller;

import com.questbuddy.user.model.User;
import com.questbuddy.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthController authController;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void login_missingFields_returns400() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("missing_fields"));

        verifyNoInteractions(userService);
    }

    @Test
    public void login_invalidCredentials_returns401() throws Exception {
        when(userService.login("test@example.com", "pw")).thenReturn(Optional.empty());

        String json = "{ \"email\": \"test@example.com\", \"password\": \"pw\" }";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_credentials"));

        verify(userService).login("test@example.com", "pw");
    }

    @Test
    public void login_success_returnsUserId() throws Exception {
        User u = new User();
        u.setId(123L);
        when(userService.login("user@example.com", "pw")).thenReturn(Optional.of(u));

        String json = "{ \"email\": \"user@example.com\", \"password\": \"pw\" }";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123));
    }

    // ---------- /signup ----------

    @Test
    public void signup_missingFields_returns400() throws Exception {
        String json = "{ \"email\": \"test@example.com\" }";

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("missing_fields"));

        verifyNoInteractions(userService);
    }

    @Test
    public void signup_emailExists_returns409() throws Exception {
        when(userService.findByEmail("dup@example.com")).thenReturn(Optional.of(new User()));

        String json = "{ \"email\": \"dup@example.com\", \"username\": \"ayaan\", \"password\": \"pw\" }";

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("email_exists"));

        verify(userService).findByEmail("dup@example.com");
        verify(userService, never()).findByUsername(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    public void signup_usernameExists_returns409() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userService.findByUsername("dupuser")).thenReturn(Optional.of(new User()));

        String json = "{ \"email\": \"test@example.com\", \"username\": \"dupuser\", \"password\": \"pw\" }";

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("username_exists"));

        verify(userService).findByEmail("test@example.com");
        verify(userService).findByUsername("dupuser");
        verify(userService, never()).save(any(User.class));
    }

    @Test
    public void signup_success_createsUserAndReturns201() throws Exception {
        when(userService.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pw")).thenReturn("ENCODED");

        User saved = new User();
        saved.setId(99L);

        when(userService.save(any(User.class))).thenAnswer(invocation -> {
            User arg = invocation.getArgument(0);
            assertEquals("new@example.com", arg.getEmail());
            assertEquals("newuser", arg.getUsername());
            assertEquals("ENCODED", arg.getPasswordHash());
            assertEquals("ENCODED", arg.getPassword());
            return saved;
        });

        String json = "{ \"email\": \"new@example.com\", \"username\": \"newuser\", \"password\": \"pw\", " +
                "\"firstName\": \"Ayaan\", \"lastName\": \"Syed\", \"avatarUrl\": \"http://img\" }";

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(99));

        verify(passwordEncoder).encode("pw");
        verify(userService).save(any(User.class));
    }

    // ---------- exception handler ----------

    @Test
    public void onIntegrity_returns409WithConstraintBody() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("outer", new RuntimeException("root-cause"));

        ResponseEntity<?> response = authController.onIntegrity(ex);

        assertEquals(409, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("constraint_violation", body.get("error"));
        assertThat((String) body.get("message"), containsString("root-cause"));
    }
}
