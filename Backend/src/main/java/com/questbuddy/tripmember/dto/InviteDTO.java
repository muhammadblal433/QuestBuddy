package com.questbuddy.tripmember.dto;

import jakarta.validation.constraints.NotNull;

// DTO for sending an invite to join a trip
public record InviteDTO(
        @NotNull Long userId
) {}
