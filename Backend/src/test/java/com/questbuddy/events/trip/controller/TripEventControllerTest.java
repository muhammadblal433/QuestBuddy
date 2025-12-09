package com.questbuddy.events.trip.controller;

import com.questbuddy.events.trip.dto.TripEventCreateDTO;
import com.questbuddy.events.trip.dto.TripEventEditDTO;
import com.questbuddy.events.trip.dto.TripEventResponseDTO;
import com.questbuddy.events.trip.service.TripEventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(TripEventController.class)
public class TripEventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TripEventService service;

    private static final String BASE = "/api/v13/trips/10/events";

    private TripEventResponseDTO sample() {
        return new TripEventResponseDTO(
                5L, 10L, 3L,
                "Hike",
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T01:00:00Z"),
                "Forest",
                "Bring boots",
                1,
                Collections.emptyList(),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"),
                false
        );
    }

    @Test
    public void testEdit_success() throws Exception {
        TripEventResponseDTO out = sample();
        Mockito.when(service.edit(eq(5L), eq(10L), eq(55L), any()))
                .thenReturn(out);

        mvc.perform(put(BASE + "/55")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New\", \"startsAt\": \"2025-01-01T00:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));

        Mockito.verify(service).edit(eq(5L), eq(10L), eq(55L), any(TripEventEditDTO.class));
    }

    @Test
    public void testCreate_success() throws Exception {
        TripEventResponseDTO out = sample();
        Mockito.when(service.create(eq(5L), eq(10L), any()))
                .thenReturn(out);

        mvc.perform(post(BASE)
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dinner\",\"startsAt\":\"2025-01-01T00:00:00Z\",\"endsAt\":\"2025-01-01T01:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hike"));

        Mockito.verify(service).create(eq(5L), eq(10L), any(TripEventCreateDTO.class));
    }

    @Test
    public void testList_success() throws Exception {
        Page<TripEventResponseDTO> page =
                new PageImpl<>(Collections.singletonList(sample()));

        Mockito.when(service.list(eq(5L), eq(10L),
                        isNull(), isNull(), eq(0), eq(50)))
                .thenReturn(page);

        mvc.perform(get(BASE)
                        .header("X-User-Id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5L));

        Mockito.verify(service)
                .list(eq(5L), eq(10L), isNull(), isNull(), eq(0), eq(50));
    }

    @Test
    public void testList_withRange() throws Exception {
        Page<TripEventResponseDTO> page =
                new PageImpl<>(Collections.singletonList(sample()));

        Mockito.when(service.list(eq(5L), eq(10L),
                        any(), any(), eq(1), eq(20)))
                .thenReturn(page);

        mvc.perform(get(BASE)
                        .header("X-User-Id", "5")
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-01-01T23:00:00Z")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5L));

        Mockito.verify(service)
                .list(eq(5L), eq(10L),
                        any(Instant.class), any(Instant.class),
                        eq(1), eq(20));
    }

    @Test
    public void testDelete_success() throws Exception {
        mvc.perform(delete(BASE + "/33")
                        .header("X-User-Id", "5"))
                .andExpect(status().isOk());

        Mockito.verify(service).delete(5L, 10L, 33L);
    }
}
