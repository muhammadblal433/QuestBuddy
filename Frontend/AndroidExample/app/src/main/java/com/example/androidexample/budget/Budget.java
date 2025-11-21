package com.example.androidexample.budget;

/**
 * Represents a budget in the QuestBuddy app.
 * A budget belongs to an owner and tracks the total share
 * and total amount that has been paid, along with metadata like
 * name and creation timestamp.
 */
public class Budget {
    private long id;
    private String name, ownerUsername, createdAt;
    private double totalShare, totalPaid;

    /**
     * Constructs a new {@code Budget} instance.
     *
     * @param id            unique identifier of the budget
     * @param name          display name of the budget
     * @param ownerUsername username of the budget owner
     * @param totalShare    total share amount across all participants
     * @param totalPaid     total amount that has been paid so far
     * @param createdAt     timestamp string indicating when the budget was created
     */
    // constructor to make a budget object
    public Budget(long id, String name, String ownerUsername, double totalShare, double totalPaid, String createdAt) {
        this.id = id;
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.totalShare = totalShare;
        this.totalPaid = totalPaid;
        this.createdAt = createdAt;
    }

    /**
     * Returns the unique identifier of this budget.
     *
     * @return the budget id
     */
    // get budget id
    public long getId() { return id; }

    /**
     * Returns the display name of this budget.
     *
     * @return the budget name
     */
    // get budget name
    public String getName() { return name; }

    /**
     * Returns the username of the user who owns this budget.
     *
     * @return the owner's username
     */
    // get owner's username
    public String getOwnerUsername() { return ownerUsername; }

    /**
     * Returns the total share amount across all users in the budget.
     *
     * @return the total share amount
     */
    // get total share amount
    public double getTotalShare() { return totalShare; }

    /**
     * Returns the total amount that has been paid into the budget.
     *
     * @return the total paid amount
     */
    // get total paid amount
    public double getTotalPaid() { return totalPaid; }

    /**
     * Returns the creation timestamp of this budget as a string.
     *
     * @return the creation date string
     */
    // get creation date
    public String getCreatedAt() { return createdAt; }
}