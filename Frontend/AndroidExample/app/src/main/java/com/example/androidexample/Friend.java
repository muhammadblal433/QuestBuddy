package com.example.androidexample;

public class Friend {

    private long id; // friend id
    private String displayName; // name shown on screen
    private String username; // user login name
    private String status; // current status
    private boolean incoming; // true if it's an incoming request
    private int mutualCount; // number of mutual friends
    private String email; // email

    public long getId() { return id; } // get id
    public void setId(long id) { this.id = id; } // set id

    public String getDisplayName() { return displayName; } // get display name
    public void setDisplayName(String displayName) { this.displayName = displayName; } // set display name

    public String getUsername() { return username; } // get username
    public void setUsername(String username) { this.username = username; } // set username

    public String getStatus() { return status; } // get user status
    public void setStatus(String status) { this.status = status; } // set user status

    public String getEmail() { return email; } // get email

    public void setEmail(String email) { this.email = email; } // set email

    public int getMutualCount() { return mutualCount; } // get mutual count
    public void setMutualCount(int mutualCount) { this.mutualCount = mutualCount; } // set mutual count

    public boolean isIncoming() { return incoming; } // check if incoming
    public void setIncoming(boolean incoming) { this.incoming = incoming; } // set incoming flag
}
