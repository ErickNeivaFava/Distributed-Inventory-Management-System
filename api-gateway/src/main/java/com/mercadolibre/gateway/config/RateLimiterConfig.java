package com.mercadolibre.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Use authenticated user ID or fallback to IP address
            return exchange.getPrincipal()
                    .map(principal -> Objects.requireNonNull(principal.getName()))
                    .switchIfEmpty(Mono.just(
                            Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                                    .getAddress().getHostAddress()));
        };
    }
}