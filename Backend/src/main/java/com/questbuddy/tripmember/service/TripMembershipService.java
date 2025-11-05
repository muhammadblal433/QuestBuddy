package com.questbuddy.tripmember.service;

import com.questbuddy.repository.UserRepository;
import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.tripmember.model.TripMember.Role;
import com.questbuddy.tripmember.model.TripMember.Status;
import com.questbuddy.tripmember.repository.TripMemberRepository;
import com.questbuddy.tripmember.security.TripMembershipGate;

import com.questbuddy.model.User;
import com.questbuddy.trip.Trip;


import com.questbuddy.repository.UserRepository;
import com.questbuddy.trip.TripRepository;

import com.questbuddy.notification.NotificationService;
import com.questbuddy.notification.NotificationType;
import com.questbuddy.notification.dto.NotificationCreateDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TripMembershipService {

    private final TripMemberRepository members;
    private final TripRepository trips;
    private final UserRepository users;
    private final TripMembershipGate gate;
    private final NotificationService notifications;

    public TripMembershipService(TripMemberRepository members,
                                 TripRepository trips,
                                 UserRepository users,
                                 TripMembershipGate gate,
                                 NotificationService notifications) {
        this.members = members;
        this.trips = trips;
        this.users = users;
        this.gate = gate;
        this.notifications = notifications;
    }

    /** Owner invites a user to a trip (idempotent). */
    @Transactional
    public void invite(Long inviterId, Long tripId, Long userId) {
        // must be owner
        if (!gate.isOwner(tripId, inviterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not_owner");
        }

        Trip trip = trips.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "trip_not_found"));
        User invitee = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found"));
        User inviter = users.findById(inviterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "inviter_not_found"));

        // Idempotent: do nothing if there's already a row (pending or accepted)
        if (members.findByTrip_IdAndUser_Id(tripId, userId).isPresent()) return;

        TripMember m = new TripMember();
        m.setTrip(trip);
        m.setUser(invitee);
        m.setRole(Role.MEMBER);
        m.setStatus(Status.PENDING);
        m.setInvitedBy(inviter);
        members.save(m);

        // Notification
        safelyNotify(invitee.getId(),
                "Trip invite",
                display(inviter) + " invited you to Trip #" + trip.getId(),
                NotificationType.INVITE,
                null, trip.getId(), null);
    }

    /** Invitee approves (accepts) their own pending invite. */
    @Transactional
    public void approve(Long approverId, Long tripId) {
        TripMember m = members.findByTrip_IdAndUser_Id(tripId, approverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "membership_not_found"));

        if (m.getStatus() == Status.ACCEPTED) return; // do nth as already accepted invite

        m.setStatus(Status.ACCEPTED);
        members.save(m);

        // notification
        User approver = m.getUser();
        User inviter  = m.getInvitedBy();
        Trip trip     = m.getTrip();
        safelyNotify(inviter.getId(),
                "Invite accepted",
                display(approver) + " joined Trip #" + trip.getId(),
                NotificationType.APPROVAL,
                null, trip.getId(), null);
    }

    // Invitee declines (rejects) their pending invite
    @Transactional
    public void decline(Long userId, Long tripId) {
        TripMember m = members.findByTrip_IdAndUser_Id(tripId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "membership_not_found"));

        if (m.getStatus() == Status.ACCEPTED) {
            // If already accepted, treat as no-op or enforce a different flow (leave/remove)
            return;
        }

        User invitee = m.getUser();
        User inviter = m.getInvitedBy();
        Trip trip    = m.getTrip();

        // Remove the pending invite row on decline
        members.delete(m);

        // Notify the owner/inviter that the invitee declined
        safelyNotify(inviter.getId(),
                "Invite declined",
                display(invitee) + " declined Trip #" + trip.getId(),
                // Using REMINDER for "declined" as discussed (no enum change required)
                NotificationType.REMINDER,
                null, trip.getId(), null);
    }

    /**
     * Remove a member (owner can remove anyone; a user can remove themselves = leave).
     * NOTE: removing a non-existent membership --> no operation due to @Transactional.
     */
    @Transactional
    public void remove(Long actorId, Long tripId, Long targetUserId) {
        boolean owner = gate.isOwner(tripId, actorId);
        boolean self = actorId != null && actorId.equals(targetUserId);
        if (!(owner || self)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        members.deleteByTrip_IdAndUser_Id(tripId, targetUserId);
    }

    /** List accepted members (requester must be an accepted member). */
    @Transactional(readOnly = true)
    public List<User> listAccepted(Long requesterId, Long tripId) {
        if (!gate.isMember(tripId, requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not_member");
        }
        return members.findAllByTrip_IdAndStatusOrderByUser_IdAsc(tripId, Status.ACCEPTED)
                .stream()
                .map(TripMember::getUser)
                .toList();
    }

    /**
     * Seed the OWNER membership on trip creation.
     * Call this once after saving a new Trip.
     */
    @Transactional
    public void seedOwner(Long ownerId, Long tripId) {
        if (members.findByTrip_IdAndUser_Id(tripId, ownerId).isPresent()) return;

        Trip trip = trips.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "trip_not_found"));
        User owner = users.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found"));

        TripMember tm = new TripMember();
        tm.setTrip(trip);
        tm.setUser(owner);
        tm.setRole(Role.OWNER);
        tm.setStatus(Status.ACCEPTED);
        tm.setInvitedBy(owner);
        members.save(tm);
    }

    // helpers

    private static String display(User u) {
        String first = safe(u.getFirstName());
        String last  = safe(u.getLastName());
        if (first != null && last != null) return first + " " + last;
        if (first != null) return first;
        if (last  != null) return last;
        String uname = safe(u.getUsername());
        if (uname != null) return uname;
        String email = safe(u.getEmail());
        if (email != null) return email;
        return "User#" + u.getId();
    }

    // check for string
    private static String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // helper to create a notification based on an invite
    private void safelyNotify(Long recipientUserId,
                              String title,
                              String message,
                              NotificationType type,
                              Long eventId,
                              Long tripId,
                              Long taskId) {
        try {
            if (notifications != null && recipientUserId != null) {
                notifications.create(new NotificationCreateDTO(
                        recipientUserId,
                        title,
                        message,
                        type,
                        eventId,
                        tripId,
                        taskId
                ));
            }
        } catch (Exception ignored) {
            // Notifications shouldn't break core flow; swallow and continue.
        }
    }
}