package com.questbuddy.friends.controller;

import com.questbuddy.friends.dto.ApiMessage;
import com.questbuddy.friends.dto.FriendDTO;
import com.questbuddy.friends.dto.FriendSuggestionDTO;
import com.questbuddy.friends.service.FriendshipService;
import com.questbuddy.user.model.User;
import com.questbuddy.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v8")
@Tag(
        name = "Friendships",
        description = "Manage friend requests, friends lists, blocks, and suggestions using username-based endpoints."
)
public class FriendshipController {

    private final FriendshipService service;
    private final UserRepository users;

    public FriendshipController(FriendshipService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    // Username-based Requests

    @PostMapping("/users/{meUsername}/friends/requests/{targetUsername}")
    @Operation(
            summary = "Send a friend request",
            description = "Sends a friend request from one user to another, both identified by usernames."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Friend request sent",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or both users not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiMessage> send(
            @Parameter(
                    description = "Username of the user sending the friend request",
                    example = "ayaan"
            )
            @PathVariable String meUsername,
            @Parameter(
                    description = "Username of the target user",
                    example = "friend_user"
            )
            @PathVariable String targetUsername) {
        service.sendRequest(idOf(meUsername), idOf(targetUsername));
        return ResponseEntity.ok(new ApiMessage("Friend request sent"));
    }

    @PostMapping("/users/{meUsername}/friends/requests/{requesterUsername}/accept")
    @Operation(
            summary = "Accept a friend request",
            description = "Accepts an incoming friend request from requesterUsername to meUsername."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Friend request accepted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or both users or the request not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiMessage> accept(
            @Parameter(
                    description = "Username of the user accepting the request",
                    example = "ayaan"
            )
            @PathVariable String meUsername,
            @Parameter(
                    description = "Username of the user who sent the request",
                    example = "friend_user"
            )
            @PathVariable String requesterUsername) {
        service.accept(idOf(meUsername), idOf(requesterUsername));
        return ResponseEntity.ok(new ApiMessage("Friend request accepted"));
    }

    @PostMapping("/users/{meUsername}/friends/requests/{requesterUsername}/reject")
    @Operation(
            summary = "Reject a friend request",
            description = "Rejects an incoming friend request from requesterUsername to meUsername."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Friend request rejected",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or both users or the request not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiMessage> reject(
            @Parameter(
                    description = "Username of the user rejecting the request",
                    example = "ayaan"
            )
            @PathVariable String meUsername,
            @Parameter(
                    description = "Username of the user who sent the request",
                    example = "friend_user"
            )
            @PathVariable String requesterUsername) {
        service.reject(idOf(meUsername), idOf(requesterUsername));
        return ResponseEntity.ok(new ApiMessage("Friend request rejected"));
    }

    // Block / Unblock / Unfriend

    @PostMapping("/users/{meUsername}/friends/{otherUsername}/block")
    @Operation(
            summary = "Block a user",
            description = "Blocks another user, preventing further friend requests or interactions as defined by the service."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User blocked",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or both users not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiMessage> block(
            @Parameter(
                    description = "Username of the user performing the block",
                    example = "ayaan"
            )
            @PathVariable String meUsername,
            @Parameter(
                    description = "Username of the user being blocked",
                    example = "annoying_guy"
            )
            @PathVariable String otherUsername) {
        service.block(idOf(meUsername), idOf(otherUsername));
        return ResponseEntity.ok(new ApiMessage("User blocked"));
    }

    // Lists

    @GetMapping("/friends/ping")
    @Operation(
            summary = "Friendship service health check",
            description = "Simple endpoint to verify that FriendshipController v8 is alive and reachable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Controller is alive"
            )
    })
    public String ping() {
        return "FriendshipController v8 is alive again!";
    }

    @GetMapping("/users/{meUsername}/friends")
    @Operation(
            summary = "List friends",
            description = "Returns the list of friends for the given username."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Friends list returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public List<FriendDTO> list(
            @Parameter(
                    description = "Username whose friends are being listed",
                    example = "ayaan"
            )
            @PathVariable String meUsername) {
        return service.listFriends(idOf(meUsername));
    }

    @GetMapping("/users/{meUsername}/friends/requests/incoming")
    @Operation(
            summary = "List incoming friend requests",
            description = "Returns the list of incoming friend requests for the given username."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Incoming friend requests returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public List<FriendDTO> incoming(
            @Parameter(
                    description = "Username whose incoming friend requests are being listed",
                    example = "ayaan"
            )
            @PathVariable String meUsername) {
        return service.incomingRequests(idOf(meUsername));
    }

    @GetMapping("/users/{meUsername}/friends/requests/outgoing")
    @Operation(
            summary = "List outgoing friend requests",
            description = "Returns the list of outgoing friend requests from the given username."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Outgoing friend requests returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public List<FriendDTO> outgoing(
            @Parameter(
                    description = "Username whose outgoing friend requests are being listed",
                    example = "ayaan"
            )
            @PathVariable String meUsername) {
        return service.outgoingRequests(idOf(meUsername));
    }

    // Suggestions

    @GetMapping("/users/{meUsername}/friends/suggestions")
    @Operation(
            summary = "Friend suggestions",
            description = "Returns friend suggestions for the given username, limited by the 'limit' query parameter."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Friend suggestions returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendSuggestionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public List<FriendSuggestionDTO> suggestions(
            @Parameter(
                    description = "Username for which to compute friend suggestions",
                    example = "ayaan"
            )
            @PathVariable String meUsername,
            @Parameter(
                    description = "Maximum number of suggestions to return",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int limit) {
        return service.suggestions(idOf(meUsername), limit);
    }

    @DeleteMapping("/users/{meUsername}/friends/{otherUsername}/block")
    @Operation(
            summary = "Unblock a user",
            description = "Removes an existing block between meUsername and otherUsername."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User unblocked",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or both users not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiMessage> unblock(
            @Parameter(
                    description = "Username of the user performing the unblock",
                    example = "ayaan"
            )
            @PathVariable String meUsername,
            @Parameter(
                    description = "Username of the user being unblocked",
                    example = "annoying_guy"
            )
            @PathVariable String otherUsername) {
        service.unblock(idOf(meUsername), idOf(otherUsername));
        return ResponseEntity.ok(new ApiMessage("User unblocked"));
    }

    @DeleteMapping("/users/{meUsername}/friends/{otherUsername}")
    @Operation(
            summary = "Unfriend a user",
            description = "Removes an existing friendship between meUsername and otherUsername."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User unfriended",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or both users not found, or friendship does not exist",
                    content = @Content
            )
    })
    public ResponseEntity<ApiMessage> unfriend(
            @Parameter(
                    description = "Username of the user performing the unfriend",
                    example = "ayaan"
            )
            @PathVariable String meUsername,
            @Parameter(
                    description = "Username of the user being unfriended",
                    example = "ex_friend"
            )
            @PathVariable String otherUsername) {
        service.unfriend(idOf(meUsername), idOf(otherUsername));
        return ResponseEntity.ok(new ApiMessage("User unfriended"));
    }

    private Long idOf(String username) {
        User u = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found: " + username));
        return u.getId();
    }
}
