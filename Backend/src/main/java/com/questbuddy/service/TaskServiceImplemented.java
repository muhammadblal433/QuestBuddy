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
        return taskRepo.findByUserId(userId);
    }

    @Override
    public Task updateTask(Long id, Task updatedTask) {
        return taskRepo.findById(id)
                .map(task -> {
                    task.setTitle(updatedTask.getTitle());
                    task.setDescription(updatedTask.getDescription());
                    task.setStatus(updatedTask.getStatus());
                    task.setDueDate(updatedTask.getDueDate());
                    return taskRepo.save(task);
                })
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public void deleteTask(Long id) {
        taskRepo.deleteById(id);
    }
}
