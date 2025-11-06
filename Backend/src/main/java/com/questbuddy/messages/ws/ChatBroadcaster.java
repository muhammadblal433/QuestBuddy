package com.questbuddy.messages.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import org.springframework.stereotype.Component;
import com.questbuddy.messages.ws.DirectChatEndpoint;
import com.questbuddy.messages.direct.dto.DirectMessageResponseDTO;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds compact JSON payloads and pushes them via the Trip/DM endpoints.
 * For now we use only the Trip methods; DM will be wired in later.
 */
@Component
public class ChatBroadcaster {

    private static final ObjectMapper om = new ObjectMapper();

    // TripMessage

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
        Map<String,Object> extra = new HashMap<>();
        extra.put("messageId", messageId);
        extra.put("emoji", emoji);
        sendTrip(tripId, json("REACTION_TOGGLE", "TRIP", tripId, null, extra));
    }

    // Helper funcs for TripMessage
    private void sendTrip(Long tripId, String payload) {
        if (payload == null) return;
        TripChatEndpoint.sendToTrip(tripId, payload); // uses the same room your TYPING_* uses
    }

    private static String json(String event, String channelType, Object channelId, Object messageDto, Map<String, Object> extra) {
        try {
            Map<String, Object> m = new HashMap<>();
            m.put("event", event);
            m.put("channelType", channelType);
            m.put("channelId", channelId);
            m.put("timestamp", Instant.now().toString());
            if (messageDto != null) m.put("message", messageDto);
            if (extra != null) m.putAll(extra);
            return om.writeValueAsString(m);
        } catch (Exception e) {
            return "{\"event\":\"" + event + "\",\"channelType\":\"" + channelType + "\",\"channelId\":\"" + channelId + "\"}";
        }
    }

    // Direct Message
    public void dmMessageNew(long u1, long u2, DirectMessageResponseDTO dto) {
        String key = DirectChatEndpoint.canonicalPairKey(u1, u2);
        sendDm(key, json("MESSAGE_NEW", "DM", key, dto, null));
    }

    public void dmEdit(long u1, long u2, DirectMessageResponseDTO dto) {
        String key = DirectChatEndpoint.canonicalPairKey(u1, u2);
        sendDm(key, json("EDIT", "DM", key, dto, null));
    }

    public void dmDelete(long u1, long u2, DirectMessageResponseDTO dto) {
        String key = DirectChatEndpoint.canonicalPairKey(u1, u2);
        sendDm(key, json("DELETE", "DM", key, dto, null));
    }

    // Lightweight toggle signal; clients can refresh counts if needed.
    public void dmReactionToggle(long u1, long u2, Long messageId, String emoji) {
        String key = DirectChatEndpoint.canonicalPairKey(u1, u2);
        java.util.Map<String,Object> extra = new java.util.HashMap<>();
        extra.put("messageId", messageId);
        extra.put("emoji", emoji);
        sendDm(key, json("REACTION_TOGGLE", "DM", key, null, extra));
    }

    // Optional unreadCount if your markRead computes it; pass null otherwise.
    public void dmReadReceipt(long u1, long u2, Long upToMessageId, Long readerId, Integer unreadCount) {
        String key = DirectChatEndpoint.canonicalPairKey(u1, u2);
        java.util.Map<String,Object> extra = new java.util.HashMap<>();
        extra.put("upToMessageId", upToMessageId);
        extra.put("readerId", readerId);
        if (unreadCount != null) extra.put("unreadCount", unreadCount);
        sendDm(key, json("READ_RECEIPT", "DM", key, null, extra));
    }

    // helper for DM
    private void sendDm(String pairKey, String payload) {
        if (payload == null) return;
        DirectChatEndpoint.sendToPair(pairKey, payload);
    }
}
