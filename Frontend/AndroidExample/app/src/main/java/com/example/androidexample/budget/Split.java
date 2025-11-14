package com.example.androidexample.budget;

// holds data for one participant's split
public class Split {
    private String username;
    private double shareAmount, paidAmount, balance;

    // constructor to set all fields
    public Split(String username, double shareAmount, double paidAmount, double balance) {
        this.username = username;
        this.shareAmount = shareAmount;
        this.paidAmount = paidAmount;
        this.balance = balance;
    }

    // get username
    public String getUsername() { return username; }

    // get share amount
    public double getShareAmount() { return shareAmount; }

    // get paid amount
    public double getPaidAmount() { return paidAmount; }

    // get balance amount
    public double getBalance() { return balance; }

    // set share amount
    public void setShareAmount(double shareAmount) { this.shareAmount = shareAmount; }

    // set paid amount
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }
}
