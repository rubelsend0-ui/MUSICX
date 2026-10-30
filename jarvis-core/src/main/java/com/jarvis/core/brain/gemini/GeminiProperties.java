package com.jarvis.core.brain.gemini;

public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        int connectTimeoutMs,
        int readTimeoutMs,
        int maxRetries,
        long initialBackoffMs
) {}
