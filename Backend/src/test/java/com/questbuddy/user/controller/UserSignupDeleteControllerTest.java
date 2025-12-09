package com.questbuddy.user.controller;

import com.questbuddy.user.model.Role;
import com.questbuddy.user.model.User;
import com.questbuddy.user.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UserSignupDeleteController.class)
public class UserSignupDeleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepo;

    @MockBean
    private PasswordEncoder encoder;

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
    public void getAllUsers_empty_returnsEmptyList() throws Exception {
        when(userRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v2/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userRepo).findAll();
    }

//    @Test
//    public void getAllUsers_returnsMappedDtos() throws Exception {
//        User u1 = buildUser(1L, "a@example.com", "a");
//        User u2 = buildUser(2L, "b@example.com", "b");
//
//        when(userRepo.findAll()).thenReturn(List.of(u1, u2));
//
//        mockMvc.perform(get("/api/v2/users")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].id").value(1))
//                .andExpect(jsonPath("$[0].email").value("a@example.com"))
//                .andExpect(jsonPath("$[1].id").value(2))
//                .andExpect(jsonPath("$[1].email").value("b@example.com"));
//    }


    @Test
    public void getUserById_found_returnsDto() throws Exception {
        User u = buildUser(5L, "u@example.com", "user");
        when(userRepo.findById(5L)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/v2/users/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("u@example.com"))
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    public void getUserById_notFound_returns404() throws Exception {
        when(userRepo.findById(5L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v2/users/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/v2/users/by-username/{username} ----------

    @Test
    public void getByUsername_found_returnsDto() throws Exception {
        User u = buildUser(10L, "x@example.com", "ayaan");
        when(userRepo.findByUsernameIgnoreCase("ayaan")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/v2/users/by-username/ayaan")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("ayaan"));
    }

    @Test
    public void getByUsername_notFound_returns404() throws Exception {
        when(userRepo.findByUsernameIgnoreCase("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v2/users/by-username/unknown")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ping_returnsAliveString() throws Exception {
        mockMvc.perform(get("/api/v2/users/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("UserSignupDeleteController is alive!"));
    }


    @Test
    public void deleteUser_notFound_returns404() throws Exception {
        when(userRepo.existsById(5L)).thenReturn(false);

        mockMvc.perform(delete("/api/v2/users/5"))
                .andExpect(status().isNotFound());

        verify(userRepo).existsById(5L);
        verify(userRepo, never()).deleteById(anyLong());
    }

    @Test
    public void deleteUser_existing_deletesAndReturnsOk() throws Exception {
        when(userRepo.existsById(5L)).thenReturn(true);

        mockMvc.perform(delete("/api/v2/users/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully!"));

        verify(userRepo).existsById(5L);
        verify(userRepo).deleteById(5L);
    }
}
