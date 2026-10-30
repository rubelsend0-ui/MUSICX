package com.jarvis.core.memory;

import java.time.Instant;

public record MessageRecord(String role, String content, Instant timestamp) {}
