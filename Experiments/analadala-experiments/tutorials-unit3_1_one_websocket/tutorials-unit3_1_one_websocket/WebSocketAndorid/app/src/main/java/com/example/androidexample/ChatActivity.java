package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.handshake.ServerHandshake;

import java.util.Iterator;

/**
 * ChatActivity handles the chat interface where users can send and receive messages
 * using a WebSocket connection.
 */
public class ChatActivity extends AppCompatActivity implements WebSocketListener{

    private Button sendBtn;
    private EditText msgEtx, usernameEtx;
    private TextView msgTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /* initialize UI elements */
        sendBtn = (Button) findViewById(R.id.sendBtn);
        msgEtx = (EditText) findViewById(R.id.msgEdt);
        msgTv = (TextView) findViewById(R.id.tx1);
        usernameEtx = findViewById(R.id.usernameEdt);

        Button clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(v -> msgTv.setText(""));

        sendBtn.setEnabled(false);

        //connects to the websocket server
        WebSocketManager.getInstance().connectWebSocket("wss://ws.ifelse.io");

        /* connect this activity to the websocket instance */
        WebSocketManager.getInstance().setWebSocketListener(ChatActivity.this);

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            try {
                String username = usernameEtx.getText().toString().trim();
                String rawMessage = msgEtx.getText().toString().trim();

                if (username.isEmpty()) {
                    msgTv.setText("Please enter a username first.");
                    return;
                }

                if (!rawMessage.isEmpty()) {
                    String messageToSend = username + ": " + rawMessage;
                    WebSocketManager.getInstance().sendMessage(messageToSend);
                    msgEtx.setText("");
                }
            } catch (Exception e) {
                Log.e("WebSocketError", "Error sending message", e);
            }
        });
    }


    /**
     * Called when a message is received from the WebSocket.
     * This method ensures that UI updates happen on the main thread.
     */
    @Override
    public void onWebSocketMessage(String message) {
        /**
         * In Android, all UI-related operations must be performed on the main UI thread
         * to ensure smooth and responsive user interfaces. The 'runOnUiThread' method
         * is used to post a runnable to the UI thread's message queue, allowing UI updates
         * to occur safely from a background or non-UI thread.
         */
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "\n"+message);
        });
    }

    /**
     * Called when the WebSocket connection is closed.
     * Displays the closure reason in the TextView.
     *
     * @param code   The status code of the closure
     * @param reason The reason provided for closure
     */
    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(()->{
            msgTv.setText("Connected to websocket server");
            sendBtn.setEnabled(true);
        });
    }


    @Override
    public void onWebSocketError(Exception ex) {}
}