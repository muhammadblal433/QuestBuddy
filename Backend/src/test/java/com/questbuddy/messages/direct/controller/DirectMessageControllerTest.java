package com.questbuddy.messages.direct.controller;

import com.questbuddy.messages.direct.dto.*;
import com.questbuddy.messages.direct.service.DirectMessageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
public class DirectMessageControllerTest {

    @Mock
    private DirectMessageService service;

    @InjectMocks
    private DirectMessageController controller;

    private MockMvc mvc;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // PUT edit
    @Test
    public void testEdit_success() throws Exception {
        DirectMessageResponseDTO dto = new DirectMessageResponseDTO(
                1L, 5L, 25L, "x", null,null,
                Instant.now(),Instant.now(),
                false,1L, Map.of(), Set.of(), null,
                false,null,null,
                null,false
        );

        when(service.edit(eq(5L), eq(25L), eq(1001L), any())).thenReturn(dto);

        mvc.perform(put("/api/v10/direct/25/messages/1001")
                        .header("X-User-Id","5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\",\"version\":1}")
                )
                .andExpect(status().isOk());
    }

    // POST send
    @Test
    public void testPost_success() throws Exception {
        DirectMessageResponseDTO dto = new DirectMessageResponseDTO(
                1L,5L,25L,"hello",null,null,
                Instant.now(),Instant.now(),
                false,1L,Map.of(),Set.of(),null,false,null,null,
                null,false
        );

        when(service.post(eq(5L), eq(25L), any())).thenReturn(dto);

        mvc.perform(post("/api/v10/direct/25/messages")
                        .header("X-User-Id","5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\",\"clientMessageId\":\"abc\",\"sentAt\":null}")
                )
                .andExpect(status().isOk());
    }

    // POST react
    @Test
    public void testReact_success() throws Exception {
        when(service.toggleReaction(5L, 25L, 1001L, "👍"))
                .thenReturn(Map.of("👍", 1));

        mvc.perform(post("/api/v10/direct/25/messages/1001/reactions")
                        .param("emoji","👍")
                        .header("X-User-Id","5")
                )
                .andExpect(status().isOk());
    }


    // GET list
    @Test
    public void testList_success() throws Exception {
        List<DirectMessageResponseDTO> list = List.of(
                new DirectMessageResponseDTO(
                        1L,5L,25L,"hello",null,null,
                        Instant.now(),Instant.now(),
                        false,1L,Map.of(),Set.of(),
                        null,false,null,null,
                        null,false
                )
        );

        when(service.list(5L, 25L, null, 50)).thenReturn(list);

        mvc.perform(get("/api/v10/direct/25/messages")
                        .header("X-User-Id","5"))
                .andExpect(status().isOk());
    }

    // GET ping
    @Test
    public void testPing() throws Exception {
        mvc.perform(get("/api/v10/direct/messages/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    // DELETE message
    @Test
    public void testDelete_success() throws Exception {

        mvc.perform(delete("/api/v10/direct/25/messages/1001")
                        .header("X-User-Id","5"))
                .andExpect(status().isOk());

        verify(service).delete(5L,25L,1001L);
    }

    // DELETE reaction
    @Test
    public void testUnreact_success() throws Exception {
        when(service.toggleReaction(5L,25L,1001L,"👍"))
                .thenReturn(Map.of("👍",0));

        mvc.perform(delete("/api/v10/direct/25/messages/1001/reactions/👍")
                        .header("X-User-Id","5"))
                .andExpect(status().isOk());
    }
}
