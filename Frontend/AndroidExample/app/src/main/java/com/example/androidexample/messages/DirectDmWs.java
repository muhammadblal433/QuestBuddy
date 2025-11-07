package com.example.androidexample.messages;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class DirectDmWs {

    public interface Listener {
        void onOpen();
        void onClose(int code, String reason, boolean willRetry);
        void onError(Throwable t);
        void onEvent(JSONObject event);
    }

    private static final String TAG = "DM-WS";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final String url;
    private final long userId;
    private final Listener listener;

    @Nullable private WebSocketClient ws;
    private boolean manualClose = false;

    // reconnect backoff
    private int attempt = 0;

    public DirectDmWs(String url, long userId, Listener listener) {
        this.url = url;
        this.userId = userId;
        this.listener = listener;
    }

    public void connect() {
        manualClose = false;

        try {
            URI uri = new URI(url);

            Map<String, String> headers = new HashMap<>();
            headers.put("X-User-Id", String.valueOf(userId));

            ws = new WebSocketClient(uri, new Draft_6455(), headers, 0) {
                @Override public void onOpen(ServerHandshake handshakedata) {
                    attempt = 0;
                    // keep-alive (seconds). Library will send pings internally.
                    setConnectionLostTimeout(30);
                    postMain(listener::onOpen);
                }

                @Override public void onMessage(String message) {
                    try {
                        JSONObject evt = new JSONObject(message);
                        postMain(() -> listener.onEvent(evt));
                    } catch (JSONException e) {
                        Log.w(TAG, "Non-JSON message: " + message);
                    }
                }

                @Override public void onClose(int code, String reason, boolean remote) {
                    boolean willRetry = !manualClose;
                    postMain(() -> listener.onClose(code, reason, willRetry));
                    if (willRetry) scheduleReconnect();
                }

                @Override public void onError(Exception ex) {
                    Log.e(TAG, "WS error", ex);
                    boolean willRetry = !manualClose;
                    postMain(() -> listener.onError(ex));
                    // onError may be called before/without onClose; schedule reconnect
                    if (willRetry) scheduleReconnect();
                }
            };

            // Enable TLS for wss://
            if ("wss".equalsIgnoreCase(uri.getScheme())) {
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, null, new SecureRandom());
                    SSLSocketFactory factory = sslContext.getSocketFactory();
                    ws.setSocket(factory.createSocket());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to init TLS; connecting without custom factory", e);
                }
            }

            ws.connect(); // async
        } catch (Exception e) {
            Log.e(TAG, "connect() error", e);
            postMain(() -> listener.onError(e));
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        attempt++;
        long delayMs = Math.min(30_000, (long) Math.pow(2, Math.min(6, attempt)) * 500L);
        main.postDelayed(this::connect, delayMs);
    }

    public void close() {
        manualClose = true;
        if (ws != null) {
            try { ws.close(1000, "bye"); } catch (Exception ignored) {}
            ws = null;
        }
    }

    public void send(JSONObject obj) {
        WebSocketClient s = ws;
        if (s == null || !s.isOpen()) return;   // or throw / log
        s.send(obj.toString());
    }

    private void postMain(Runnable r) { main.post(r); }
}