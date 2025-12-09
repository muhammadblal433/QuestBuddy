package com.questbuddy.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.notification.*;
import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.notification.dto.NotificationResponseDTO;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationControllerTest {

    @Mock private NotificationService service;
    @Mock private NotificationMapper mapper;

    @InjectMocks
    private NotificationController controller;

    private final ObjectMapper om = new ObjectMapper();

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testPing() throws Exception {
        mvc().perform(get("/api/v7/notifications/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("NotificationController v7 is alive!"));
    }

    @Test
    public void testMarkRead_success() throws Exception {
        Notification n = new Notification();
        n.setId(10L);
        n.setRead(true);

        NotificationResponseDTO dto =
                new NotificationResponseDTO(10L, 5L, "T", "M", NotificationType.REMINDER,
                        null, null, null, Instant.now(), true);

        when(service.markRead(10L, 5L)).thenReturn(n);
        when(mapper.toResponse(n)).thenReturn(dto);

        mvc().perform(put("/api/v7/notifications/10/read")
                        .header("X-User-Id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    public void testMarkRead_notFound() throws Exception {
        when(service.markRead(10L, 5L))
                .thenThrow(new IllegalArgumentException("notification_not_found"));

        mvc().perform(put("/api/v7/notifications/10/read")
                        .header("X-User-Id", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testMarkRead_badRequest() throws Exception {
        when(service.markRead(10L, 5L))
                .thenThrow(new IllegalArgumentException("bad"));

        mvc().perform(put("/api/v7/notifications/10/read")
                        .header("X-User-Id", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testMarkRead_forbidden() throws Exception {
        when(service.markRead(10L, 5L))
                .thenThrow(new SecurityException("forbidden"));

        mvc().perform(put("/api/v7/notifications/10/read")
                        .header("X-User-Id", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreate_success() throws Exception {
        Notification n = new Notification();
        n.setId(99L);

        NotificationResponseDTO out =
                new NotificationResponseDTO(99L, 5L, "T", "M",
                        NotificationType.REMINDER, null, null, null,
                        Instant.now(), false);

        NotificationCreateDTO dto =
                new NotificationCreateDTO(5L, "T", "M", NotificationType.REMINDER,
                        null, null, null);

        when(service.create(any())).thenReturn(n);
        when(mapper.toResponse(n)).thenReturn(out);

        mvc().perform(post("/api/v7/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    public void testCreate_badReference() throws Exception {
        NotificationCreateDTO dto =
                new NotificationCreateDTO(5L, "T", "M", null, 1L, null, null);

        when(service.create(any()))
                .thenThrow(new IllegalArgumentException("event_not_found"));

        mvc().perform(post("/api/v7/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testList_all() throws Exception {
        Notification n = new Notification();
        n.setId(1L);
        n.setRecipient(new com.questbuddy.user.model.User());
        n.getRecipient().setId(5L);

        NotificationResponseDTO dto =
                new NotificationResponseDTO(1L, 5L, "A", "B",
                        NotificationType.REMINDER, null, null, null,
                        Instant.now(), false);

        when(service.listForUser(5L, null)).thenReturn(List.of(n));
        when(mapper.toResponse(n)).thenReturn(dto);

        mvc().perform(get("/api/v7/notifications")
                        .header("X-User-Id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testList_unreadOnly() throws Exception {
        Notification n = new Notification();
        n.setId(1L);
        n.setRecipient(new com.questbuddy.user.model.User());
        n.getRecipient().setId(5L);

        NotificationResponseDTO dto =
                new NotificationResponseDTO(1L, 5L, "A", "B",
                        NotificationType.REMINDER, null, null, null,
                        Instant.now(), false);

        when(service.listForUser(5L, true)).thenReturn(List.of(n));
        when(mapper.toResponse(n)).thenReturn(dto);

        mvc().perform(get("/api/v7/notifications")
                        .header("X-User-Id", "5")
                        .param("unread", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testDelete_success() throws Exception {
        doReturn(true).when(service).deleteForOwner(10L, 5L);

        mvc().perform(delete("/api/v7/notifications/10")
                        .header("X-User-Id", "5"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDelete_notFound() throws Exception {
        doThrow(new IllegalArgumentException("notification_not_found"))
                .when(service).deleteForOwner(10L, 5L);

        mvc().perform(delete("/api/v7/notifications/10")
                        .header("X-User-Id", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDelete_badRequest() throws Exception {
        doThrow(new IllegalArgumentException("bad"))
                .when(service).deleteForOwner(10L, 5L);

        mvc().perform(delete("/api/v7/notifications/10")
                        .header("X-User-Id", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete_forbidden() throws Exception {
        doThrow(new SecurityException("forbidden"))
                .when(service).deleteForOwner(10L, 5L);

        mvc().perform(delete("/api/v7/notifications/10")
                        .header("X-User-Id", "5"))
                .andExpect(status().isForbidden());
    }
}

