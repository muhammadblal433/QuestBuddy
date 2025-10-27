package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.ScrollView;

import org.java_websocket.handshake.ServerHandshake;


public class ChatActivity1 extends AppCompatActivity implements WebSocketListener{

    private Button sendBtn, backMainBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);

        /* initialize UI elements */
        sendBtn = (Button) findViewById(R.id.sendBtn);
        backMainBtn = (Button) findViewById(R.id.backMainBtn);
        msgEtx = (EditText) findViewById(R.id.msgEdt);
        msgTv = (TextView) findViewById(R.id.tx1);

        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            username = "User"; // fallback if not provided
        }

        /* connect this activity to the websocket instance */
        WebSocketManager1.getInstance().setWebSocketListener(ChatActivity1.this);

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(ChatActivity1.this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
            } else {
                try {

                    String formattedMessage = username + ": " + message; // username pops up
                    // send message to WebSocket
                    WebSocketManager1.getInstance().sendMessage(formattedMessage);

                    // show message immediately in chat window
                    String s = msgTv.getText().toString();
                    String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    msgTv.setText(s + "\n[" + time + "] " + username + ": " + message);

                    // clear input box
                    msgEtx.setText("");
                } catch (Exception e) {
                    Log.d("ExceptionSendMessage:", e.getMessage());
                }
            }
        });

        /* back button listener */
        backMainBtn.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }


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
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            msgTv.setText(s + "\n[" + time + "] " + username + ": " + message);
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {}

    @Override
    public void onWebSocketError(Exception ex) {}
}