package com.questbuddy.service;

import com.questbuddy.task.model.Task;
import com.questbuddy.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.questbuddy.notification.NotificationService;
import com.questbuddy.notification.NotificationType;
import com.questbuddy.notification.dto.NotificationCreateDTO;

@Service
public class TaskServiceImplemented implements TaskService {

    private final TaskRepository taskRepo;

    private final NotificationService notificationService;

    public TaskServiceImplemented(TaskRepository taskRepo,
                                  NotificationService notificationService) {
        this.taskRepo = taskRepo;
        this.notificationService = notificationService;
    }

    @Override
    public Task createTask(Task task) {
        Task saved = taskRepo.save(task);

        if (saved.getUser() != null && saved.getUser().getId() != null) {
            notificationService.create(new NotificationCreateDTO(
                    saved.getUser().getId(),
                    "Task Assigned",
                    "You’ve been assigned: " + saved.getTitle(),
                    NotificationType.TASK,
                    null, null, saved.getTaskId()
            ));
        }
        return saved;
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
                    // keep same user and setUser only if there is reassignment needed
                    Task saved = taskRepo.save(task);

                    if (saved.getUser() != null && saved.getUser().getId() != null) {
                        notificationService.create(new NotificationCreateDTO(
                                saved.getUser().getId(),
                                "Task Updated",
                                "Updated: " + saved.getTitle(),
                                NotificationType.REMINDER,
                                null, null, saved.getTaskId()
                        ));
                    }
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public void deleteTask(Long id) {
        // Optional: notify owner here if you load the task first.
        taskRepo.deleteById(id);
    }
}