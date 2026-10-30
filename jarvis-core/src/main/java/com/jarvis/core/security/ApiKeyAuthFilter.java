package com.jarvis.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jarvis.core.config.JarvisProperties;
import com.jarvis.core.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final JarvisProperties properties;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(JarvisProperties properties, RateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.properties = properties;
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/api/health") || path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        if (!rateLimiterService.tryConsume(clientIp)) {
            sendError(response, HttpServletResponse.SC_TOO_MANY_REQUESTS, "Rate limit exceeded.");
            return;
        }

        String expectedSecret = (properties.security() != null) ? properties.security().apiSecret() : null;
        if (expectedSecret != null && !expectedSecret.trim().isBlank()) {
            String authorization = request.getHeader("Authorization");
            String customHeader = request.getHeader("X-Jarvis-Secret");

            boolean valid = false;
            if (customHeader != null && customHeader.equals(expectedSecret)) {
                valid = true;
            } else if (authorization != null && authorization.startsWith("Bearer ")) {
                if (authorization.substring(7).trim().equals(expectedSecret)) {
                    valid = true;
                }
            }

            if (!valid) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid API Secret.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private void sendError(HttpServletResponse response, int status, String msg) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(false, msg, status)));
    }
}
