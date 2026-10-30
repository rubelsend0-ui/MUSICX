package com.jarvis.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "Message must not be empty")
        @Size(max = 4000, message = "Message must not exceed 4000 characters")
        String message,

        @NotBlank(message = "Session ID must not be empty")
        @Size(max = 128, message = "Session ID must not exceed 128 characters")
        String sessionId
) {}
