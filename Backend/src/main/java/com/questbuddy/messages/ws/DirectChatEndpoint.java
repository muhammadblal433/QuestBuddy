package com.questbuddy.messages.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.config.SpringContext;
import com.questbuddy.messages.direct.dto.DirectMessageCreateDTO;
import com.questbuddy.messages.direct.dto.DirectMessageEditDTO;
import com.questbuddy.messages.direct.dto.DirectMessageResponseDTO;
import com.questbuddy.messages.direct.service.DirectMessageService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Two-way Direct Message WebSocket endpoint.
 * Connect to: ws://<host>/ws/messages/dm/{pairKey}/{userId} where pairKey = "<minUserId>:<maxUserId>"
 *
 * This endpoint takes into account for creating messages, editing and deleting them,
 * reacting and toggling reactions to a message, and "is typing".
 */
@Component
@ServerEndpoint("/ws/messages/dm/{pairKey}/{userId}")
public class DirectChatEndpoint {

    private static final Map<String, Set<Session>> BY_PAIR = new ConcurrentHashMap<>();
    private static final ObjectMapper OM = new ObjectMapper();

    private DirectMessageService directSvc() { return SpringContext.getBean(DirectMessageService.class); }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("pairKey") String pairKey,
                       @PathParam("userId") Long userId) {
        // minimal sanity: ensure userId is part of pairKey
        long[] ab = parsePairKey(pairKey);
        if (ab == null || (userId != ab[0] && userId != ab[1])) {
            try { session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "user not in pairKey")); } catch (Exception ignored) {}
            return;
        }
        BY_PAIR.computeIfAbsent(pairKey, k -> ConcurrentHashMap.newKeySet()).add(session);
        sendSafe(session, json(Map.of(
                "event", "HELLO",
                "channelType", "DM",
                "channelId", pairKey,
                "timestamp", Instant.now().toString()
        )));
    }

    @OnMessage
    public void onMessage(String msg,
                          Session session,
                          @PathParam("pairKey") String pairKey,
                          @PathParam("userId") Long me) {
        // large message case
        if (msg != null && msg.length() > 64_000) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", "payload too large")));
            return;
        }
        try {
            JsonNode root = OM.readTree(msg);
            String event = optText(root, "event");
            String clientMsgId = optText(root, "clientMsgId");

            if ("PING".equalsIgnoreCase(event)) { sendSafe(session, json(Map.of("event", "PONG"))); return; }

            long[] ab = parsePairKey(pairKey);
            if (ab == null) { sendSafe(session, json(Map.of("event", "ERROR", "reason", "bad pairKey"))); return; }
            long other = (me.equals(ab[0])) ? ab[1] : ab[0];

            switch (event) {
                case "MESSAGE_NEW":
                    handleCreate(session, me, other, pairKey, clientMsgId, root.path("payload"));
                    break;
                case "EDIT":
                    handleEdit(session, me, other, pairKey, clientMsgId, root.path("payload"));
                    break;
                case "DELETE":
                    handleDelete(session, me, other, pairKey, clientMsgId, root.path("payload"));
                    break;
                case "REACTION_ADD":
                case "REACTION_REMOVE":
                    handleReaction(session, me, other, pairKey, clientMsgId, root.path("payload"));
                    break;
                case "READ_RECEIPT":
                    handleReadReceipt(session, me, other, pairKey, clientMsgId, root.path("payload"));
                    break;
                case "TYPING_START":
                case "TYPING_STOP":
                    handleTyping(other, pairKey, event);
                    break;
                default:
                    sendSafe(session, json(Map.of("event", "ERROR", "reason", "unknown event")));
            }
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", "bad JSON")));
        }
    }

    @OnClose
    public void onClose(Session session,
                        @PathParam("pairKey") String pairKey,
                        @PathParam("userId") Long userId) {
        var set = BY_PAIR.get(pairKey);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) BY_PAIR.remove(pairKey);
        }
    }

    @OnError
    public void onError(Session session, Throwable t,
                        @PathParam("pairKey") String pairKey,
                        @PathParam("userId") Long userId) {
        onClose(session, pairKey, userId);
    }

    // functions to handle create, edit and deleting of a trip message

    private void handleCreate(Session session, Long me, Long other, String pairKey, String clientMsgId, JsonNode p) {
        try {
            DirectMessageCreateDTO in = new DirectMessageCreateDTO(
                    textOrNull(p, "content"),
                    longOrNull(p, "parentMessageId"),
                    longOrNull(p, "forwardFromMessageId"),
                    textOrNull(p, "clientMessageId") != null ? textOrNull(p, "clientMessageId") : clientMsgId,
                    instantOrNull(p, "sentAt")
            );
            DirectMessageResponseDTO saved = directSvc().post(me, other, in);

            // ACK only; service will broadcast to both peers via ChatBroadcaster
            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "DM",
                    "channelId", pairKey,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", saved.id()
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleEdit(Session session, Long me, Long other, String pairKey, String clientMsgId, JsonNode p) {
        try {
            Long messageId = longOrNull(p, "messageId");
            DirectMessageEditDTO in = new DirectMessageEditDTO(
                    textOrNull(p, "content"),
                    longOrNull(p, "version")
            );
            DirectMessageResponseDTO updated = directSvc().edit(me, other, messageId, in);

            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "DM",
                    "channelId", pairKey,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", updated.id()
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleDelete(Session session, Long me, Long other, String pairKey, String clientMsgId, JsonNode p) {
        try {
            Long messageId = longOrNull(p, "messageId");   // DM delete does NOT require version
            directSvc().delete(me, other, messageId);

            // ACK only; DM service will broadcast the DELETE event
            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "DM",
                    "channelId", pairKey,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", messageId
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleReaction(Session session, Long me, Long other, String pairKey, String clientMsgId, JsonNode p) {
        try {
            Long messageId = longOrNull(p, "messageId");
            String emoji   = textOrNull(p, "emoji");
            directSvc().toggleReaction(me, other, messageId, emoji);

            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "DM",
                    "channelId", pairKey,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", messageId
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleReadReceipt(Session session, Long me, Long other, String pairKey, String clientMsgId, JsonNode p) {
        try {
            Long messageId = longOrNull(p, "messageId");
            // call your existing markRead; its signature may be (me, other, messageId) or similar
            directSvc().markRead(me, other, messageId);

            sendSafe(session, json(Map.of(
                    "event", "ACK",
                    "channelType", "DM",
                    "channelId", pairKey,
                    "clientMsgId", clientMsgId,
                    "serverMsgId", messageId
            )));
        } catch (Exception e) {
            sendSafe(session, json(Map.of("event", "ERROR", "reason", safeReason(e))));
        }
    }

    private void handleTyping(Long other, String pairKey, String event) {
        // Ephemeral broadcast to the pair; no DB.
        sendToPair(pairKey, json(Map.of(
                "event", event,
                "channelType", "DM",
                "channelId", pairKey,
                "timestamp", Instant.now().toString()
        )));
    }

    // --- utilities ---

    /** Canonical key helper */
    public static String canonicalPairKey(long u1, long u2) {
        long a = Math.min(u1, u2);
        long b = Math.max(u1, u2);
        return a + ":" + b;
    }

    private static long[] parsePairKey(String key) {
        try {
            String[] p = key.split(":", 2);
            long a = Long.parseLong(p[0]);
            long b = Long.parseLong(p[1]);
            return new long[]{Math.min(a,b), Math.max(a,b)};
        } catch (Exception e) {
            return null;
        }
    }

    public static void sendToPair(String pairKey, String json) {
        var set = BY_PAIR.get(pairKey);
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

    private static String optText(JsonNode n, String field) { return n.hasNonNull(field) ? n.get(field).asText() : null; }
    private static String textOrNull(JsonNode n, String field) { return optText(n, field); }
    private static Long longOrNull(JsonNode n, String field) { return n.hasNonNull(field) ? n.get(field).asLong() : null; }
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
