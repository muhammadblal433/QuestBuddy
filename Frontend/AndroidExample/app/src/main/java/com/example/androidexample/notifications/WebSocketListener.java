package com.example.androidexample.notifications;

import org.java_websocket.handshake.ServerHandshake;

// defines methods that respond to websocket events like open, message, close, and error
public interface WebSocketListener {
    void onWebSocketOpen(ServerHandshake handshake);
    void onWebSocketMessage(String message);
    void onWebSocketClose(int code, String reason, boolean remote);
    void onWebSocketError(Exception ex);
}

