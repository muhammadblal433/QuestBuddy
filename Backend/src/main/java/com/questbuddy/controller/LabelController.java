package com.questbuddy.controller;

import com.questbuddy.model.Label;
import com.questbuddy.model.User;
import com.questbuddy.repository.LabelRepository;
import com.questbuddy.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.List;

@RestController
@RequestMapping("/api/v5/labels")
public class LabelController {

    private final LabelRepository labels;
    private final UserRepository users;

    public LabelController(LabelRepository labels, UserRepository users) {
        this.labels = labels;
        this.users = users;
    }

    // DTOs
    public record CreateLabelReq(Long userId, String name, String color) {}
    public record LabelRes(Long id, Long userId, String name, String color) {}

    private LabelRes toRes(Label l) {
        return new LabelRes(l.getId(), l.getUser().getId(), l.getName(), l.getColor());
    }

    private User requireUser(Long id) {
        return users.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    // CREATE
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> create(@RequestBody CreateLabelReq body) {
        if (body == null || body.userId() == null || body.name() == null || body.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }

        var user = requireUser(body.userId());

        // prevent duplicate label names per user
        if (labels.existsByUser_IdAndNameIgnoreCase(user.getId(), body.name().trim())) {
            return ResponseEntity.status(409).body(Map.of("error", "label_exists"));
        }

        var l = new Label();
        l.setUser(user);
        l.setName(body.name().trim());
        l.setColor(body.color());
        var saved = labels.save(l);

        return ResponseEntity.status(HttpStatus.CREATED).body(toRes(saved));
    }

    // LIST by user
    @GetMapping(value = "/user/{userId}", produces = "application/json")
    public ResponseEntity<List<LabelRes>> listByUser(@PathVariable Long userId) {
        // optional: 404 if user missing
        requireUser(userId);
        var out = labels.findByUser_Id(userId).stream().map(this::toRes).toList();
        return ResponseEntity.ok(out);
    }

    // DELETE by id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!labels.existsById(id)) return ResponseEntity.notFound().build();
        labels.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Label deleted"));
    }

    // Health check just to make sure file is actually being read
    @GetMapping("/ping")
    public String ping() { return "LabelController is alive!"; }

    // Errors that could occur
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(Map.of("error", "not_found", "message", e.getMessage()));
    }
}
