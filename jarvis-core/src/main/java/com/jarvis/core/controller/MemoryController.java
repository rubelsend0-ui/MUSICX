package com.jarvis.core.controller;

import com.jarvis.core.dto.MemoryHistoryResponse;
import com.jarvis.core.memory.ConversationMemory;
import com.jarvis.core.memory.MessageRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {
    private final ConversationMemory memory;

    public MemoryController(ConversationMemory memory) {
        this.memory = memory;
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<MemoryHistoryResponse> getSessionMemory(@PathVariable String sessionId) {
        List<MessageRecord> history = memory.getHistory(sessionId);
        return ResponseEntity.ok(new MemoryHistoryResponse(sessionId, history.size(), history));
    }
}
