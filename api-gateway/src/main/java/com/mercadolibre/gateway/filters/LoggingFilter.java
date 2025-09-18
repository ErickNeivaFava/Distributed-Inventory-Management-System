package com.mercadolibre.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestId = request.getHeaders().getFirst("X-Request-ID");
            if (requestId == null) {
                requestId = java.util.UUID.randomUUID().toString();
            }

            long startTime = System.currentTimeMillis();
            String timestamp = LocalDateTime.now().format(formatter);
            
            // Log request
            logger.info("[{}] [{}] REQUEST: {} {} from {} - User-Agent: {} - Request-ID: {}", 
                    timestamp,
                    requestId,
                    request.getMethod(),
                    request.getURI(),
                    getClientIp(request),
                    request.getHeaders().getFirst("User-Agent"),
                    requestId);

            // Add request ID to response headers
            exchange.getResponse().getHeaders().add("X-Request-ID", requestId);

            String finalRequestId = requestId;
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;
                String endTimestamp = LocalDateTime.now().format(formatter);
                
                logger.info("[{}] [{}] RESPONSE: {} - Duration: {}ms - Status: {}", 
                        endTimestamp,
                        finalRequestId,
                        request.getMethod() + " " + request.getURI(),
                        duration,
                        response.getStatusCode());
                
                // Log slow requests
                if (duration > config.getSlowRequestThreshold()) {
                    logger.warn("[{}] [{}] SLOW REQUEST: {} took {}ms (threshold: {}ms)", 
                            endTimestamp,
                            finalRequestId,
                            request.getURI(),
                            duration,
                            config.getSlowRequestThreshold());
                }
            }));
        };
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    public static class Config {
        private boolean enabled = true;
        private long slowRequestThreshold = 1000; // 1 second
        private boolean logHeaders = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getSlowRequestThreshold() {
            return slowRequestThreshold;
        }

        public void setSlowRequestThreshold(long slowRequestThreshold) {
            this.slowRequestThreshold = slowRequestThreshold;
        }

        public boolean isLogHeaders() {
            return logHeaders;
        }

        public void setLogHeaders(boolean logHeaders) {
            this.logHeaders = logHeaders;
        }
    }
}
