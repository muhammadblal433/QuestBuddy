package com.questbuddy.friends.service;

import com.questbuddy.friends.dto.FriendDTO;
import com.questbuddy.friends.dto.FriendSuggestionDTO;
import com.questbuddy.friends.model.Friendship;
import com.questbuddy.friends.model.FriendshipStatus;
import com.questbuddy.friends.repository.FriendshipRepository;
import com.questbuddy.model.User;
import com.questbuddy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class FriendshipServiceImplemented implements FriendshipService {

    private final FriendshipRepository friendshipRepo;
    private final UserRepository userRepo;

    public FriendshipServiceImplemented(FriendshipRepository friendshipRepo, UserRepository userRepo) {
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void sendRequest(Long meId, Long targetUserId) {
        requireUsersExist(meId, targetUserId);
        if (Objects.equals(meId, targetUserId)) throw new ResponseStatusException(BAD_REQUEST, "Cannot friend yourself");
        if (friendshipRepo.existsBlockedEitherDirection(meId, targetUserId))
            throw new ResponseStatusException(FORBIDDEN, "One of you has blocked the other");

        var incoming = friendshipRepo.findByUser_IdAndFriend_Id(targetUserId, meId)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .orElse(null);
        if (incoming != null) {
            incoming.setStatus(FriendshipStatus.ACCEPTED);
            return;
        }

        var existing = friendshipRepo.findEitherDirection(meId, targetUserId).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == FriendshipStatus.ACCEPTED)
                throw new ResponseStatusException(CONFLICT, "Already friends");
            if (existing.getStatus() == FriendshipStatus.PENDING && Objects.equals(existing.getUser().getId(), meId))
                throw new ResponseStatusException(CONFLICT, "Request already sent");
            if (existing.getStatus() == FriendshipStatus.BLOCKED)
                throw new ResponseStatusException(FORBIDDEN, "Blocked");
        }

        Friendship f = new Friendship();
        f.setUser(getUser(meId));
        f.setFriend(getUser(targetUserId));
        f.setStatus(FriendshipStatus.PENDING);
        friendshipRepo.save(f);
    }

    @Override
    public void accept(Long meId, Long requesterId) {
        requireUsersExist(meId, requesterId);
        Friendship f = friendshipRepo.findByUser_IdAndFriend_Id(requesterId, meId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No incoming request"));
        if (f.getStatus() != FriendshipStatus.PENDING)
            throw new ResponseStatusException(CONFLICT, "Not pending");
        f.setStatus(FriendshipStatus.ACCEPTED);

        // need to do this: hook chat/DM creation after acceptance
    }

    @Override
    public void reject(Long meId, Long requesterId) {
        Friendship f = friendshipRepo.findByUser_IdAndFriend_Id(requesterId, meId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No incoming request"));
        if (f.getStatus() != FriendshipStatus.PENDING)
            throw new ResponseStatusException(CONFLICT, "Not pending");
        friendshipRepo.delete(f);
    }

    @Override
    public void unfriend(Long meId, Long otherUserId) {
        Friendship f = friendshipRepo.findEitherDirection(meId, otherUserId)
                .filter(x -> x.getStatus() == FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Not friends"));
        friendshipRepo.delete(f);
    }

    @Override
    public void block(Long meId, Long otherUserId) {
        requireUsersExist(meId, otherUserId);
        Friendship f = friendshipRepo.findEitherDirection(meId, otherUserId).orElse(null);
        if (f == null) {
            f = new Friendship();
            f.setUser(getUser(meId));
            f.setFriend(getUser(otherUserId));
        }
        f.setStatus(FriendshipStatus.BLOCKED);
        friendshipRepo.save(f);
    }

    @Override
    public void unblock(Long meId, Long otherUserId) {
        Friendship f = friendshipRepo.findEitherDirection(meId, otherUserId)
                .filter(x -> x.getStatus() == FriendshipStatus.BLOCKED)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No block exists"));
        friendshipRepo.delete(f);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendDTO> listFriends(Long meId) {
        return friendshipRepo.findAllAcceptedForUser(meId).stream()
                .map(f -> toFriendDTO(otherOf(f, meId), FriendshipStatus.ACCEPTED))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendDTO> incomingRequests(Long meId) {
        return friendshipRepo.findIncomingRequests(meId).stream()
                .map(f -> toFriendDTO(f.getUser(), f.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendDTO> outgoingRequests(Long meId) {
        return friendshipRepo.findOutgoingRequests(meId).stream()
                .map(f -> toFriendDTO(f.getFriend(), f.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendSuggestionDTO> suggestions(Long meId, int limit) {
        var myAccepted = friendshipRepo.findAllAcceptedForUser(meId);
        Set<Long> myFriendIds = myAccepted.stream().map(f -> otherOf(f, meId).getId()).collect(Collectors.toSet());

        Map<Long,Integer> counts = new HashMap<>();
        if (!myFriendIds.isEmpty()) {
            var friendsAccepted = friendshipRepo.findByUser_IdInAndStatus(myFriendIds, FriendshipStatus.ACCEPTED);
            for (Friendship f : friendsAccepted) {
                Long a = f.getUser().getId();
                Long b = f.getFriend().getId();
                if (myFriendIds.contains(a)) counts.merge(b, 1, Integer::sum);
                if (myFriendIds.contains(b)) counts.merge(a, 1, Integer::sum);
            }
        }

        Set<Long> excluded = new HashSet<>(myFriendIds);
        excluded.add(meId);
        // Exclude anyone with any relationship (pending/blocked/accepted); reuse eitherDirection by scanning? Simplify by filtering later.

        return counts.entrySet().stream()
                .sorted((x,y) -> Integer.compare(y.getValue(), x.getValue()))
                .limit(limit)
                .map(e -> {
                    User u = getUser(e.getKey());
                    return new FriendSuggestionDTO(u.getId(), displayName(u), usernameOrEmail(u), e.getValue());
                })
                .collect(Collectors.toList());
    }

    private User otherOf(Friendship f, Long meId) {
        return Objects.equals(f.getUser().getId(), meId) ? f.getFriend() : f.getUser();
    }

    private User getUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found: " + id));
    }

    private void requireUsersExist(Long... ids) {
        for (Long id : ids) getUser(id);
    }

    private FriendDTO toFriendDTO(User u, FriendshipStatus status) {
        return new FriendDTO(u.getId(), displayName(u), usernameOrEmail(u), status);
    }

    private String displayName(User u) {
        String fn = safe(u.getFirstName());
        String ln = safe(u.getLastName());
        String combined = (fn + " " + ln).trim();
        return combined.isBlank() ? usernameOrEmail(u) : combined;
    }

    private String usernameOrEmail(User u) {
        String uname = safe(u.getUsername());
        if (!uname.isBlank()) return uname;
        String email = safe(u.getEmail());
        return email;
    }

    private static String safe(String s) { return s == null ? "" : s; }
}