package com.questbuddy.service;

import com.questbuddy.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskService {
    Task createTask(Task task);
    List<Task> getAllTasks();
    Optional<Task> getTaskById(Long id);
    List<Task> getTasksByUserId(Long userId);
    Task updateTask(Long id, Task updatedTask);
    void deleteTask(Long id);
}