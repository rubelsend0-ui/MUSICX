package com.jarvis.core.brain.gemini.dto;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {
    public record Candidate(GeminiContent content, String finishReason) {}
}
