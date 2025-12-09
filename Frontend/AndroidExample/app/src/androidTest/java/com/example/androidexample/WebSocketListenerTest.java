package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.notifications.WebSocketListener;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WebSocketListenerTest {

    private TestWebSocketListener listener;

    // Test implementation of WebSocketListener interface
    private static class TestWebSocketListener implements WebSocketListener {
        public boolean onOpenCalled = false;
        public boolean onMessageCalled = false;
        public boolean onCloseCalled = false;
        public boolean onErrorCalled = false;

        public ServerHandshake lastHandshake = null;
        public String lastMessage = null;
        public int lastCloseCode = -1;
        public String lastCloseReason = null;
        public boolean lastCloseRemote = false;
        public Exception lastError = null;

        @Override
        public void onWebSocketOpen(ServerHandshake handshake) {
            onOpenCalled = true;
            lastHandshake = handshake;
        }

        @Override
        public void onWebSocketMessage(String message) {
            onMessageCalled = true;
            lastMessage = message;
        }

        @Override
        public void onWebSocketClose(int code, String reason, boolean remote) {
            onCloseCalled = true;
            lastCloseCode = code;
            lastCloseReason = reason;
            lastCloseRemote = remote;
        }

        @Override
        public void onWebSocketError(Exception ex) {
            onErrorCalled = true;
            lastError = ex;
        }
    }

    @Before
    public void setUp() {
        listener = new TestWebSocketListener();
    }

    @Test
    public void interface_canBeImplemented() {
        assertNotNull(listener);
        assertTrue(listener instanceof WebSocketListener);
    }

    @Test
    public void onWebSocketOpen_canBeCalled() {
        listener.onWebSocketOpen(null);

        assertTrue(listener.onOpenCalled);
    }

    @Test
    public void onWebSocketOpen_receivesHandshake() {
        ServerHandshake mockHandshake = new ServerHandshake() {
            @Override
            public short getHttpStatus() { return 200; }

            @Override
            public String getHttpStatusMessage() { return "OK"; }

            @Override
            public String getFieldValue(String name) { return null; }

            @Override
            public boolean hasFieldValue(String name) { return false; }

            @Override
            public byte[] getContent() { return new byte[0]; }

            @Override
            public java.util.Iterator<String> iterateHttpFields() {
                return new java.util.ArrayList<String>().iterator();
            }
        };

        listener.onWebSocketOpen(mockHandshake);

        assertTrue(listener.onOpenCalled);
        assertEquals(mockHandshake, listener.lastHandshake);
    }

    @Test
    public void onWebSocketMessage_canBeCalled() {
        listener.onWebSocketMessage("test message");

        assertTrue(listener.onMessageCalled);
        assertEquals("test message", listener.lastMessage);
    }

    @Test
    public void onWebSocketMessage_receivesEmptyString() {
        listener.onWebSocketMessage("");

        assertTrue(listener.onMessageCalled);
        assertEquals("", listener.lastMessage);
    }

    @Test
    public void onWebSocketMessage_receivesNull() {
        listener.onWebSocketMessage(null);

        assertTrue(listener.onMessageCalled);
        assertEquals(null, listener.lastMessage);
    }

    @Test
    public void onWebSocketMessage_receivesJson() {
        String json = "{\"type\":\"notification\",\"data\":\"test\"}";

        listener.onWebSocketMessage(json);

        assertTrue(listener.onMessageCalled);
        assertEquals(json, listener.lastMessage);
    }

    @Test
    public void onWebSocketMessage_receivesLongMessage() {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("test ");
        }

        listener.onWebSocketMessage(longMessage.toString());

        assertTrue(listener.onMessageCalled);
        assertEquals(longMessage.toString(), listener.lastMessage);
    }

    @Test
    public void onWebSocketClose_canBeCalled() {
        listener.onWebSocketClose(1000, "Normal closure", true);

        assertTrue(listener.onCloseCalled);
        assertEquals(1000, listener.lastCloseCode);
        assertEquals("Normal closure", listener.lastCloseReason);
        assertTrue(listener.lastCloseRemote);
    }

    @Test
    public void onWebSocketClose_withDifferentCodes() {
        int[] codes = {1000, 1001, 1002, 1003, 1006, 1011};

        for (int code : codes) {
            TestWebSocketListener tempListener = new TestWebSocketListener();
            tempListener.onWebSocketClose(code, "Test", false);

            assertTrue(tempListener.onCloseCalled);
            assertEquals(code, tempListener.lastCloseCode);
        }
    }

    @Test
    public void onWebSocketClose_withNullReason() {
        listener.onWebSocketClose(1000, null, false);

        assertTrue(listener.onCloseCalled);
        assertEquals(1000, listener.lastCloseCode);
        assertEquals(null, listener.lastCloseReason);
        assertFalse(listener.lastCloseRemote);
    }

    @Test
    public void onWebSocketClose_remoteTrue() {
        listener.onWebSocketClose(1000, "Remote closed", true);

        assertTrue(listener.onCloseCalled);
        assertTrue(listener.lastCloseRemote);
    }

    @Test
    public void onWebSocketClose_remoteFalse() {
        listener.onWebSocketClose(1000, "Local closed", false);

        assertTrue(listener.onCloseCalled);
        assertFalse(listener.lastCloseRemote);
    }

    @Test
    public void onWebSocketError_canBeCalled() {
        Exception testException = new Exception("Test error");

        listener.onWebSocketError(testException);

        assertTrue(listener.onErrorCalled);
        assertEquals(testException, listener.lastError);
    }

    @Test
    public void onWebSocketError_withNullException() {
        listener.onWebSocketError(null);

        assertTrue(listener.onErrorCalled);
        assertEquals(null, listener.lastError);
    }

    @Test
    public void onWebSocketError_withDifferentExceptionTypes() {
        Exception[] exceptions = {
                new Exception("Generic exception"),
                new RuntimeException("Runtime exception"),
                new IllegalArgumentException("Illegal argument"),
                new NullPointerException("Null pointer")
        };

        for (Exception ex : exceptions) {
            TestWebSocketListener tempListener = new TestWebSocketListener();
            tempListener.onWebSocketError(ex);

            assertTrue(tempListener.onErrorCalled);
            assertEquals(ex, tempListener.lastError);
        }
    }

    @Test
    public void allMethods_canBeCalledSequentially() {
        listener.onWebSocketOpen(null);
        listener.onWebSocketMessage("test");
        listener.onWebSocketClose(1000, "Done", true);
        listener.onWebSocketError(new Exception("Error"));

        assertTrue(listener.onOpenCalled);
        assertTrue(listener.onMessageCalled);
        assertTrue(listener.onCloseCalled);
        assertTrue(listener.onErrorCalled);
    }

    @Test
    public void allMethods_canBeCalledMultipleTimes() {
        listener.onWebSocketOpen(null);
        listener.onWebSocketOpen(null);

        listener.onWebSocketMessage("msg1");
        listener.onWebSocketMessage("msg2");
        listener.onWebSocketMessage("msg3");

        listener.onWebSocketClose(1000, "close1", true);
        listener.onWebSocketClose(1001, "close2", false);

        listener.onWebSocketError(new Exception("error1"));
        listener.onWebSocketError(new Exception("error2"));

        assertTrue(listener.onOpenCalled);
        assertTrue(listener.onMessageCalled);
        assertTrue(listener.onCloseCalled);
        assertTrue(listener.onErrorCalled);
    }

    @Test
    public void multipleImplementations_canCoexist() {
        TestWebSocketListener listener1 = new TestWebSocketListener();
        TestWebSocketListener listener2 = new TestWebSocketListener();

        listener1.onWebSocketMessage("message1");
        listener2.onWebSocketMessage("message2");

        assertEquals("message1", listener1.lastMessage);
        assertEquals("message2", listener2.lastMessage);
    }

    @Test
    public void implementation_canBeAssignedToInterface() {
        WebSocketListener interfaceRef = listener;

        interfaceRef.onWebSocketMessage("test");

        assertTrue(listener.onMessageCalled);
    }

    @Test
    public void onWebSocketMessage_withSpecialCharacters() {
        String specialMessage = "Message with special chars: @#$%^&*()[]{}|\\;:'\"<>?,./";

        listener.onWebSocketMessage(specialMessage);

        assertTrue(listener.onMessageCalled);
        assertEquals(specialMessage, listener.lastMessage);
    }

    @Test
    public void onWebSocketMessage_withUnicode() {
        String unicodeMessage = "Unicode: 你好 🎉 مرحبا";

        listener.onWebSocketMessage(unicodeMessage);

        assertTrue(listener.onMessageCalled);
        assertEquals(unicodeMessage, listener.lastMessage);
    }

    @Test
    public void onWebSocketClose_withEmptyReason() {
        listener.onWebSocketClose(1000, "", false);

        assertTrue(listener.onCloseCalled);
        assertEquals("", listener.lastCloseReason);
    }

    @Test
    public void listener_initialState() {
        assertFalse(listener.onOpenCalled);
        assertFalse(listener.onMessageCalled);
        assertFalse(listener.onCloseCalled);
        assertFalse(listener.onErrorCalled);

        assertEquals(null, listener.lastHandshake);
        assertEquals(null, listener.lastMessage);
        assertEquals(-1, listener.lastCloseCode);
        assertEquals(null, listener.lastCloseReason);
        assertFalse(listener.lastCloseRemote);
        assertEquals(null, listener.lastError);
    }
}