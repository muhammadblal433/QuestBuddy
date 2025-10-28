package com.questbuddy.friends.controller;

import com.questbuddy.friends.dto.ApiMessage;
import com.questbuddy.friends.dto.FriendDTO;
import com.questbuddy.friends.dto.FriendSuggestionDTO;
import com.questbuddy.friends.service.FriendshipService;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v8")
public class FriendshipController {

    private final FriendshipService service;
    private final UserRepository users;

    public FriendshipController(FriendshipService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    // Health check
    @GetMapping("/friends/ping")
    public String ping() {
        return "FriendshipController v8 is alive again!";
    }

    // Username-based Requests
    @PostMapping("/users/{meUsername}/friends/requests/{targetUsername}")
    public ResponseEntity<ApiMessage> send(@PathVariable String meUsername,
                                           @PathVariable String targetUsername) {
        service.sendRequest(idOf(meUsername), idOf(targetUsername));
        return ResponseEntity.ok(new ApiMessage("Friend request sent"));
    }

    @PostMapping("/users/{meUsername}/friends/requests/{requesterUsername}/accept")
    public ResponseEntity<ApiMessage> accept(@PathVariable String meUsername,
                                             @PathVariable String requesterUsername) {
        service.accept(idOf(meUsername), idOf(requesterUsername));
        return ResponseEntity.ok(new ApiMessage("Friend request accepted"));
    }

    @PostMapping("/users/{meUsername}/friends/requests/{requesterUsername}/reject")
    public ResponseEntity<ApiMessage> reject(@PathVariable String meUsername,
                                             @PathVariable String requesterUsername) {
        service.reject(idOf(meUsername), idOf(requesterUsername));
        return ResponseEntity.ok(new ApiMessage("Friend request rejected"));
    }

    // Lists
    @GetMapping("/users/{meUsername}/friends")
    public List<FriendDTO> list(@PathVariable String meUsername) {
        return service.listFriends(idOf(meUsername));
    }

    @GetMapping("/users/{meUsername}/friends/requests/incoming")
    public List<FriendDTO> incoming(@PathVariable String meUsername) {
        return service.incomingRequests(idOf(meUsername));
    }

    @GetMapping("/users/{meUsername}/friends/requests/outgoing")
    public List<FriendDTO> outgoing(@PathVariable String meUsername) {
        return service.outgoingRequests(idOf(meUsername));
    }

    // Block / Unblock / Unfriend
    @PostMapping("/users/{meUsername}/friends/{otherUsername}/block")
    public ResponseEntity<ApiMessage> block(@PathVariable String meUsername,
                                            @PathVariable String otherUsername) {
        service.block(idOf(meUsername), idOf(otherUsername));
        return ResponseEntity.ok(new ApiMessage("User blocked"));
    }

    @DeleteMapping("/users/{meUsername}/friends/{otherUsername}/block")
    public ResponseEntity<ApiMessage> unblock(@PathVariable String meUsername,
                                              @PathVariable String otherUsername) {
        service.unblock(idOf(meUsername), idOf(otherUsername));
        return ResponseEntity.ok(new ApiMessage("User unblocked"));
    }

    @DeleteMapping("/users/{meUsername}/friends/{otherUsername}")
    public ResponseEntity<ApiMessage> unfriend(@PathVariable String meUsername,
                                               @PathVariable String otherUsername) {
        service.unfriend(idOf(meUsername), idOf(otherUsername));
        return ResponseEntity.ok(new ApiMessage("User unfriended"));
    }

    // Suggestions
    @GetMapping("/users/{meUsername}/friends/suggestions")
    public List<FriendSuggestionDTO> suggestions(@PathVariable String meUsername,
                                                 @RequestParam(defaultValue = "20") int limit) {
        return service.suggestions(idOf(meUsername), limit);
    }

    // helper method
    private Long idOf(String username) {
        User u = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found: " + username));
        return u.getId();
    }
}