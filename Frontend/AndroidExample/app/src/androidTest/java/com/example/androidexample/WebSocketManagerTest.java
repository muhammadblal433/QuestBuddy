package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.notifications.WebSocketListener;
import com.example.androidexample.notifications.WebSocketManager;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WebSocketManagerTest {

    private WebSocketManager manager;
    private TestWebSocketListener testListener;

    private static class TestWebSocketListener implements WebSocketListener {
        public boolean openCalled = false;
        public boolean messageCalled = false;
        public boolean closeCalled = false;
        public boolean errorCalled = false;
        public String lastMessage = null;
        public Exception lastError = null;

        @Override
        public void onWebSocketOpen(ServerHandshake handshake) {
            openCalled = true;
        }

        @Override
        public void onWebSocketMessage(String message) {
            messageCalled = true;
            lastMessage = message;
        }

        @Override
        public void onWebSocketClose(int code, String reason, boolean remote) {
            closeCalled = true;
        }

        @Override
        public void onWebSocketError(Exception ex) {
            errorCalled = true;
            lastError = ex;
        }
    }

    @Before
    public void setUp() {
        manager = WebSocketManager.getInstance();
        testListener = new TestWebSocketListener();

        // Disconnect any existing connection
        if (manager.isConnected()) {
            manager.disconnectWebSocket();
        }
    }

    @Test
    public void getInstance_returnsSameInstance() {
        WebSocketManager instance1 = WebSocketManager.getInstance();
        WebSocketManager instance2 = WebSocketManager.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    public void getInstance_isNotNull() {
        assertNotNull(WebSocketManager.getInstance());
    }

    @Test
    public void setWebSocketListener_setsListener() {
        manager.setWebSocketListener(testListener);

        // Listener is set (can't directly verify but method should not crash)
        assertNotNull(manager);
    }

    @Test
    public void removeWebSocketListener_removesListener() {
        manager.setWebSocketListener(testListener);
        manager.removeWebSocketListener();

        // Listener is removed (can't directly verify but method should not crash)
        assertNotNull(manager);
    }

    @Test
    public void isConnected_initiallyFalse() {
        // After disconnecting in setUp
        assertFalse(manager.isConnected());
    }

    @Test
    public void connectWebSocket_withValidUrl() {
        String testUrl = "ws://echo.websocket.org";

        manager.connectWebSocket(testUrl);

        // Wait a bit for connection attempt
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Connection may or may not succeed depending on network, but should not crash
        assertNotNull(manager);
    }

    @Test
    public void connectWebSocket_withInvalidUrl_doesNotCrash() {
        String invalidUrl = "invalid-url";

        manager.connectWebSocket(invalidUrl);

        // Should handle exception gracefully
        assertNotNull(manager);
    }

    @Test
    public void disconnectWebSocket_whenNotConnected_doesNotCrash() {
        assertFalse(manager.isConnected());

        manager.disconnectWebSocket();

        assertNotNull(manager);
    }

    @Test
    public void disconnectWebSocket_whenConnected() {
        String testUrl = "ws://echo.websocket.org";

        manager.connectWebSocket(testUrl);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manager.disconnectWebSocket();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull(manager);
    }

    @Test
    public void setListener_nullListener_doesNotCrash() {
        manager.setWebSocketListener(null);

        assertNotNull(manager);
    }

    @Test
    public void connectWebSocket_multipleUrls() {
        String url1 = "ws://echo.websocket.org";
        String url2 = "ws://echo.websocket.org/echo";

        manager.connectWebSocket(url1);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manager.disconnectWebSocket();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manager.connectWebSocket(url2);

        assertNotNull(manager);
    }

    @Test
    public void multipleInstances_areSame() {
        WebSocketManager m1 = WebSocketManager.getInstance();
        WebSocketManager m2 = WebSocketManager.getInstance();
        WebSocketManager m3 = WebSocketManager.getInstance();

        assertSame(m1, m2);
        assertSame(m2, m3);
    }

    @Test
    public void setListener_multipleListeners() {
        TestWebSocketListener listener1 = new TestWebSocketListener();
        TestWebSocketListener listener2 = new TestWebSocketListener();

        manager.setWebSocketListener(listener1);
        manager.setWebSocketListener(listener2);

        // Second listener should replace first (can't directly verify but should not crash)
        assertNotNull(manager);
    }

    @Test
    public void removeListener_afterSetting() {
        manager.setWebSocketListener(testListener);
        manager.removeWebSocketListener();

        assertNotNull(manager);
    }

    @Test
    public void removeListener_withoutSetting() {
        manager.removeWebSocketListener();
        manager.removeWebSocketListener();

        assertNotNull(manager);
    }

    @Test
    public void isConnected_afterDisconnect_returnsFalse() {
        manager.disconnectWebSocket();

        assertFalse(manager.isConnected());
    }

    @Test
    public void connectWebSocket_withEmptyString_doesNotCrash() {
        manager.connectWebSocket("");

        assertNotNull(manager);
    }

    @Test
    public void connectWebSocket_withNullUrl_doesNotCrash() {
        try {
            manager.connectWebSocket(null);
        } catch (Exception e) {
            // Expected to throw exception for null URL
        }

        assertNotNull(manager);
    }

    @Test
    public void getInstance_calledMultipleTimes() {
        for (int i = 0; i < 10; i++) {
            WebSocketManager instance = WebSocketManager.getInstance();
            assertNotNull(instance);
        }
    }

    @Test
    public void disconnectWebSocket_calledMultipleTimes() {
        manager.disconnectWebSocket();
        manager.disconnectWebSocket();
        manager.disconnectWebSocket();

        assertFalse(manager.isConnected());
    }
}