package com.jarvis.core.service;

import com.jarvis.core.brain.BrainManager;
import com.jarvis.core.brain.BrainProvider;
import com.jarvis.core.dto.ChatRequest;
import com.jarvis.core.dto.ChatResponse;
import com.jarvis.core.memory.ConversationMemory;
import com.jarvis.core.memory.MessageRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private final BrainManager brainManager;
    private final ConversationMemory memory;

    public ChatService(BrainManager brainManager, ConversationMemory memory) {
        this.brainManager = brainManager;
        this.memory = memory;
    }

    public ChatResponse processChat(ChatRequest request) {
        BrainProvider provider = brainManager.getActiveProvider();
        List<MessageRecord> history = memory.getHistory(request.sessionId());

        String reply = provider.generateReply(request.message(), history);

        memory.appendMessage(request.sessionId(), "user", request.message());
        memory.appendMessage(request.sessionId(), "model", reply);

        return new ChatResponse(true, reply, provider.getActiveModel(), request.sessionId());
    }
}
