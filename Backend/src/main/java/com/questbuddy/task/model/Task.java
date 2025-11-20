package com.questbuddy.task.model;

import com.questbuddy.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Foreign key reference

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String status = "Pending";

    @Column(name = "due_date")
    private LocalDate dueDate;

    public Task() {}
    public Task(User user, String title, String description, String status, LocalDate dueDate) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
    }

    public Long getTaskId() {
        return taskId; }

    public void setTaskId(Long taskId) {
        this.taskId = taskId; }

    public User getUser() {
        return user; }

    public void setUser(User user) {
        this.user = user; }

    public String getTitle() {
        return title; }

    public void setTitle(String title) {
        this.title = title; }

    public String getDescription() {
        return description; }

    public void setDescription(String description) {
        this.description = description; }

    public String getStatus() {
        return status; }

    public void setStatus(String status) {
        this.status = status; }

    public LocalDate getDueDate() {
        return dueDate; }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate; }
}