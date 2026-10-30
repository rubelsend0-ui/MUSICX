package com.jarvis.core.dto;

import jakarta.validation.constraints.NotBlank;

public record BrainSwitchRequest(@NotBlank(message = "Provider name must be specified") String provider) {}
