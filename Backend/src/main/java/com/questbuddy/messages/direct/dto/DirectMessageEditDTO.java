package com.questbuddy.messages.direct.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * Edit payload for a direct message.
 * Requires the current version to prevent overwriting concurrent edits.
 */
public record DirectMessageEditDTO(
        @NotBlank @Size(max = 2000) String content,
        @NotNull Long version
) {}