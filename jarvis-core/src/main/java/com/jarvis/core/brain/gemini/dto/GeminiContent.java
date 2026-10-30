package com.jarvis.core.brain.gemini.dto;

import java.util.List;

public record GeminiContent(String role, List<GeminiPart> parts) {}
