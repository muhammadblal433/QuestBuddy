package com.questbuddy.messages.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.config.SpringContext;
import com.questbuddy.messages.guard.TripMembershipGate;
import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.trip.service.TripMessageService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Two-way Trip chat WebSocket endpoint.
 * Connect to: ws://<host>/ws/messages/trips/{tripId}/{userId}
 *
 * This endpoint takes into account for creating messages, editing and deleting them,
 * reacting and toggling reactions to a message, and "is typing".
 */
@Component
@ServerEndpoint("/ws/messages/trips/{tripId}/{userId}")
public class TripChatEndpoint {

    private static final Map<Long, Set<Session>> BY_TRIP = new ConcurrentHashMap<>();
    private static final ObjectMapper OM = new ObjectMapper();

    private TripMessageService tripSvc() { return SpringContext.getBean(TripMessageService.class); }
    private TripMembershipGate membership() { return SpringContext.getBean(TripMembershipGate.class); }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("tripId") Long tripId,
                       @PathParam("userId") Long userId) {
        try {
            if (!membership().isMember(tripId, userId)) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Not a member of this trip"));
                return;
            }
        } catch (Exception e) {
            try { session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Handshake error")); } catch (Exception ignored) {}
            return;
        }
        BY_TRIP.computeIfAbsent(tripId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sendSafe(session, json(Map.of(
                "event", "HELLO",
                "channelType", "TRIP",
                "channelId", tripId,
                "timestamp", Instant.now().toString()
        )));
    }

    @OnMessage
    public void onMessage(String msg,
                          Session session,
                          @PathParam("tripId") Long tripId,
                          @PathParam("userId") Long userId) {
        if (msg != null && msg.length() > 64_000) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", "payload too large")));
            return;
        }
        try {
            JsonNode root = OM.readTree(msg);
            String event = optText(root, "event");
            String clientMsgId = optText(root, "clientMsgId");

            if ("PING".equalsIgnoreCase(event)) {
                sendSafe(session, json(Map.of("event", "PONG")));
                return;
            }

            switch (event) {
                case "MESSAGE_NEW" -> handleCreate(session, userId, tripId, clientMsgId, root.path("payload"));
                case "EDIT"        -> handleEdit(session, userId, tripId, clientMsgId, root.path("payload"));
                case "DELETE"      -> handleDelete(session, userId, tripId, clientMsgId, root.path("payload")); // requires version
                case "REACTION_ADD":
                case "REACTION_REMOVE":
                    handleReaction(session, userId, tripId, clientMsgId, root.path("payload"));
                    break;
                case "TYPING_START":
                case "TYPING_STOP":
                    handleTyping(userId, tripId, event);
                    break;
                default -> sendSafe(session, json(Map.of("event", "ERROR", "reason", "unknown event")));
            }
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", "bad JSON")));
        }
    }

    @OnClose
    public void onClose(Session session,
                        @PathParam("tripId") Long tripId,
                        @PathParam("userId") Long userId) {
        var set = BY_TRIP.get(tripId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) BY_TRIP.remove(tripId);
        }
    }

    @OnError
    public void onError(Session session, Throwable t,
                        @PathParam("tripId") Long tripId,
                        @PathParam("userId") Long userId) {
        onClose(session, tripId, userId);
    }

    // functions to handle create, edit and deleting of a trip message

    private void handleCreate(Session session, Long me, Long tripId, String clientMsgId, JsonNode p) {
        try {
            TripMessageCreateDTO in = new TripMessageCreateDTO(
                    textOrNull(p, "content"),
                    longOrNull(p, "parentMessageId"),
                    longOrNull(p, "forwardFromMessageId"),
                    textOrNull(p, "clientMessageId") != null ? textOrNull(p, "clientMessageId") : clientMsgId,
                    instantOrNull(p, "sentAt")
            );
            TripMessageResponseDTO saved = tripSvc().post(me, tripId, in);

            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "TRIP",
                    "channelId", tripId,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", saved.id()
            )));
            sendToTrip(tripId, json(Map.of(
                    "event", "MESSAGE_NEW",
                    "channelType", "TRIP",
                    "channelId", tripId,
                    "message", saved,
                    "timestamp", Instant.now().toString()
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleEdit(Session session, Long me, Long tripId, String clientMsgId, JsonNode p) {
        try {
            Long messageId = longOrNull(p, "messageId");
            TripMessageEditDTO in = new TripMessageEditDTO(
                    textOrNull(p, "content"),
                    longOrNull(p, "version")
            );
            TripMessageResponseDTO updated = tripSvc().edit(me, tripId, messageId, in);

            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "TRIP",
                    "channelId", tripId,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", updated.id()
            )));
            sendToTrip(tripId, json(Map.of(
                    "event", "EDIT",
                    "channelType", "TRIP",
                    "channelId", tripId,
                    "message", updated,
                    "timestamp", Instant.now().toString()
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleDelete(Session session, Long me, Long tripId, String clientMsgId, JsonNode p) {
        try {
            Long messageId = longOrNull(p, "messageId");
            Long version   = longOrNull(p, "version"); // REQUIRED by your service
            TripMessageResponseDTO deleted = tripSvc().delete(me, tripId, messageId, version);

            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "TRIP",
                    "channelId", tripId,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", deleted.id()
            )));
            sendToTrip(tripId, json(Map.of(
                    "event", "DELETE",
                    "channelType", "TRIP",
                    "channelId", tripId,
                    "message", deleted,
                    "timestamp", Instant.now().toString()
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleReaction(Session session, Long me, Long tripId, String clientMsgId, JsonNode p) {
        try {
            Long messageId = longOrNull(p, "messageId");
            String emoji   = textOrNull(p, "emoji");
            tripSvc().toggleReaction(me, tripId, messageId, emoji);

            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "TRIP",
                    "channelId", tripId,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", messageId
            )));
            sendToTrip(tripId, json(Map.of(
                    "event", "REACTION_TOGGLE",
                    "channelType", "TRIP",
                    "channelId", tripId,
                    "messageId", messageId,
                    "emoji", emoji,
                    "timestamp", Instant.now().toString()
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleTyping(Long me, Long tripId, String event) {
        sendToTrip(tripId, json(Map.of(
                "event", event,
                "channelType", "TRIP",
                "channelId", tripId,
                "senderId", me,
                "timestamp", Instant.now().toString()
        )));
    }

    // helper functions

    public static void sendToTrip(Long tripId, String json) {
        var set = BY_TRIP.get(tripId);
        if (set == null) return;
        for (Session s : set) {
            if (s.isOpen()) sendSafe(s, json);
        }
    }

    private static void sendSafe(Session s, String json) {
        if (s == null || json == null) return;
        try { s.getBasicRemote().sendText(json); } catch (Exception ignored) {}
    }

    private static String json(Map<String, Object> map) {
        try { return OM.writeValueAsString(map); } catch (Exception e) { return null; }
    }

    private static String optText(JsonNode n, String field) {
        return n.hasNonNull(field) ? n.get(field).asText() : null;
    }

    private static String textOrNull(JsonNode n, String field) {
        return optText(n, field);
    }

    private static Long longOrNull(JsonNode n, String field) {
        return n.hasNonNull(field) ? n.get(field).asLong() : null;
    }

    private static Instant instantOrNull(JsonNode n, String field) {
        String t = textOrNull(n, field);
        try { return (t != null) ? Instant.parse(t) : null; } catch (Exception e) { return null; }
    }

    private static String safeReason(Exception e) {
        String m = e.getMessage();
        if (m == null || m.isBlank()) return "server error";
        return m.length() > 256 ? m.substring(0, 256) : m;
    }
}
