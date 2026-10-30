package com.jarvis.core.memory;

import com.jarvis.core.config.JarvisProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class InMemoryConversationMemory implements ConversationMemory {
    private final int maxMessages;
    private final Map<String, Deque<MessageRecord>> storage;

    public InMemoryConversationMemory(JarvisProperties properties) {
        int configuredSessions = 1000;
        int configuredMaxMessages = 20;

        if (properties.memory() != null) {
            configuredSessions = Math.max(10, properties.memory().maxSessions());
            configuredMaxMessages = Math.max(2, properties.memory().maxMessagesPerSession());
        }

        this.maxMessages = configuredMaxMessages;
        final int finalMaxSessions = configuredSessions;

        this.storage = Collections.synchronizedMap(new LinkedHashMap<>(finalMaxSessions, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Deque<MessageRecord>> eldest) {
                return size() > finalMaxSessions;
            }
        });
    }

    @Override
    public List<MessageRecord> getHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return List.of();
        Deque<MessageRecord> deque = storage.get(sessionId);
        if (deque == null) return List.of();
        synchronized (deque) {
            return new ArrayList<>(deque);
        }
    }

    @Override
    public void appendMessage(String sessionId, String role, String content) {
        if (sessionId == null || sessionId.isBlank()) return;
        Deque<MessageRecord> deque = storage.computeIfAbsent(sessionId, k -> new ArrayDeque<>());
        synchronized (deque) {
            if (deque.size() >= maxMessages) deque.pollFirst();
            deque.addLast(new MessageRecord(role, content, Instant.now()));
        }
    }

    @Override
    public void clear(String sessionId) {
        if (sessionId != null) storage.remove(sessionId);
    }
}
