package com.questbuddy.tripmember.controller;

import com.questbuddy.tripmember.dto.PendingInviteDTO;
import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.tripmember.service.TripMembershipService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@Validated
@RequestMapping("/api/v12/users/{userId}/trip-invites")
@Tag(
        name = "Trip Invites",
        description = "List pending trip invitations for users."
)
public class TripInviteController {

    private final TripMembershipService svc;

    public TripInviteController(TripMembershipService svc) {
        this.svc = svc;
    }

    /** List all incoming pending trip invites for a user. */
    @GetMapping("/pending")
    @Operation(
            summary = "List incoming pending invites for a user",
            description = "Returns all trips that have a pending invite for the given user. "
                    + "The caller must be the same user (X-User-Id header)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pending invites returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PendingInviteDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not allowed to view these invites",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public List<PendingInviteDTO> listPendingForUser(
            @Parameter(
                    description = "ID of the user making the request (must match {userId})",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the user whose incoming invites are being listed",
                    example = "5"
            )
            @PathVariable Long userId
    ) {
        List<TripMember> pending = svc.listPendingInvitesForUser(me, userId);
        return pending.stream()
                .map(PendingInviteDTO::from)
                .toList();
    }
}
