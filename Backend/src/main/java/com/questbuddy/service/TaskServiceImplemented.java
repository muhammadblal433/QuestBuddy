package com.questbuddy.service;

import com.questbuddy.model.Task;
import com.questbuddy.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImplemented implements TaskService {

    private final TaskRepository taskRepo;

    public TaskServiceImplemented(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;
    }

    @Override
    public Task createTask(Task task) {
        return taskRepo.save(task);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        return taskRepo.findById(id);
    }

    @Override
    public List<Task> getTasksByUserId(Long userId) {
        return taskRepo.findByUser_Id(userId);
    }

    @Override
    public Task updateTask(Long id, Task updatedTask) {
        return taskRepo.findById(id)
                .map(task -> {
                    if (updatedTask.getTitle() != null)       task.setTitle(updatedTask.getTitle());
                    if (updatedTask.getDescription() != null)  task.setDescription(updatedTask.getDescription());
                    if (updatedTask.getStatus() != null)       task.setStatus(updatedTask.getStatus());
                    if (updatedTask.getDueDate() != null)      task.setDueDate(updatedTask.getDueDate());
                    // keep same user and setUser only if you want reassignment
                    return taskRepo.save(task);
                })
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public void deleteTask(Long id) {
        taskRepo.deleteById(id);
    }
}
