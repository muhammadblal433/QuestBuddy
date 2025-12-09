package com.questbuddy.tripmember.controller;

import com.questbuddy.tripmember.dto.PendingInviteDTO;
import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.tripmember.service.TripMembershipService;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(TripInviteController.class)
public class TripInviteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TripMembershipService svc;

    @Test
    public void testListPendingSuccess() throws Exception {
        PendingInviteDTO dto = new PendingInviteDTO(10L, "Trip A", 1L, "owner");

        TripMember m = mock(TripMember.class);
        when(svc.listPendingInvitesForUser(5L, 5L))
                .thenReturn(List.of(m));
        when(m.getTrip()).thenReturn(null);
        when(m.getInvitedBy()).thenReturn(null);

        mockStatic(PendingInviteDTO.class).when(() -> PendingInviteDTO.from(m)).thenReturn(dto);

        mvc.perform(get("/api/v12/users/5/trip-invites/pending")
                        .header("X-User-Id", 5L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testListPendingForbidden() throws Exception {
        when(svc.listPendingInvitesForUser(1L, 5L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mvc.perform(get("/api/v12/users/5/trip-invites/pending")
                        .header("X-User-Id", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testListPendingUserNotFound() throws Exception {
        when(svc.listPendingInvitesForUser(5L, 5L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        mvc.perform(get("/api/v12/users/5/trip-invites/pending")
                        .header("X-User-Id", 5L))
                .andExpect(status().isNotFound());
    }
}
