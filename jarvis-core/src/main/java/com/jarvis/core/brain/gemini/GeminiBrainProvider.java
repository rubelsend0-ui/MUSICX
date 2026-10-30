package com.jarvis.core.brain.gemini;

import com.jarvis.core.brain.BrainProvider;
import com.jarvis.core.brain.ProviderType;
import com.jarvis.core.brain.gemini.dto.*;
import com.jarvis.core.config.JarvisProperties;
import com.jarvis.core.exception.BrainProviderUnavailableException;
import com.jarvis.core.exception.UpstreamBrainException;
import com.jarvis.core.memory.MessageRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeminiBrainProvider implements BrainProvider {
    private static final Logger log = LoggerFactory.getLogger(GeminiBrainProvider.class);
    private final RestClient restClient;
    private final JarvisProperties properties;

    public GeminiBrainProvider(RestClient restClient, JarvisProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public ProviderType getType() { return ProviderType.GEMINI; }

    @Override
    public String getActiveModel() {
        GeminiProperties gemini = getGeminiProps();
        return (gemini != null && gemini.model() != null && !gemini.model().isBlank()) ? gemini.model() : "gemini-1.5-flash";
    }

    @Override
    public boolean isConfigured() {
        GeminiProperties gemini = getGeminiProps();
        return gemini != null && gemini.apiKey() != null && !gemini.apiKey().trim().isBlank();
    }

    @Override
    public String generateReply(String userMessage, List<MessageRecord> history) {
        if (!isConfigured()) {
            throw new BrainProviderUnavailableException("GEMINI_API_KEY is not configured.");
        }

        GeminiProperties gemini = getGeminiProps();
        String url = String.format("%s/models/%s:generateContent", gemini.baseUrl(), gemini.model());

        List<GeminiContent> contents = new ArrayList<>();
        if (history != null) {
            for (MessageRecord record : history) {
                String role = "user".equalsIgnoreCase(record.role()) ? "user" : "model";
                contents.add(new GeminiContent(role, List.of(new GeminiPart(record.content()))));
            }
        }
        contents.add(new GeminiContent("user", List.of(new GeminiPart(userMessage))));

        GeminiRequest requestBody = new GeminiRequest(contents);
        int maxRetries = Math.max(1, gemini.maxRetries());
        long backoffMs = Math.max(500, gemini.initialBackoffMs());

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                ResponseEntity<GeminiResponse> response = restClient.post()
                        .uri(url)
                        .header("x-goog-api-key", gemini.apiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .toEntity(GeminiResponse.class);

                return extractContent(response.getBody());
            } catch (HttpClientErrorException ex) {
                int code = ex.getStatusCode().value();
                if (code == 429) throw new UpstreamBrainException(429, "Gemini quota or rate limit exceeded.");
                throw new UpstreamBrainException(code, "Client error from Gemini API: " + ex.getMessage());
            } catch (HttpServerErrorException ex) {
                int code = ex.getStatusCode().value();
                if (attempt == maxRetries || code != 503) {
                    throw new UpstreamBrainException(code, "Gemini server error: " + ex.getMessage());
                }
                sleepBackoff(backoffMs, attempt);
            } catch (Exception ex) {
                if (attempt == maxRetries) {
                    throw new UpstreamBrainException(503, "Gemini communication failed: " + ex.getMessage());
                }
                sleepBackoff(backoffMs, attempt);
            }
        }
        throw new UpstreamBrainException(503, "Gemini failed after retry attempts.");
    }

    private String extractContent(GeminiResponse response) {
        if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
            GeminiResponse.Candidate candidate = response.candidates().getFirst();
            if (candidate.content() != null && candidate.content().parts() != null && !candidate.content().parts().isEmpty()) {
                String text = candidate.content().parts().getFirst().text();
                if (text != null && !text.isBlank()) return text.trim();
            }
        }
        return "Received empty response from Gemini.";
    }

    private void sleepBackoff(long baseBackoffMs, int attempt) {
        try {
            long delay = baseBackoffMs * (long) Math.pow(2, attempt - 1);
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new UpstreamBrainException(500, "Thread interrupted during backoff.");
        }
    }

    private GeminiProperties getGeminiProps() {
        return (properties.brain() != null) ? properties.brain().gemini() : null;
    }
}
