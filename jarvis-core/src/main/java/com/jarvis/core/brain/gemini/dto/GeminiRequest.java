package com.jarvis.core.brain.gemini.dto;

import java.util.List;

public record GeminiRequest(List<GeminiContent> contents) {}
