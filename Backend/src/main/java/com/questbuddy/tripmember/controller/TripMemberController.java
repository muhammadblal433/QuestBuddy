package com.questbuddy.tripmember.controller;

import com.questbuddy.tripmember.dto.InviteDTO;
import com.questbuddy.tripmember.dto.UpdateStatusDTO;
import com.questbuddy.tripmember.dto.UserSummaryDTO;
import com.questbuddy.tripmember.service.TripMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RequestMapping("/api/v12/trips/{tripId}/members")
@Tag(
        name = "Trip Members",
        description = "Manage trip membership: list members, invite, approve/decline invites, and remove/leave trips."
)
public class TripMemberController {

    private final TripMembershipService svc;

    public TripMemberController(TripMembershipService svc) {
        this.svc = svc;
    }

    /** Invitee approves their pending invite. Body must be { "status": "ACCEPTED" }. */
    @PutMapping("/approve")
    @Operation(
            summary = "Approve a trip invitation",
            description = "The invitee approves their pending invite to a trip. "
                    + "The request body must be { \"status\": \"ACCEPTED\" }."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Invite approved successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status was not ACCEPTED",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not the invitee or not allowed to approve this invite",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or invite not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> approve(
            @Parameter(
                    description = "ID of the user approving the invite",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip the user is joining",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Valid @RequestBody UpdateStatusDTO body) {
        if (!"ACCEPTED".equals(body.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be ACCEPTED");
        }
        svc.approve(me, tripId);
        return ResponseEntity.noContent().build();
    }

    /** Invitee declines their pending invite. Body must be { "status": "DECLINED" }. */
    @PutMapping("/decline")
    @Operation(
            summary = "Decline a trip invitation",
            description = "The invitee declines their pending invite to a trip. "
                    + "The request body must be { \"status\": \"DECLINED\" }."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Invite declined successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status was not DECLINED",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not the invitee or not allowed to decline this invite",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or invite not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> decline(
            @Parameter(
                    description = "ID of the user declining the invite",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip the user is declining",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Valid @RequestBody UpdateStatusDTO body) {
        if (!"DECLINED".equals(body.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be DECLINED");
        }
        svc.decline(me, tripId);
        return ResponseEntity.noContent().build();
    }

    /** Owner invites a user to the trip. */
    @PostMapping("/invite")
    @Operation(
            summary = "Invite a user to a trip",
            description = "Trip owner invites another user to join the trip. "
                    + "The body contains the userId of the invitee."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Invite created successfully (no body)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not the trip owner",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or target user not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> invite(
            @Parameter(
                    description = "ID of the user sending the invite (must be trip owner)",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip for which the invite is being sent",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Valid @RequestBody InviteDTO body) {
        svc.invite(me, tripId, body.userId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** List accepted members (requester must be a member). */
    @GetMapping
    @Operation(
            summary = "List accepted members of a trip",
            description = "Returns the list of accepted members for a trip. "
                    + "The caller must be a member of the trip."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Members returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserSummaryDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not a member of this trip",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content
            )
    })
    public List<UserSummaryDTO> list(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip whose members are being listed",
                    example = "10"
            )
            @PathVariable Long tripId) {
        return svc.listAccepted(me, tripId).stream()
                .map(UserSummaryDTO::from)
                .toList();
    }

    /** Remove a member. Owner can remove anyone; a user can remove themself (leave). */
    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Remove a member from a trip",
            description = "Trip owner can remove any member; a user can remove themself (leave the trip)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Member removed successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not allowed to remove this member",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or user not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> remove(
            @Parameter(
                    description = "ID of the user making the request",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip",
                    example = "10"
            )
            @PathVariable Long tripId,
            @Parameter(
                    description = "ID of the user to remove from the trip",
                    example = "20"
            )
            @PathVariable Long userId) {
        svc.remove(me, tripId, userId);
        return ResponseEntity.noContent().build();
    }

    /** Convenience: leave the trip (same as DELETE /{me}). */
    @DeleteMapping("/me")
    @Operation(
            summary = "Leave a trip",
            description = "Convenience endpoint for the current user to leave the trip "
                    + "(equivalent to DELETE /{userId} with userId = X-User-Id)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User left the trip successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trip or user not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> leave(
            @Parameter(
                    description = "ID of the user leaving the trip",
                    example = "5"
            )
            @RequestHeader("X-User-Id") Long me,
            @Parameter(
                    description = "ID of the trip the user is leaving",
                    example = "10"
            )
            @PathVariable Long tripId) {
        svc.remove(me, tripId, me);
        return ResponseEntity.noContent().build();
    }
}
