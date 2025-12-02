package com.example.androidexample.trips;

import android.os.Handler;
import android.os.Looper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;


public class TripWebSocket {
    public interface WsListener {
        void onOpen();
        void onMessage(String json);
        void onClosed(int code, String reason);
        void onFailure(Throwable t);
    }


    private final String baseWsUrl; // websocket url
    private final long me;
    private final long tripId;
    private final WsListener listener;
    private WebSocketClient client;
    private final Handler main = new Handler(Looper.getMainLooper());


    public TripWebSocket(String baseWsUrl, long me, long tripId, WsListener listener) {
        this.baseWsUrl = baseWsUrl; this.me = me; this.tripId = tripId; this.listener = listener;
    }

    public void connect() {
        try {
            URI uri = URI.create(baseWsUrl + "/ws/messages/trips/" + tripId + "/" + me);
            Map<String, String> headers = new HashMap<>();
            client = new WebSocketClient(uri, new Draft_6455(), headers, 10_000) {
                @Override public void onOpen(ServerHandshake handshakedata) { post(() -> listener.onOpen()); }
                @Override public void onMessage(String message) { post(() -> listener.onMessage(message)); }
                @Override public void onClose(int code, String reason, boolean remote) { post(() -> listener.onClosed(code, reason)); }
                @Override public void onError(Exception ex) { post(() -> listener.onFailure(ex)); }
            };
            if (uri.getScheme().equalsIgnoreCase("wss")) {
                try { SSLContext ctx = SSLContext.getInstance("TLS"); ctx.init(null, null, null); client.setSocketFactory(ctx.getSocketFactory()); } catch (Exception ignored) {}
            }
            client.connect();
        } catch (Exception e) { if (listener != null) listener.onFailure(e); }
    }


    public void close() { try { if (client != null) client.close(1000, "bye"); } catch (Exception ignored) {} client = null; }
    private void post(Runnable r) { if (Looper.myLooper() == Looper.getMainLooper()) r.run(); else main.post(r); }
}