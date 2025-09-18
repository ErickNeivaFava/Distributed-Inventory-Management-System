package com.mercadolibre.gateway.config;

import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

//    @Bean
//    public WebFluxProperties webFluxProperties() {
//        return new WebFluxProperties();
//    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("inventoryCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/inventory")))
                        .uri("lb://inventory-service"))
                .route("sync-service", r -> r
                        .path("/api/sync/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("syncCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/sync")))
                        .uri("lb://sync-service"))
                .build();
    }
}
