package com.questbuddy.trip.controller;

import com.questbuddy.trip.*;
import com.questbuddy.trip.TripService.ResourceNotFound;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@WebMvcTest(TripController.class)
public class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripService tripService;

    private TripResponseDTO sampleDto(Long id, Long ownerId) {
        return new TripResponseDTO(
                id,
                ownerId,
                "Trip Name",
                "NY",
                "Ames",
                40.0,
                -90.0,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 5),
                Instant.now(),
                Instant.now()
        );
    }


    @Test
    public void update_success_returnsDto() throws Exception {
        TripResponseDTO dto = sampleDto(10L, 5L);

        when(tripService.update(eq(5L), eq(10L), any(TripUpdateDTO.class)))
                .thenReturn(dto);

        String body = """
                {
                    "name": "Trip Name",
                    "destination": "NY",
                    "startLocationName": "Ames",
                    "startLat": 40.0,
                    "startLon": -90.0,
                    "startDate": "2025-01-01",
                    "endDate": "2025-01-05"
                }
                """;

        mockMvc.perform(put("/api/v6/trips/10")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.ownerId").value(5))
                .andExpect(jsonPath("$.name").value("Trip Name"));
    }

    @Test
    public void create_success_returnsDto() throws Exception {
        TripResponseDTO dto = sampleDto(20L, 5L);

        when(tripService.create(eq(5L), any(TripCreateDTO.class)))
                .thenReturn(dto);

        String body = """
                {
                    "name": "Trip Name",
                    "destination": "NY",
                    "startLocationName": "Ames",
                    "startLat": 40.0,
                    "startLon": -90.0,
                    "startDate": "2025-01-01",
                    "endDate": "2025-01-05"
                }
                """;

        mockMvc.perform(post("/api/v6/trips")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.ownerId").value(5));
    }

    @Test
    public void list_returnsTrips() throws Exception {
        TripResponseDTO d1 = sampleDto(1L, 5L);
        TripResponseDTO d2 = sampleDto(2L, 5L);

        when(tripService.list(5L)).thenReturn(List.of(d1, d2));

        mockMvc.perform(get("/api/v6/trips")
                        .header("X-User-Id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }


    @Test
    public void get_success_returnsDto() throws Exception {
        TripResponseDTO dto = sampleDto(15L, 5L);

        when(tripService.get(5L, 15L)).thenReturn(dto);

        mockMvc.perform(get("/api/v6/trips/15")
                        .header("X-User-Id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.ownerId").value(5));
    }

    @Test
    public void delete_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/v6/trips/10")
                        .header("X-User-Id", "5"))
                .andExpect(status().isNoContent());

        verify(tripService).delete(5L, 10L);
    }
}
