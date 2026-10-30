package com.jarvis.core.dto;

import com.jarvis.core.memory.MessageRecord;
import java.util.List;

public record MemoryHistoryResponse(String sessionId, int messageCount, List<MessageRecord> history) {}
