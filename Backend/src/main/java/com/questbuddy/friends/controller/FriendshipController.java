package com.questbuddy.friends.controller;

import com.questbuddy.friends.dto.ApiMessage;
import com.questbuddy.friends.dto.FriendDTO;
import com.questbuddy.friends.dto.FriendSuggestionDTO;
import com.questbuddy.friends.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v8")
public class FriendshipController {

    private final FriendshipService service;

    public FriendshipController(FriendshipService service) {
        this.service = service;
    }

    // Health check
    @GetMapping("/friends/ping")
    public String ping() {
        return "FriendshipController v8 is alive!";
    }

    // Requests (path-param user id)
    @PostMapping("/users/{meId}/friends/requests/{targetId}")
    public ResponseEntity<ApiMessage> send(@PathVariable Long meId, @PathVariable Long targetId) {
        service.sendRequest(meId, targetId);
        return ResponseEntity.ok(new ApiMessage("Friend request sent"));
    }

    @PostMapping("/users/{meId}/friends/requests/{requesterId}/accept")
    public ResponseEntity<ApiMessage> accept(@PathVariable Long meId, @PathVariable Long requesterId) {
        service.accept(meId, requesterId);
        return ResponseEntity.ok(new ApiMessage("Friend request accepted"));
    }

    @PostMapping("/users/{meId}/friends/requests/{requesterId}/reject")
    public ResponseEntity<ApiMessage> reject(@PathVariable Long meId, @PathVariable Long requesterId) {
        service.reject(meId, requesterId);
        return ResponseEntity.ok(new ApiMessage("Friend request rejected"));
    }

    // Lists
    @GetMapping("/users/{meId}/friends")
    public List<FriendDTO> list(@PathVariable Long meId) {
        return service.listFriends(meId);
    }

    @GetMapping("/users/{meId}/friends/requests/incoming")
    public List<FriendDTO> incoming(@PathVariable Long meId) {
        return service.incomingRequests(meId);
    }

    @GetMapping("/users/{meId}/friends/requests/outgoing")
    public List<FriendDTO> outgoing(@PathVariable Long meId) {
        return service.outgoingRequests(meId);
    }

    // Block / Unblock / Unfriend
    @PostMapping("/users/{meId}/friends/{otherId}/block")
    public ResponseEntity<ApiMessage> block(@PathVariable Long meId, @PathVariable Long otherId) {
        service.block(meId, otherId);
        return ResponseEntity.ok(new ApiMessage("User blocked"));
    }

    @DeleteMapping("/users/{meId}/friends/{otherId}/block")
    public ResponseEntity<ApiMessage> unblock(@PathVariable Long meId, @PathVariable Long otherId) {
        service.unblock(meId, otherId);
        return ResponseEntity.ok(new ApiMessage("User unblocked"));
    }

    @DeleteMapping("/users/{meId}/friends/{otherId}")
    public ResponseEntity<ApiMessage> unfriend(@PathVariable Long meId, @PathVariable Long otherId) {
        service.unfriend(meId, otherId);
        return ResponseEntity.ok(new ApiMessage("User unfriended"));
    }

    // Suggestions
    @GetMapping("/users/{meId}/friends/suggestions")
    public List<FriendSuggestionDTO> suggestions(@PathVariable Long meId,
                                                 @RequestParam(defaultValue = "20") int limit) {
        return service.suggestions(meId, limit);
    }
}