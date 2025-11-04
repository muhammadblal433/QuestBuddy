// src/main/java/com/questbuddy/notification/ws/NotificationEndpoint.java
package com.questbuddy.notification.ws;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raw WebSocket endpoint (JSR-356) that keeps connections per userId.
 * Connect to: ws://<host>:8080/ws/notifications/{userId}
 */
@ServerEndpoint("/ws/notifications/{userId}")
@Component
public class NotificationEndpoint {

    // userId -> connected sessions
    private static final Map<Long, Set<Session>> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        SESSIONS.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @OnMessage
    public void onMessage(String msg, Session session, @PathParam("userId") Long userId) {
        // Optional: basic ping/pong
        try { session.getBasicRemote().sendText("{\"kind\":\"PONG\"}"); } catch (Exception ignored) {}
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        var set = SESSIONS.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) SESSIONS.remove(userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable t, @PathParam("userId") Long userId) {
        onClose(session, userId);
    }

    /** Called by Spring beans to push a JSON message to all sessions for a user. */
    public static void sendToUser(Long userId, String json) {
        var set = SESSIONS.get(userId);
        if (set == null) return;
        for (Session s : set) {
            if (s.isOpen()) {
                try { s.getBasicRemote().sendText(json); } catch (Exception ignored) {}
            }
        }
    }
}