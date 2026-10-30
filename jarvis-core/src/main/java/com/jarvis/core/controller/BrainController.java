package com.jarvis.core.controller;

import com.jarvis.core.brain.BrainManager;
import com.jarvis.core.brain.BrainProvider;
import com.jarvis.core.brain.ProviderType;
import com.jarvis.core.dto.BrainInfoResponse;
import com.jarvis.core.dto.BrainSwitchRequest;
import com.jarvis.core.dto.ModelsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BrainController {
    private final BrainManager brainManager;

    public BrainController(BrainManager brainManager) {
        this.brainManager = brainManager;
    }

    @GetMapping("/brain")
    public ResponseEntity<BrainInfoResponse> getBrainInfo() {
        BrainProvider provider = brainManager.getActiveProvider();
        return ResponseEntity.ok(new BrainInfoResponse(provider.getType().name(), provider.getActiveModel(), provider.isConfigured()));
    }

    @GetMapping("/models")
    public ResponseEntity<ModelsResponse> getModels() {
        List<ModelsResponse.ProviderModelDetails> list = brainManager.getAllProviders().values().stream()
                .map(p -> new ModelsResponse.ProviderModelDetails(p.getType().name(), p.getActiveModel(), p.isConfigured()))
                .toList();
        return ResponseEntity.ok(new ModelsResponse(list));
    }

    @PostMapping("/brain/switch")
    public ResponseEntity<BrainInfoResponse> switchBrain(@Valid @RequestBody BrainSwitchRequest request) {
        ProviderType targetType = ProviderType.valueOf(request.provider().trim().toUpperCase());
        brainManager.switchProvider(targetType);
        BrainProvider active = brainManager.getActiveProvider();
        return ResponseEntity.ok(new BrainInfoResponse(active.getType().name(), active.getActiveModel(), active.isConfigured()));
    }
}
