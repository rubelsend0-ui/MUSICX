package com.jarvis.core.brain;

import com.jarvis.core.memory.MessageRecord;
import java.util.List;

public interface BrainProvider {
    ProviderType getType();
    String getActiveModel();
    boolean isConfigured();
    String generateReply(String userMessage, List<MessageRecord> history);
}
