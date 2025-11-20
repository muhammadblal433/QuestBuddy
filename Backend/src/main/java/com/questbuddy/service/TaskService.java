package com.questbuddy.service;

import com.questbuddy.task.model.Task;
import java.util.List;
import java.util.Optional;

/**
 * Service boundary for Task domain logic.
 * Provides create, read, update, delete, and user-scoped lookup operations.
 * Implementations typically wrap a repository and apply validation/transactions.
 */
public interface TaskService {
    Task createTask(Task task);
    List<Task> getAllTasks();
    Optional<Task> getTaskById(Long id);
    List<Task> getTasksByUserId(Long userId);
    Task updateTask(Long id, Task updatedTask);
    void deleteTask(Long id);
}