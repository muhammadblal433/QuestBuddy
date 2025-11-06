package com.example.androidexample;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

// manages websocket connections, sending messages, and notifying listeners about events
public class WebSocketManager {

    private static WebSocketManager instance;
    private MyWebSocketClient webSocketClient;
    private WebSocketListener webSocketListener;

    private WebSocketManager() {}

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void setWebSocketListener(WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    public void removeWebSocketListener() {
        this.webSocketListener = null;
    }

    public void connectWebSocket(String serverUrl) {
        try {
            URI serverUri = URI.create(serverUrl);
            webSocketClient = new MyWebSocketClient(serverUri);
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    private class MyWebSocketClient extends WebSocketClient {
        private MyWebSocketClient(URI serverUri) { super(serverUri); }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("WebSocket", "Connected");
            if (webSocketListener != null) webSocketListener.onWebSocketOpen(handshakedata);
        }

        @Override
        public void onMessage(String message) {
            Log.d("WebSocket", "Received: " + message);
            if (webSocketListener != null) webSocketListener.onWebSocketMessage(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket", "Closed: " + reason);
            if (webSocketListener != null) webSocketListener.onWebSocketClose(code, reason, remote);
        }

        @Override
        public void onError(Exception ex) {
            Log.e("WebSocket", "Error: " + ex.getMessage());
            if (webSocketListener != null) webSocketListener.onWebSocketError(ex);
        }
    }
}
