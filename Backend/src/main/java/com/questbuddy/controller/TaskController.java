package com.questbuddy.controller;

import com.questbuddy.model.Task;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import com.questbuddy.service.TaskService;
import jakarta.persistence.Converts;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v3/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepo; // use repo directly to resolve userId

    public TaskController(TaskService taskService, UserRepository userRepo) {
        this.taskService = taskService;
        this.userRepo = userRepo;
    }

    // DTOs
    // Request body for create/update operations (flat userId; not nested)
    public record TaskReq(Long userId, String title, String description, String status, java.time.LocalDate dueDate) {}

    // Minimal user info included on task responses (no secrets)
    public record UserLite(Long id, String email, String username, String firstName, String lastName) {}

    // Outbound shape returned to clients
    public record TaskRes(Long taskId, UserLite user, String title, String description, String status, java.time.LocalDate dueDate) {}

    // Maps a Task entity to its response DTO (User goes to UserLite)
    private TaskRes toRes(com.questbuddy.model.Task t) {
        var u = t.getUser();
        var owner = (u == null) ? null : new UserLite(u.getId(), u.getEmail(), u.getUsername(), u.getFirstName(), u.getLastName());
        return new TaskRes(t.getTaskId(), owner, t.getTitle(), t.getDescription(), t.getStatus(), t.getDueDate());
    }

    private User requireUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    // CREATE TASK for the given user. If status is missing/blank, defaults to "Pending"
    // The errors that can occur are 400 if required fields are missing (userId, title), 404 if the userId does not exist, Returns: 201 + TaskRes
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createTask(@RequestBody TaskReq body) {
        if (body == null || body.userId() == null || body.title() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "missing_fields"));
        }

        var user = userRepo.findById(body.userId())
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found"));

        var task = new com.questbuddy.model.Task(
                user,
                body.title(),
                body.description(),
                (body.status() == null || body.status().isBlank()) ? "Pending" : body.status(),
                body.dueDate()
        );
        var saved = taskService.createTask(task);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(toRes(saved));
    }

    // READ ALL TASKS
    @GetMapping(produces = "application/json")
    public ResponseEntity<java.util.List<TaskRes>> getAllTasks() {
        var out = taskService.getAllTasks().stream().map(this::toRes).toList();
        return ResponseEntity.ok(out);
    }

    // READ BY TASK ID
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(this::toRes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // READ BY USER
    @GetMapping(value = "/user/{userId}", produces = "application/json")
    public ResponseEntity<java.util.List<TaskRes>> getTasksByUser(@PathVariable Long userId) {
        var out = taskService.getTasksByUserId(userId).stream().map(this::toRes).toList();
        return ResponseEntity.ok(out);
    }

    // UPDATE (full PUT, null-safe via service)
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskReq body) {
        var patch = new com.questbuddy.model.Task();
        patch.setTitle(body.title());
        patch.setDescription(body.description());
        patch.setStatus(body.status());
        patch.setDueDate(body.dueDate());

        var saved = taskService.updateTask(id, patch);
        return ResponseEntity.ok(toRes(saved));
    }

    // DELETE TASK
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }

    // HEALTH CHECK TO MAKE SURE IF THIS FILE IS EVEN BEING READ
    @GetMapping("/ping")
    public String ping() {
        return "TaskController DTO v3";
    }

    // Converts thrown NoSuchElementException into a 404 JSON body
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> onNotFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(Map.of("error", "not_found", "message", e.getMessage()));
    }

    // Converts IllegalArgumentException into a 400 JSON body
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> onBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "bad_request", "message", e.getMessage()));
    }
}
