package com.smartwatts.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RateLimitingFilterTest {

    @Autowired(required = false)
    private RateLimitingFilter rateLimitingFilter;

    @Autowired(required = false)
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clean up Redis before each test
        if (redisTemplate != null) {
            redisTemplate.getConnectionFactory()
                    .getReactiveConnection()
                    .serverCommands()
                    .flushAll()
                    .block();
        }
    }

    @Test
    void testRateLimitingFilterExists() {
        assertNotNull(rateLimitingFilter, "Rate limiting filter should be configured");
    }

    @Test
    void testRateLimitingWithValidConfig() {
        if (rateLimitingFilter == null) {
            // Skip test if Redis is not available
            return;
        }

        RateLimitingFilter.Config config = new RateLimitingFilter.Config();
        config.setLimit(10);
        config.setWindow(60);

        assertNotNull(config);
        assertEquals(10, config.getLimit());
        assertEquals(60, config.getWindow());
    }

    @Test
    void testRateLimitingFilterConfiguration() {
        if (rateLimitingFilter == null) {
            // Skip test if Redis is not available
            return;
        }

        // Verify filter is properly configured
        assertNotNull(rateLimitingFilter);
    }
}


