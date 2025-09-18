package com.mercadolibre.gateway.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.common.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            
            // Skip authentication for health checks and public endpoints
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid authorization header for path: {}", path);
                return onError(exchange, "Missing or invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Basic token validation (in production, validate JWT signature and expiration)
            if (!isValidToken(token)) {
                logger.warn("Invalid token for path: {}", path);
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Extract user information from token (in production, decode JWT)
            String userId = extractUserIdFromToken(token);
            if (userId != null) {
                exchange.getRequest().mutate()
                        .header("X-User-ID", userId)
                        .build();
            }

            logger.debug("Authentication successful for user: {} on path: {}", userId, path);
            return chain.filter(exchange);
        };
    }

    private boolean isPublicEndpoint(String path) {
        List<String> publicPaths = List.of(
                "/actuator/health",
                "/actuator/info",
                "/api/docs",
                "/swagger-ui",
                "/v3/api-docs"
        );
        
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private boolean isValidToken(String token) {
        // Basic validation - in production, validate JWT signature and expiration
        return token != null && token.length() > 10 && !token.equals("invalid");
    }

    private String extractUserIdFromToken(String token) {
        // In production, decode JWT and extract user ID
        // For this prototype, return a mock user ID
        return "user-" + token.hashCode();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        ApiResponse<Void> errorResponse = ApiResponse.error(message,
                httpStatus == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED" : "FORBIDDEN");

        try {
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            exchange.getResponse().getHeaders().setContentLength(responseBody.length());
            
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory()
                            .wrap(responseBody.getBytes(StandardCharsets.UTF_8)))
            );
        } catch (JsonProcessingException e) {
            logger.error("Error serializing error response", e);
            return exchange.getResponse().setComplete();
        }
    }

    public static class Config {
        private boolean enabled = true;
        private List<String> skipPaths = List.of("/actuator/health", "/api/docs");

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getSkipPaths() {
            return skipPaths;
        }

        public void setSkipPaths(List<String> skipPaths) {
            this.skipPaths = skipPaths;
        }
    }
}
