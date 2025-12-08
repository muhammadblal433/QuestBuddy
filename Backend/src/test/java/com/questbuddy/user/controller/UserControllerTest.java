package com.questbuddy.user.controller;

import com.questbuddy.user.model.Role;
import com.questbuddy.user.model.User;
import com.questbuddy.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User buildUser(Long id, String email, String username) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setUsername(username);
        u.setPasswordHash("HASH");
        u.setPassword("HASH");
        u.setRole(Role.TRIP_MEMBER);
        u.setActive(true);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        return u;
    }

    @Test
    public void update_missingUserHeader_returns401() throws Exception {
        String body = "{ \"email\": \"new@example.com\" }";

        mockMvc.perform(put("/api/v1/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("missing_user"));

        verifyNoInteractions(userService);
    }

    @Test
    public void update_forbiddenWhenHeaderDoesNotMatchPathId() throws Exception {
        String body = "{ \"email\": \"new@example.com\" }";

        mockMvc.perform(put("/api/v1/users/5")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("forbidden"));

        verifyNoInteractions(userService);
    }

    @Test
    public void update_success_returnsUpdatedDto() throws Exception {
        User updated = buildUser(5L, "updated@example.com", "updatedUser");
        updated.setFirstName("Ayaan");
        updated.setLastName("Syed");
        updated.setAvatarUrl("http://avatar");

        when(userService.updateProfile(eq(5L),
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(updated);

        String body = "{ \"email\": \"updated@example.com\", \"username\": \"updatedUser\", " +
                "\"firstName\": \"Ayaan\", \"lastName\": \"Syed\", \"avatarUrl\": \"http://avatar\" }";

        mockMvc.perform(put("/api/v1/users/5")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.firstName").value("Ayaan"))
                .andExpect(jsonPath("$.lastName").value("Syed"))
                .andExpect(jsonPath("$.avatarUrl").value("http://avatar"))
                .andExpect(jsonPath("$.role").value("TRIP_MEMBER"))
                .andExpect(jsonPath("$.active").value(true));
    }


    @Test
    public void updateMe_missingHeader_returns401() throws Exception {
        String body = "{ \"email\": \"new@example.com\" }";

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("missing_user"));

        verifyNoInteractions(userService);
    }

    @Test
    public void updateMe_success_updatesCurrentUser() throws Exception {
        User updated = buildUser(10L, "me@example.com", "meuser");
        when(userService.updateProfile(eq(10L),
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(updated);

        String body = "{ \"email\": \"me@example.com\", \"username\": \"meuser\", " +
                "\"firstName\": \"A\", \"lastName\": \"B\", \"avatarUrl\": \"http://me\" }";

        mockMvc.perform(put("/api/v1/users/me")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.username").value("meuser"));
    }

    @Test
    public void signupBatch_createsOnlyEntriesWithRequiredFields() throws Exception {
        User created = buildUser(1L, "a@example.com", "user1");
        when(userService.signup(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(created);

        String body = "[" +
                "{ \"email\": \"a@example.com\", \"username\": \"user1\", \"password\": \"pw\", \"firstName\": \"A\", \"lastName\": \"B\" }," +
                "{ \"email\": \"missing@example.com\", \"password\": \"pw\" }" + // missing username -> skipped
                "]";

        mockMvc.perform(post("/api/v1/auth/signup/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("a@example.com"))
                .andExpect(jsonPath("$[0].username").value("user1"));

        verify(userService, times(1)).signup(anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    public void getUser_found_returnsDto() throws Exception {
        User u = buildUser(5L, "u@example.com", "user");
        when(userService.getById(5L)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/v1/users/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("u@example.com"))
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    public void getUser_notFound_returns404() throws Exception {
        when(userService.getById(5L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void me_missingHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("missing_user"));

        verifyNoInteractions(userService);
    }

    @Test
    public void me_userFound_returnsDto() throws Exception {
        User u = buildUser(7L, "me@example.com", "meuser");
        when(userService.getById(7L)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-User-Id", "7")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.username").value("meuser"));
    }

    @Test
    public void me_userNotFound_returns404() throws Exception {
        when(userService.getById(7L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-User-Id", "7")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

//    @Test
//    public void listPremiumUsers_returnsDtos() throws Exception {
//        User u1 = buildUser(1L, "p1@example.com", "p1");
//        u1.setPremiumUser(true);
//        User u2 = buildUser(2L, "p2@example.com", "p2");
//        u2.setPremiumUser(true);
//
//        when(userService.findPremiumUsers()).thenReturn(List.of(u1, u2));
//
//        mockMvc.perform(get("/api/v1/users/premium")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].id").value(1))
//                .andExpect(jsonPath("$[0].premium").value(true))
//                .andExpect(jsonPath("$[1].id").value(2))
//                .andExpect(jsonPath("$[1].premium").value(true));
//    }
}
