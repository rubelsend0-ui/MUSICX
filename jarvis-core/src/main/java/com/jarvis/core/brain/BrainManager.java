package com.jarvis.core.brain;

import com.jarvis.core.config.JarvisProperties;
import com.jarvis.core.exception.BrainProviderUnavailableException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class BrainManager {
    private final Map<ProviderType, BrainProvider> providers = new EnumMap<>(ProviderType.class);
    private volatile ProviderType currentProviderType;

    public BrainManager(List<BrainProvider> brainProviders, JarvisProperties properties) {
        for (BrainProvider provider : brainProviders) {
            this.providers.put(provider.getType(), provider);
        }
        ProviderType defaultType = (properties.brain() != null && properties.brain().activeProvider() != null)
                ? properties.brain().activeProvider() : ProviderType.GEMINI;
        this.currentProviderType = defaultType;
    }

    public BrainProvider getActiveProvider() {
        BrainProvider provider = providers.get(currentProviderType);
        if (provider == null) {
            throw new BrainProviderUnavailableException("No active brain provider registered for: " + currentProviderType);
        }
        return provider;
    }

    public synchronized void switchProvider(ProviderType newType) {
        if (!providers.containsKey(newType)) {
            throw new IllegalArgumentException("Unsupported provider: " + newType);
        }
        this.currentProviderType = newType;
    }

    public Map<ProviderType, BrainProvider> getAllProviders() { return Map.copyOf(providers); }
    public ProviderType getCurrentProviderType() { return currentProviderType; }
}
