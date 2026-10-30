package com.jarvis.core.dto;

public record ChatResponse(boolean success, String reply, String model, String sessionId) {}
