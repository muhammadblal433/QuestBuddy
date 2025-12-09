package com.questbuddy.messages.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.trip.service.TripMessageService;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(TripMessageController.class)
public class TripMessageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TripMessageService service;

    @Autowired
    private ObjectMapper om;

    private TripMessageResponseDTO sample() {
        return new TripMessageResponseDTO(
                1L, 10L, 5L, "hello",
                null, null,
                Instant.now(), Instant.now(),
                false, 1L,
                Collections.singletonMap("👍", 3),
                Collections.singleton("👍"),
                null, false, null, null
        );
    }
    
    @Test
    public void testEdit_success() throws Exception {
        TripMessageResponseDTO dto = sample();
        TripMessageEditDTO in = new TripMessageEditDTO("abc", 1L);

        when(service.edit(eq(5L), eq(10L), eq(1001L), any())).thenReturn(dto);

        mvc.perform(put("/api/v9/trips/10/messages/1001")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(service).edit(eq(5L), eq(10L), eq(1001L), any());
    }

    @Test
    public void testPost_success() throws Exception {
        TripMessageResponseDTO dto = sample();

        TripMessageCreateDTO in = new TripMessageCreateDTO(
                "hi", null, null, "client123", Instant.now()
        );

        when(service.post(eq(5L), eq(10L), any())).thenReturn(dto);

        mvc.perform(post("/api/v9/trips/10/messages")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("hello"));

        verify(service).post(eq(5L), eq(10L), any());
    }

    @Test
    public void testReact_success() throws Exception {
        Map<String, Integer> resp = new HashMap<>();
        resp.put("🔥", 2);

        when(service.toggleReaction(eq(5L), eq(10L), eq(1001L), eq("🔥"))).thenReturn(resp);

        mvc.perform(post("/api/v9/trips/10/messages/1001/reactions")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emoji\":\"🔥\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.🔥").value(2));

        verify(service).toggleReaction(5L, 10L, 1001L, "🔥");
    }

    // -------- GET list --------
    @Test
    public void testList_success() throws Exception {

        List<TripMessageResponseDTO> list =
                Collections.singletonList(sample());

        when(service.list(eq(5L), eq(10L), eq(1001L), eq(50)))
                .thenReturn(list);

        mvc.perform(get("/api/v9/trips/10/messages")
                        .param("beforeId", "1001")
                        .param("limit", "50")
                        .header("X-User-Id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(service).list(5L, 10L, 1001L, 50);
    }

    // -------- DELETE delete message --------
    @Test
    public void testDelete_success() throws Exception {
        TripMessageResponseDTO dto = sample();

        when(service.delete(eq(5L), eq(10L), eq(1001L), eq(1L)))
                .thenReturn(dto);

        mvc.perform(delete("/api/v9/trips/10/messages/1001")
                        .param("version", "1")
                        .header("X-User-Id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").value(10L));

        verify(service).delete(5L, 10L, 1001L, 1L);
    }

    @Test
    public void testUnreact_success() throws Exception {
        Map<String,Integer> resp = new HashMap<>();
        resp.put("😎", 1);

        when(service.toggleReaction(eq(5L), eq(10L), eq(800L), eq("😎")))
                .thenReturn(resp);

        mvc.perform(delete("/api/v9/trips/10/messages/800/reactions/%F0%9F%98%8E")
                        .header("X-User-Id", "5")
                        .param("emoji", "😎"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.😎").value(1));

        verify(service).toggleReaction(5L, 10L, 800L, "😎");
    }

    @Test
    public void testPing() throws Exception {
        mvc.perform(get("/api/v9/trips/messages/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }
}
