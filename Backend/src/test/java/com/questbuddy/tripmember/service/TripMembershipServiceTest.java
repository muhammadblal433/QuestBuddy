package com.questbuddy.tripmember.service;

import com.questbuddy.notification.NotificationService;
import com.questbuddy.notification.NotificationType;
import com.questbuddy.notification.dto.NotificationCreateDTO;
import com.questbuddy.trip.Trip;
import com.questbuddy.trip.TripRepository;
import com.questbuddy.tripmember.model.TripMember;
import com.questbuddy.tripmember.repository.TripMemberRepository;
import com.questbuddy.tripmember.security.TripMembershipGate;
import com.questbuddy.user.model.User;
import com.questbuddy.user.repository.UserRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TripMembershipServiceTest {

    @Mock private TripMemberRepository members;
    @Mock private TripRepository trips;
    @Mock private UserRepository users;
    @Mock private TripMembershipGate gate;
    @Mock private NotificationService notifications;

    @InjectMocks
    private TripMembershipService svc;

    private User owner;
    private User userA;
    private User userB;
    private Trip trip;

    @Before
    public void setup() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setFirstName("Owner");

        userA = new User();
        userA.setId(2L);
        userA.setUsername("userA");

        userB = new User();
        userB.setId(3L);
        userB.setUsername("userB");

        trip = new Trip();
        trip.setOwnerId(1L);
        trip.setName("Japan Trip");
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(3));

        when(users.findById(1L)).thenReturn(Optional.of(owner));
        when(users.findById(2L)).thenReturn(Optional.of(userA));
        when(users.findById(3L)).thenReturn(Optional.of(userB));
    }

    @Test(expected = ResponseStatusException.class)
    public void testInviteCallerNotOwner403() {
        when(gate.isOwner(10L, 2L)).thenReturn(false);
        svc.invite(2L, 10L, 3L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testInviteTripNotFound() {
        when(gate.isOwner(10L, 1L)).thenReturn(true);
        when(trips.findById(10L)).thenReturn(Optional.empty());
        svc.invite(1L, 10L, 3L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testInviteInviteeNotFound() {
        when(gate.isOwner(10L, 1L)).thenReturn(true);
        when(trips.findById(10L)).thenReturn(Optional.of(trip));
        when(users.findById(3L)).thenReturn(Optional.empty());
        svc.invite(1L, 10L, 3L);
    }

    @Test
    public void testInviteNoOpIfAlreadyExists() {
        when(gate.isOwner(10L, 1L)).thenReturn(true);
        when(trips.findById(10L)).thenReturn(Optional.of(trip));
        when(members.findByTrip_IdAndUser_Id(10L, 3L))
                .thenReturn(Optional.of(new TripMember()));

        svc.invite(1L, 10L, 3L);

        verify(members, never()).save(any());
        verify(notifications, never()).create(any());
    }

    @Test
    public void testInviteSuccess() {
        when(gate.isOwner(10L, 1L)).thenReturn(true);
        when(trips.findById(10L)).thenReturn(Optional.of(trip));
        when(members.findByTrip_IdAndUser_Id(10L, 3L))
                .thenReturn(Optional.empty());

        svc.invite(1L, 10L, 3L);

        verify(members).save(any(TripMember.class));
        verify(notifications).create(any(NotificationCreateDTO.class));
    }

    @Test(expected = ResponseStatusException.class)
    public void testApproveNotFound404() {
        when(members.findByTrip_IdAndUser_Id(10L, 2L))
                .thenReturn(Optional.empty());
        svc.approve(2L, 10L);
    }

    @Test
    public void testApproveAlreadyAcceptedNoOp() {
        TripMember m = new TripMember();
        m.setUser(userA);
        m.setInvitedBy(owner);
        m.setTrip(trip);
        m.setStatus(TripMember.Status.ACCEPTED);

        when(members.findByTrip_IdAndUser_Id(10L, 2L))
                .thenReturn(Optional.of(m));

        svc.approve(2L, 10L);

        verify(members, never()).save(any());
        verify(notifications, never()).create(any());
    }

    @Test
    public void testApproveSuccess() {
        TripMember m = new TripMember();
        m.setUser(userA);
        m.setInvitedBy(owner);
        m.setTrip(trip);
        m.setStatus(TripMember.Status.PENDING);

        when(members.findByTrip_IdAndUser_Id(10L, 2L))
                .thenReturn(Optional.of(m));

        svc.approve(2L, 10L);

        verify(members).save(m);
        verify(notifications).create(any(NotificationCreateDTO.class));
    }

    @Test(expected = ResponseStatusException.class)
    public void testDeclineNotFound404() {
        when(members.findByTrip_IdAndUser_Id(10L, 2L))
                .thenReturn(Optional.empty());
        svc.decline(2L, 10L);
    }

    @Test
    public void testDeclineAlreadyAcceptedNoOp() {
        TripMember m = new TripMember();
        m.setStatus(TripMember.Status.ACCEPTED);

        when(members.findByTrip_IdAndUser_Id(10L, 2L))
                .thenReturn(Optional.of(m));

        svc.decline(2L, 10L);

        verify(members, never()).delete(any());
        verify(notifications, never()).create(any());
    }

    @Test
    public void testDeclinePendingDeletes() {
        TripMember m = new TripMember();
        m.setUser(userA);
        m.setInvitedBy(owner);
        m.setTrip(trip);
        m.setStatus(TripMember.Status.PENDING);

        when(members.findByTrip_IdAndUser_Id(10L, 2L))
                .thenReturn(Optional.of(m));

        svc.decline(2L, 10L);

        verify(members).delete(m);
        verify(notifications).create(any(NotificationCreateDTO.class));
    }

    @Test(expected = ResponseStatusException.class)
    public void testRemoveForbidden() {
        when(gate.isOwner(10L, 2L)).thenReturn(false);
        svc.remove(2L, 10L, 3L);
    }

    @Test
    public void testRemoveSelf() {
        when(gate.isOwner(10L, 2L)).thenReturn(false);

        svc.remove(2L, 10L, 2L);

        verify(members).deleteByTrip_IdAndUser_Id(10L, 2L);
    }

    @Test
    public void testRemoveByOwner() {
        when(gate.isOwner(10L, 1L)).thenReturn(true);

        svc.remove(1L, 10L, 3L);

        verify(members).deleteByTrip_IdAndUser_Id(10L, 3L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testListAcceptedForbidden() {
        when(gate.isMember(10L, 2L)).thenReturn(false);
        svc.listAccepted(2L, 10L);
    }

    @Test
    public void testListAcceptedSuccess() {
        when(gate.isMember(10L, 2L)).thenReturn(true);

        TripMember m1 = new TripMember();
        m1.setUser(userA);

        TripMember m2 = new TripMember();
        m2.setUser(userB);

        when(members.findAllByTrip_IdAndStatusOrderByUser_IdAsc(10L,
                TripMember.Status.ACCEPTED))
                .thenReturn(List.of(m1, m2));

        List<User> result = svc.listAccepted(2L, 10L);

        assertEquals(2, result.size());
        assertEquals(userA, result.get(0));
    }

    @Test
    public void testSeedOwnerAlreadyExistsNoOp() {
        when(members.findByTrip_IdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(new TripMember()));

        svc.seedOwner(1L, 10L);

        verify(members, never()).save(any());
    }

    @Test(expected = ResponseStatusException.class)
    public void testSeedOwnerTripNotFound() {
        when(members.findByTrip_IdAndUser_Id(10L, 1L))
                .thenReturn(Optional.empty());
        when(trips.findById(10L)).thenReturn(Optional.empty());

        svc.seedOwner(1L, 10L);
    }

    @Test
    public void testSeedOwnerSuccess() {
        when(members.findByTrip_IdAndUser_Id(10L, 1L))
                .thenReturn(Optional.empty());
        when(trips.findById(10L)).thenReturn(Optional.of(trip));

        svc.seedOwner(1L, 10L);

        verify(members).save(any(TripMember.class));
    }

    @Test(expected = ResponseStatusException.class)
    public void testListPendingForbidden() {
        svc.listPendingInvitesForUser(1L, 2L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testListPendingUserNotFound() {
        when(users.findById(2L)).thenReturn(Optional.empty());
        svc.listPendingInvitesForUser(2L, 2L);
    }

    @Test
    public void testListPendingSuccess() {
        TripMember m = new TripMember();
        m.setUser(userA);

        when(users.findById(2L)).thenReturn(Optional.of(userA));
        when(members.findAllByUser_IdAndStatusOrderByTrip_IdAsc(2L,
                TripMember.Status.PENDING))
                .thenReturn(List.of(m));

        List<TripMember> result = svc.listPendingInvitesForUser(2L, 2L);

        assertEquals(1, result.size());
    }

    @Test(expected = ResponseStatusException.class)
    public void testEnsureMemberForbidden() {
        when(gate.isMember(10L, 2L)).thenReturn(false);
        svc.ensureMember(2L, 10L);
    }

    @Test
    public void testEnsureMemberSuccess() {
        when(gate.isMember(10L, 2L)).thenReturn(true);
        svc.ensureMember(2L, 10L);
    }

    @Test
    public void testIsOwnerDelegatesToGate() {
        when(gate.isOwner(10L, 1L)).thenReturn(true);

        assertTrue(svc.isOwner(1L, 10L));
    }
}

