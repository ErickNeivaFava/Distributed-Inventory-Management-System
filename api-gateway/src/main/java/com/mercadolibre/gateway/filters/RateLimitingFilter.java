package com.mercadolibre.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RateLimitingFilter extends RequestRateLimiterGatewayFilterFactory {

    private final RateLimiter<?> rateLimiter;
    private final KeyResolver keyResolver;

    public RateLimitingFilter(RateLimiter<?> rateLimiter, KeyResolver keyResolver) {
        super(rateLimiter, keyResolver);
        this.rateLimiter = rateLimiter;
        this.keyResolver = keyResolver;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            return keyResolver.resolve(exchange).flatMap(key ->
                    rateLimiter.isAllowed(route.getId(), key).flatMap(response -> {
                        if (response.isAllowed()) {
                            return chain.filter(exchange);
                        }

                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    })
            );
        };
    }
}
