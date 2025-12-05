package com.questbuddy.tripmember.dto;

import com.questbuddy.user.model.User;

public record UserSummaryDTO(
        Long id,
        String displayName,
        String username,
        String avatarUrl
) {
    public static UserSummaryDTO from(User u) {
        String display = displayNameFor(u);
        String uname   = safe(u.getUsername());
        String avatar  = safe(u.getAvatarUrl());
        return new UserSummaryDTO(u.getId(), display, uname, avatar);
    }

    private static String displayNameFor(User u) {
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

    private static String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}