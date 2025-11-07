package com.example.androidexample.friends;

public class Friend {

    private long id;               // friend id
    private String displayName;    // name shown on screen
    private String username;       // user login name
    private long currentUserID;    // current user's ID (primitive to avoid NPE)
    private String status;         // current status
    private boolean incoming;      // true if it's an incoming request
    private int mutualCount;       // number of mutual friends
    private String email;          // email

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCurrentId() { return currentUserID; }
    public void setCurrentUserID(long id) { this.currentUserID = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getMutualCount() { return mutualCount; }
    public void setMutualCount(int mutualCount) { this.mutualCount = mutualCount; }

    public boolean isIncoming() { return incoming; }
    public void setIncoming(boolean incoming) { this.incoming = incoming; }
}