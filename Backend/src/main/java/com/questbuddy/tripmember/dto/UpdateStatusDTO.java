package com.questbuddy.tripmember.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// DTO to change status of invitation (and by extension -> membership status)
public record UpdateStatusDTO(
        @NotNull
        @Pattern(regexp = "ACCEPTED")
        String status
) {}