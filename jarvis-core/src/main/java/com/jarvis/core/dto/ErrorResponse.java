package com.jarvis.core.dto;

public record ErrorResponse(boolean success, String error, int status) {}
