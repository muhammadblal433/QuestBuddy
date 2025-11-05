package com.questbuddy.messages.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds compact JSON payloads and pushes them via the Trip/DM endpoints.
 * For now we use only the Trip methods; DM will be wired in later.
 */
@Component
public class ChatBroadcaster {

    private final ObjectMapper om = new ObjectMapper();

    // helper funcs (using helper from endpoint file)

    public void tripMessageNew(Long tripId, TripMessageResponseDTO dto) {
        sendTrip(tripId, json("MESSAGE_NEW", "TRIP", tripId, dto, null));
    }

    public void tripEdit(Long tripId, TripMessageResponseDTO dto) {
        sendTrip(tripId, json("EDIT", "TRIP", tripId, dto, null));
    }

    public void tripDelete(Long tripId, TripMessageResponseDTO dto) {
        sendTrip(tripId, json("DELETE", "TRIP", tripId, dto, null));
    }

    // clients can refresh counts if needed
    public void tripReactionToggle(Long tripId, Long messageId, String emoji) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("messageId", messageId);
        extra.put("emoji", emoji);
        sendTrip(tripId, json("REACTION_TOGGLE", "TRIP", tripId, null, extra));
    }

    // ----- Internals -----

    private void sendTrip(Long tripId, String payload) {
        if (payload == null) return;
        TripChatEndpoint.sendToTrip(tripId, payload);
    }

    private String json(String event, String channelType, Object channelId, Object messageDto, Map<String, Object> extra) {
        try {
            Map<String, Object> p = new HashMap<>();
            p.put("event", event);
            p.put("channelType", channelType);
            p.put("channelId", channelId);
            if (messageDto != null) p.put("message", messageDto);
            p.put("timestamp", Instant.now().toString());
            if (extra != null) p.putAll(extra);
            return om.writeValueAsString(p);
        } catch (Exception e) {
            return null;
        }
    }
}
