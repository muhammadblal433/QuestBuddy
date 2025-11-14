package com.example.androidexample.budget;

public class Budget {
    private long id;
    private String name, ownerUsername, createdAt;
    private double totalShare, totalPaid;

    // constructor to make a budget object
    public Budget(long id, String name, String ownerUsername, double totalShare, double totalPaid, String createdAt) {
        this.id = id;
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.totalShare = totalShare;
        this.totalPaid = totalPaid;
        this.createdAt = createdAt;
    }

    // get budget id
    public long getId() { return id; }

    // get budget name
    public String getName() { return name; }

    // get owner's username
    public String getOwnerUsername() { return ownerUsername; }

    // get total share amount
    public double getTotalShare() { return totalShare; }

    // get total paid amount
    public double getTotalPaid() { return totalPaid; }

    // get creation date
    public String getCreatedAt() { return createdAt; }
}
