package com.questbuddy.messages.ws;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trip chat WebSocket endpoint.
 *
 * Keep an in-memory registry of sessions per tripId.
 * Provide a static sendToTrip(...) used by ChatBroadcaster.
 */
@Component
@ServerEndpoint("/ws/messages/trips/{tripId}/{userId}")
public class TripChatEndpoint {

    // tripId -> set of sessions
    private static final Map<Long, Set<Session>> BY_TRIP = new ConcurrentHashMap<>();

    // Adds the session to the trip’s set
    @OnOpen
    public void onOpen(Session session,
                       @PathParam("tripId") Long tripId,
                       @PathParam("userId") Long userId) {
        BY_TRIP.computeIfAbsent(tripId, k -> ConcurrentHashMap.newKeySet()).add(session);
        // (Optional) greet
        try { session.getBasicRemote().sendText("{\"event\":\"HELLO\",\"channelType\":\"TRIP\",\"channelId\":"+tripId+"}"); } catch (Exception ignored) {}
    }

    // currently only one way
    @OnMessage
    public void onMessage(String msg, Session session,
                          @PathParam("tripId") Long tripId,
                          @PathParam("userId") Long userId) {
        try { session.getBasicRemote().sendText("{\"event\":\"PONG\"}"); } catch (Exception ignored) {}
    }

    // Cleans up the session
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

    /** Called by ChatBroadcaster to push a JSON message to all sessions subscribed to a trip. */
    public static void sendToTrip(Long tripId, String json) {
        var set = BY_TRIP.get(tripId);
        if (set == null) return;
        for (Session s : set) {
            if (s.isOpen()) {
                try { s.getBasicRemote().sendText(json); } catch (Exception ignored) {}
            }
        }
    }
}
