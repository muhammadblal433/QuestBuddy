package com.questbuddy.tripmember.controller;

import com.questbuddy.tripmember.dto.InviteDTO;
import com.questbuddy.tripmember.dto.UpdateStatusDTO;
import com.questbuddy.tripmember.dto.UserSummaryDTO;
import com.questbuddy.tripmember.service.TripMembershipService;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(TripMemberController.class)
public class TripMemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TripMembershipService svc;

    @Test
    public void testApproveSuccess() throws Exception {
        mvc.perform(put("/api/v12/trips/10/members/approve")
                        .header("X-User-Id", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isNoContent());

        verify(svc).approve(5L, 10L);
    }

    @Test
    public void testApproveBadStatus400() throws Exception {
        mvc.perform(put("/api/v12/trips/10/members/approve")
                        .header("X-User-Id", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testApproveNotFound() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(svc).approve(5L, 10L);

        mvc.perform(put("/api/v12/trips/10/members/approve")
                        .header("X-User-Id", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeclineBadStatus400() throws Exception {
        mvc.perform(put("/api/v12/trips/10/members/decline")
                        .header("X-User-Id", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"WRONG\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInviteSuccess() throws Exception {
        mvc.perform(post("/api/v12/trips/10/members/invite")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":5}"))
                .andExpect(status().isCreated());

        verify(svc).invite(1L, 10L, 5L);
    }

    @Test
    public void testInviteForbidden() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN))
                .when(svc).invite(1L, 10L, 5L);

        mvc.perform(post("/api/v12/trips/10/members/invite")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testInviteNotFound() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(svc).invite(1L, 10L, 5L);

        mvc.perform(post("/api/v12/trips/10/members/invite")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":5}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testListMembersSuccess() throws Exception {
        UserSummaryDTO u = new UserSummaryDTO(5L, "John", "john", null);
        when(svc.listAccepted(1L, 10L)).thenReturn(List.of(
                new com.questbuddy.user.model.User() {{
                    setId(5L);
                    setUsername("john");
                }}
        ));

        // Mock static conversion UserSummaryDTO.from()?
        mockStatic(UserSummaryDTO.class).when(() -> UserSummaryDTO.from(any()))
                .thenReturn(u);

        mvc.perform(get("/api/v12/trips/10/members")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    public void testListMembersForbidden() throws Exception {
        when(svc.listAccepted(1L, 10L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mvc.perform(get("/api/v12/trips/10/members")
                        .header("X-User-Id", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testListMembersNotFound() throws Exception {
        when(svc.listAccepted(1L, 10L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        mvc.perform(get("/api/v12/trips/10/members")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRemoveSuccess() throws Exception {
        mvc.perform(delete("/api/v12/trips/10/members/5")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());

        verify(svc).remove(1L, 10L, 5L);
    }

    @Test
    public void testRemoveForbidden() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN))
                .when(svc).remove(1L, 10L, 5L);

        mvc.perform(delete("/api/v12/trips/10/members/5")
                        .header("X-User-Id", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testRemoveNotFound() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(svc).remove(1L, 10L, 5L);

        mvc.perform(delete("/api/v12/trips/10/members/5")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testLeaveSuccess() throws Exception {
        mvc.perform(delete("/api/v12/trips/10/members/me")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());

        verify(svc).remove(1L, 10L, 1L);
    }

    @Test
    public void testLeaveNotFound() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(svc).remove(1L, 10L, 1L);

        mvc.perform(delete("/api/v12/trips/10/members/me")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNotFound());
    }
}
