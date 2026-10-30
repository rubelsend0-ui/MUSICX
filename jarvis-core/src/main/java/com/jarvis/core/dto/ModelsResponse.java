package com.jarvis.core.dto;

import java.util.List;

public record ModelsResponse(List<ProviderModelDetails> models) {
    public record ProviderModelDetails(String provider, String activeModel, boolean configured) {}
}
