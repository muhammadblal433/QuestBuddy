package com.questbuddy.friends.controller;

import com.questbuddy.friends.dto.FriendDTO;
import com.questbuddy.friends.dto.FriendSuggestionDTO;
import com.questbuddy.friends.service.FriendshipService;
import org.springframework.http.HttpStatus;
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

    // Requests (no header; user id in path)
    @PostMapping("/users/{meId}/friends/requests/{targetId}")
    @ResponseStatus(HttpStatus.OK)
    public void send(@PathVariable Long meId, @PathVariable Long targetId) {
        service.sendRequest(meId, targetId);
    }

    @PostMapping("/users/{meId}/friends/requests/{requesterId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public void accept(@PathVariable Long meId, @PathVariable Long requesterId) {
        service.accept(meId, requesterId);
    }

    @PostMapping("/users/{meId}/friends/requests/{requesterId}/reject")
    @ResponseStatus(HttpStatus.OK)
    public void reject(@PathVariable Long meId, @PathVariable Long requesterId) {
        service.reject(meId, requesterId);
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
    @ResponseStatus(HttpStatus.OK)
    public void block(@PathVariable Long meId, @PathVariable Long otherId) {
        service.block(meId, otherId);
    }

    @DeleteMapping("/users/{meId}/friends/{otherId}/block")
    @ResponseStatus(HttpStatus.OK)
    public void unblock(@PathVariable Long meId, @PathVariable Long otherId) {
        service.unblock(meId, otherId);
    }

    @DeleteMapping("/users/{meId}/friends/{otherId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfriend(@PathVariable Long meId, @PathVariable Long otherId) {
        service.unfriend(meId, otherId);
    }

    // Suggestions
    @GetMapping("/users/{meId}/friends/suggestions")
    public List<FriendSuggestionDTO> suggestions(@PathVariable Long meId,
                                                 @RequestParam(defaultValue = "20") int limit) {
        return service.suggestions(meId, limit);
    }
}