package com.questbuddy.friends.controller;

import com.questbuddy.friends.dto.FriendDTO;
import com.questbuddy.friends.dto.FriendSuggestionDTO;
import com.questbuddy.friends.service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v8/friends")
public class FriendshipController {

    private final FriendshipService service;

    public FriendshipController(FriendshipService service) {
        this.service = service;
    }

    @PostMapping("/requests/{targetId}")
    @ResponseStatus(HttpStatus.OK)
    public void send(@RequestHeader("X-User-Id") Long userId, @PathVariable Long targetId) {
        service.sendRequest(userId, targetId);
    }

    @PostMapping("/requests/{requesterId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public void accept(@RequestHeader("X-User-Id") Long userId, @PathVariable Long requesterId) {
        service.accept(userId, requesterId);
    }

    @PostMapping("/requests/{requesterId}/reject")
    @ResponseStatus(HttpStatus.OK)
    public void reject(@RequestHeader("X-User-Id") Long userId, @PathVariable Long requesterId) {
        service.reject(userId, requesterId);
    }

    @GetMapping
    public List<FriendDTO> list(@RequestHeader("X-User-Id") Long userId) {
        return service.listFriends(userId);
    }

    @GetMapping("/requests/incoming")
    public List<FriendDTO> incoming(@RequestHeader("X-User-Id") Long userId) {
        return service.incomingRequests(userId);
    }

    @GetMapping("/requests/outgoing")
    public List<FriendDTO> outgoing(@RequestHeader("X-User-Id") Long userId) {
        return service.outgoingRequests(userId);
    }

    // Block/Unfriend
    @PostMapping("/{otherId}/block")
    @ResponseStatus(HttpStatus.OK)
    public void block(@RequestHeader("X-User-Id") Long userId, @PathVariable Long otherId) {
        service.block(userId, otherId);
    }

    @DeleteMapping("/{otherId}/block")
    @ResponseStatus(HttpStatus.OK)
    public void unblock(@RequestHeader("X-User-Id") Long userId, @PathVariable Long otherId) {
        service.unblock(userId, otherId);
    }

    @DeleteMapping("/{otherId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfriend(@RequestHeader("X-User-Id") Long userId, @PathVariable Long otherId) {
        service.unfriend(userId, otherId);
    }

    // Suggestions
    @GetMapping("/suggestions")
    public List<FriendSuggestionDTO> suggestions(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestParam(defaultValue = "20") int limit) {
        return service.suggestions(userId, limit);
    }
}