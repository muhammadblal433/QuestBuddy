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

@RestController
@Validated
@RequestMapping("/api/v12/trips/{tripId}/members")
public class TripMemberController {

    private final TripMembershipService svc;

    public TripMemberController(TripMembershipService svc) {
        this.svc = svc;
    }

    /** List accepted members (requester must be a member). */
    @GetMapping
    public List<UserSummaryDTO> list(@RequestHeader("X-User-Id") Long me,
                                     @PathVariable Long tripId) {
        return svc.listAccepted(me, tripId).stream()
                .map(UserSummaryDTO::from)
                .toList();
    }

    /** Owner invites a user to the trip. */
    @PostMapping("/invite")
    public ResponseEntity<Void> invite(@RequestHeader("X-User-Id") Long me,
                                       @PathVariable Long tripId,
                                       @Valid @RequestBody InviteDTO body) {
        svc.invite(me, tripId, body.userId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** Invitee approves their pending invite. Body must be { "status": "ACCEPTED" }. */
    @PutMapping("/approve")
    public ResponseEntity<Void> approve(@RequestHeader("X-User-Id") Long me,
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
    public ResponseEntity<Void> decline(@RequestHeader("X-User-Id") Long me,
                                        @PathVariable Long tripId,
                                        @Valid @RequestBody UpdateStatusDTO body) {
        if (!"DECLINED".equals(body.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be DECLINED");
        }
        svc.decline(me, tripId);
        return ResponseEntity.noContent().build();
    }

    /** Remove a member. Owner can remove anyone; a user can remove themself (leave). */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(@RequestHeader("X-User-Id") Long me,
                                       @PathVariable Long tripId,
                                       @PathVariable Long userId) {
        svc.remove(me, tripId, userId);
        return ResponseEntity.noContent().build();
    }

    /** Convenience: leave the trip (same as DELETE /{me}). */
    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(@RequestHeader("X-User-Id") Long me,
                                      @PathVariable Long tripId) {
        svc.remove(me, tripId, me);
        return ResponseEntity.noContent().build();
    }
}