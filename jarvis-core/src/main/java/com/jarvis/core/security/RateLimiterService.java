package com.jarvis.core.security;

import com.jarvis.core.config.JarvisProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RateLimiterService {
    private final int capacity;
    private final double refillTokensPerSecond;
    private final Map<String, TokenBucket> buckets;

    public RateLimiterService(JarvisProperties properties) {
        int cap = 60;
        int refillMin = 60;
        if (properties.security() != null && properties.security().rateLimit() != null) {
            cap = Math.max(1, properties.security().rateLimit().capacity());
            refillMin = Math.max(1, properties.security().rateLimit().refillTokensPerMinute());
        }
        this.capacity = cap;
        this.refillTokensPerSecond = (double) refillMin / 60.0;
        this.buckets = Collections.synchronizedMap(new LinkedHashMap<>(2000, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, TokenBucket> eldest) {
                return size() > 5000;
            }
        });
    }

    public boolean tryConsume(String clientKey) {
        TokenBucket bucket = buckets.computeIfAbsent(clientKey, k -> new TokenBucket(capacity, refillTokensPerSecond));
        return bucket.tryConsume();
    }

    private static class TokenBucket {
        private final int capacity;
        private final double refillRatePerSecond;
        private double tokens;
        private Instant lastRefill;

        public TokenBucket(int capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            long elapsedMillis = java.time.Duration.between(lastRefill, now).toMillis();
            if (elapsedMillis > 0) {
                tokens = Math.min(capacity, tokens + (elapsedMillis / 1000.0) * refillRatePerSecond);
                lastRefill = now;
            }
        }
    }
}
