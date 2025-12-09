package com.questbuddy.calendar.controller;

import com.questbuddy.calendar.*;
import com.questbuddy.calendar.dto.*;
import jakarta.validation.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    private static final String BASE = "/api/v4/calendar/events";

    @Test
    public void testUpdate_success() throws Exception {
        EventResponseDTO dto = new EventResponseDTO(
                10L,"T","D",
                Instant.now(),Instant.now(),
                "L",false,Instant.now(),Instant.now()
        );
        when(eventService.update(eq(5L), eq(10L), any()))
                .thenReturn(dto);

        mvc.perform(put(BASE+"/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id","5")
                        .content("""
                    {"title":"New","description":"D"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    public void testUpdate_validationException() throws Exception {
        when(eventService.update(eq(5L),eq(10L),any()))
                .thenThrow(new ValidationException("bad"));

        mvc.perform(put(BASE+"/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id","5")
                        .content("""
                    {"title":"Bad"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }

    @Test
    public void testUpdate_notFound() throws Exception {
        when(eventService.update(eq(5L),eq(10L),any()))
                .thenThrow(new EventService.ResourceNotFound("no"));

        mvc.perform(put(BASE+"/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id","5")
                        .content("""
                    {"title":"X"}
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    public void testCreate_success() throws Exception {
        EventResponseDTO dto = new EventResponseDTO(
                11L,"T","D",Instant.now(),Instant.now(),"L",false,Instant.now(),Instant.now()
        );
        when(eventService.create(eq(5L), any()))
                .thenReturn(dto);

        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id","5")
                        .content("""
                    {"title":"T","description":"D",
                     "startAt":"2025-01-01T00:00:00Z",
                     "endAt":"2025-01-02T00:00:00Z",
                     "location":"L",
                     "allDay":false}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    public void testCreate_invalidBody() throws Exception {
        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id","5")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testList_success() throws Exception {
        when(eventService.list(eq(5L), any(), any()))
                .thenReturn(List.of());

        mvc.perform(get(BASE)
                        .header("X-User-Id","5"))
                .andExpect(status().isOk());
    }


    @Test
    public void testListAll_success() throws Exception {
        when(eventService.listAll()).thenReturn(List.of());

        mvc.perform(get(BASE+"/all")
                        .header("X-User-Id","5"))
                .andExpect(status().isOk());
    }

    @Test
    public void testListAll_between() throws Exception {
        when(eventService.listAllBetween(any(), any()))
                .thenReturn(List.of());

        mvc.perform(get(BASE+"/all")
                        .header("X-User-Id","5")
                        .param("from","2025-01-01T00:00:00Z")
                        .param("to","2025-01-02T00:00:00Z"))
                .andExpect(status().isOk());
    }

    @Test
    public void testListByUser_success() throws Exception {
        when(eventService.listByUser(eq(8L)))
                .thenReturn(List.of());

        mvc.perform(get(BASE+"/user/8")
                        .header("X-User-Id","5"))
                .andExpect(status().isOk());
    }

    @Test
    public void testListByUser_between() throws Exception {
        when(eventService.listByUserBetween(eq(8L), any(), any()))
                .thenReturn(List.of());

        mvc.perform(get(BASE+"/user/8")
                        .header("X-User-Id","5")
                        .param("from","2025-01-01T00:00:00Z")
                        .param("to","2025-01-02T00:00:00Z"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGet_success() throws Exception {
        EventResponseDTO dto = new EventResponseDTO(
                15L,"T","D",Instant.now(),Instant.now(),"L",false,Instant.now(),Instant.now()
        );
        when(eventService.get(5L,15L)).thenReturn(dto);

        mvc.perform(get(BASE+"/15")
                        .header("X-User-Id","5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15));
    }

    @Test
    public void testGet_notFound() throws Exception {
        when(eventService.get(5L,15L))
                .thenThrow(new EventService.ResourceNotFound("no"));

        mvc.perform(get(BASE+"/15")
                        .header("X-User-Id","5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }


    @Test
    public void testPing() throws Exception {
        mvc.perform(get(BASE+"/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("EventController is alive!"));
    }

    @Test
    public void testDelete_success() throws Exception {
        mvc.perform(delete(BASE+"/33")
                        .header("X-User-Id","5")).andExpect(status().isOk());
    }

    @Test
    public void testDelete_notFound() throws Exception {
        doThrow(new EventService.ResourceNotFound("no"))
                .when(eventService).delete(5L,33L);

        mvc.perform(delete(BASE+"/33")
                        .header("X-User-Id","5"))
                .andExpect(status().isNotFound());
    }
}

