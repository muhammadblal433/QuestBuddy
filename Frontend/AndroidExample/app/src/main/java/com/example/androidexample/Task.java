package com.example.androidexample;

// the Task class defines the structure of a task object used throughout the app.
// it mirrors the backend model so the frontend and server can share data easily.
public class Task {
    private long taskId;
    private String title;
    private String description;
    private String status;
    private String dueDate;

    public Task(long taskId, String title, String description, String status, String dueDate) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
    }

    public long getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getDueDate() { return dueDate; }
}
