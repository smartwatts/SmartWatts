package com.smartwatts.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component("RateLimiting")
@Slf4j
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    @Autowired(required = false)
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local limit = tonumber(ARGV[1])\n" +
        "local window = tonumber(ARGV[2])\n" +
        "local current = redis.call('INCR', key)\n" +
        "if current == 1 then\n" +
        "    redis.call('EXPIRE', key, window)\n" +
        "end\n" +
        "if current > limit then\n" +
        "    return {0, current}\n" +
        "else\n" +
        "    return {1, current}\n" +
        "end";

    public RateLimitingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // If Redis is not available, allow the request (fallback)
            if (redisTemplate == null) {
                log.warn("Redis not available for rate limiting, allowing request");
                return chain.filter(exchange);
            }

            String clientId = getClientId(exchange);
            String key = "rate_limit:" + clientId + ":" + exchange.getRequest().getURI().getPath();
            
            @SuppressWarnings("unchecked")
            RedisScript<List<Long>> script = (RedisScript<List<Long>>) (RedisScript<?>) RedisScript.of(RATE_LIMIT_SCRIPT, List.class);
            List<String> keys = Collections.singletonList(key);
            List<String> args = List.of(
                String.valueOf(config.getLimit()),
                String.valueOf(config.getWindow())
            );

            return redisTemplate.execute(script, keys, args)
                .next() // Convert Flux to Mono
                .flatMap(result -> {
                    List<Long> results = (List<Long>) result;
                    long allowed = results.get(0);
                    long current = results.get(1);

                    // Add rate limit headers
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getLimit()));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, config.getLimit() - current)));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + config.getWindow()));

                    if (allowed == 1) {
                        log.debug("Rate limit check passed for client: {}, current: {}/{}", clientId, current, config.getLimit());
                        return chain.filter(exchange);
                    } else {
                        log.warn("Rate limit exceeded for client: {}, current: {}/{}", clientId, current, config.getLimit());
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        String body = "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}";
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                        return exchange.getResponse().writeWith(Mono.just(buffer));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error in rate limiting: {}", e.getMessage(), e);
                    // On error, allow the request (fail open)
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange)); // Fallback if Redis unavailable
        };
    }

    private String getClientId(ServerWebExchange exchange) {
        // Try to get client ID from header, IP, or user
        String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");
        if (clientId != null && !clientId.isEmpty()) {
            return clientId;
        }
        
        // Fallback to IP address
        String ipAddress = exchange.getRequest().getRemoteAddress() != null 
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            : "unknown";
        return ipAddress;
    }

    public static class Config {
        private int limit = 100;
        private int window = 60; // seconds

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getWindow() {
            return window;
        }

        public void setWindow(int window) {
            this.window = window;
        }
    }
}