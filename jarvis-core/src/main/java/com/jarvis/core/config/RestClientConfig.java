package com.jarvis.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient restClient(JarvisProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int connectTimeout = (properties.brain() != null && properties.brain().gemini() != null)
                ? properties.brain().gemini().connectTimeoutMs() : 5000;
        int readTimeout = (properties.brain() != null && properties.brain().gemini() != null)
                ? properties.brain().gemini().readTimeoutMs() : 30000;

        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return RestClient.builder().requestFactory(factory).build();
    }
}
