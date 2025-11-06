package com.example.androidexample;

public class NotificationModel {
    private long id;
    private long recipientId;
    private String title;
    private String message;
    private String type;
    private String createdAt;
    private boolean isRead;

    // constructor to initialize all notification fields
    public NotificationModel(long id, long recipientId, String title, String message,
                             String type, String createdAt, boolean isRead) {
        this.id = id;
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // returns the notification id
    public long getId() { return id; }
    // returns the recipient id
    public long getRecipientId() { return recipientId; }

    // returns the title of the notification
    public String getTitle() { return title; }

    // returns the message body of the notification
    public String getMessage() { return message; }

    // returns the notification type
    public String getType() { return type; }

    // returns the creation time of the notification
    public String getCreatedAt() { return createdAt; }

    // returns true if the notification has been read
    public boolean isRead() { return isRead; }


    // sets the read status of the notification
    public void setRead(boolean read) { isRead = read; }
}
