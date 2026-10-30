package com.jarvis.core.memory;

import java.util.List;

public interface ConversationMemory {
    List<MessageRecord> getHistory(String sessionId);
    void appendMessage(String sessionId, String role, String content);
    void clear(String sessionId);
}
