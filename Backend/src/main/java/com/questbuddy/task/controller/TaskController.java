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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v3/tasks")
@Tag(
        name = "Tasks",
        description = "Endpoints for creating, reading, updating, and deleting tasks."
)
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

    // UPDATE (full PUT, null-safe via service)
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Update an existing task",
            description = "Performs a full update of an existing task by ID. "
                    + "Fields that are null are handled in the service layer."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskRes.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid update payload",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<?> updateTask(
            @Parameter(
                    description = "ID of the task to update",
                    example = "10"
            )
            @PathVariable Long id,
            @RequestBody TaskReq body) {
        var patch = new com.questbuddy.model.Task();
        patch.setTitle(body.title());
        patch.setDescription(body.description());
        patch.setStatus(body.status());
        patch.setDueDate(body.dueDate());

        var saved = taskService.updateTask(id, patch);
        return ResponseEntity.ok(toRes(saved));
    }

    // CREATE TASK for the given user. If status is missing/blank, defaults to "Pending"
    // The errors that can occur are 400 if required fields are missing (userId, title), 404 if the userId does not exist, Returns: 201 + TaskRes
    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Create a new task",
            description = "Creates a new task for the given user. If status is missing or blank, it defaults to 'Pending'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskRes.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing required fields (userId or title)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found for the given userId",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            )
    })
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
    @Operation(
            summary = "List all tasks",
            description = "Returns a list of all tasks in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskRes.class))
            )
    })
    public ResponseEntity<java.util.List<TaskRes>> getAllTasks() {
        var out = taskService.getAllTasks().stream().map(this::toRes).toList();
        return ResponseEntity.ok(out);
    }

    // READ BY TASK ID
    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(
            summary = "Get task by ID",
            description = "Returns a single task by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskRes.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content
            )
    })
    public ResponseEntity<?> getTaskById(
            @Parameter(
                    description = "ID of the task to retrieve",
                    example = "10"
            )
            @PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(this::toRes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // READ BY USER
    @GetMapping(value = "/user/{userId}", produces = "application/json")
    @Operation(
            summary = "List tasks for a specific user",
            description = "Returns all tasks that belong to the given userId."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskRes.class))
            )
    })
    public ResponseEntity<java.util.List<TaskRes>> getTasksByUser(
            @Parameter(
                    description = "ID of the user whose tasks are being listed",
                    example = "5"
            )
            @PathVariable Long userId) {
        var out = taskService.getTasksByUserId(userId).stream().map(this::toRes).toList();
        return ResponseEntity.ok(out);
    }

    // HEALTH CHECK TO MAKE SURE IF THIS FILE IS EVEN BEING READ
    @GetMapping("/ping")
    @Operation(
            summary = "Task service health check",
            description = "Simple endpoint to verify that TaskController is alive and reachable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task controller is alive"
            )
    })
    public String ping() {
        return "TaskController DTO v3";
    }

    // DELETE TASK
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a task",
            description = "Deletes a task by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task deleted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<?> deleteTask(
            @Parameter(
                    description = "ID of the task to delete",
                    example = "10"
            )
            @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
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
