package com.jarvis.core.config;

import com.jarvis.core.brain.ProviderType;
import com.jarvis.core.brain.gemini.GeminiProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jarvis")
public record JarvisProperties(
        SecurityProperties security,
        MemoryProperties memory,
        BrainProperties brain
) {
    public record SecurityProperties(
            String apiSecret,
            RateLimitProperties rateLimit
    ) {
        public record RateLimitProperties(
                int capacity,
                int refillTokensPerMinute
        ) {}
    }

    public record MemoryProperties(
            int maxSessions,
            int maxMessagesPerSession
    ) {}

    public record BrainProperties(
            ProviderType activeProvider,
            GeminiProperties gemini
    ) {}
}
