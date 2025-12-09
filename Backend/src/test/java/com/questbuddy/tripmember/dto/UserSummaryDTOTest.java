package com.questbuddy.tripmember.dto;

import com.questbuddy.user.model.User;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserSummaryDTOTest {

    @Test
    public void testFromWithFullName() {
        User u = new User();
        u.setId(1L);
        u.setFirstName("Ayaan");
        u.setLastName("Syed");
        u.setUsername("ayaan123");
        u.setAvatarUrl("avatar.png");

        UserSummaryDTO dto = UserSummaryDTO.from(u);

        assertEquals(Long.valueOf(1L), dto.id());
        assertEquals("Ayaan Syed", dto.displayName());
        assertEquals("ayaan123", dto.username());
        assertEquals("avatar.png", dto.avatarUrl());
    }

    @Test
    public void testFromWithFirstOnly() {
        User u = new User();
        u.setId(2L);
        u.setFirstName("Mark");
        u.setUsername("marky");

        UserSummaryDTO dto = UserSummaryDTO.from(u);
        assertEquals("Mark", dto.displayName());
    }

    @Test
    public void testFromWithLastOnly() {
        User u = new User();
        u.setId(3L);
        u.setLastName("Lee");
        u.setUsername("lee");

        UserSummaryDTO dto = UserSummaryDTO.from(u);
        assertEquals("Lee", dto.displayName());
    }

    @Test
    public void testFromWithUsernameFallback() {
        User u = new User();
        u.setId(4L);
        u.setUsername("user44");
    }
}
