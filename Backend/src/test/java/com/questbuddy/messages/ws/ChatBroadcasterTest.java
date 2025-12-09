package com.questbuddy.messages.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.messages.direct.dto.DirectMessageResponseDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.questbuddy.messages.ws.DirectChatEndpoint;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChatBroadcasterTest {

    private ChatBroadcaster broadcaster;
    private ObjectMapper om;

    @Before
    public void setup() {
        om = new ObjectMapper();
        broadcaster = new ChatBroadcaster(om);
    }

    @Test
    public void testTripMessageNew() {
        try (MockedStatic<TripChatEndpoint> mocked = mockStatic(TripChatEndpoint.class)) {
            TripMessageResponseDTO dto = sampleTripDto();

            broadcaster.tripMessageNew(10L, dto);

            mocked.verify(() ->
                    TripChatEndpoint.sendToTrip(eq(10L), anyString())
            );
        }
    }

    @Test
    public void testTripEdit() {
        try (MockedStatic<TripChatEndpoint> mocked = mockStatic(TripChatEndpoint.class)) {
            TripMessageResponseDTO dto = sampleTripDto();

            broadcaster.tripEdit(10L, dto);

            mocked.verify(() ->
                    TripChatEndpoint.sendToTrip(eq(10L), anyString())
            );
        }
    }

    @Test
    public void testTripDelete() {
        try (MockedStatic<TripChatEndpoint> mocked = mockStatic(TripChatEndpoint.class)) {
            TripMessageResponseDTO dto = sampleTripDto();

            broadcaster.tripDelete(10L, dto);

            mocked.verify(() ->
                    TripChatEndpoint.sendToTrip(eq(10L), anyString())
            );
        }
    }

    @Test
    public void testTripReactionToggle() {
        try (MockedStatic<TripChatEndpoint> mocked = mockStatic(TripChatEndpoint.class)) {

            broadcaster.tripReactionToggle(10L, 55L, "🔥");

            mocked.verify(() ->
                    TripChatEndpoint.sendToTrip(eq(10L), anyString())
            );
        }
    }

    @Test
    public void testTripReadReceipt() {
        try (MockedStatic<TripChatEndpoint> mocked = mockStatic(TripChatEndpoint.class)) {

            broadcaster.tripReadReceipt(10L, 5L, 99L, 3);

            mocked.verify(() ->
                    TripChatEndpoint.sendToTrip(eq(10L), anyString())
            );
        }
    }

    // --- Helpers ---

    private TripMessageResponseDTO sampleTripDto() {
        return new TripMessageResponseDTO(
                1L, 10L, 5L, "hi",
                null, null,
                Instant.now(), Instant.now(),
                false, 1L,
                Map.of(), Set.of(),
                null, false, null, null
        );
    }

    private DirectMessageResponseDTO sampleDmDto() {
        return new DirectMessageResponseDTO(
                1L, 5L, 7L, "hi",
                null, null,
                Instant.now(), Instant.now(),
                false, 1L,
                Map.of(), Set.of(),
                null, false, null, null,
                Instant.now(), false
        );
    }
}
