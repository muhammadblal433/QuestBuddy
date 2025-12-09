package com.questbuddy.tripmember.dto;

import org.junit.Test;
import static org.junit.Assert.*;

public class InviteDTOTest {

    @Test
    public void testInviteDTOStoresValue() {
        InviteDTO dto = new InviteDTO(10L);
        assertEquals(Long.valueOf(10L), dto.userId());
    }
}
