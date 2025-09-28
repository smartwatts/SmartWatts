package com.smartwatts.apigateway.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfig {

    @Bean
    public Counter requestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("smartwatts.requests.total")
                .description("Total number of requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter errorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("smartwatts.errors.total")
                .description("Total number of errors")
                .register(meterRegistry);
    }

    @Bean
    public Timer requestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("smartwatts.request.duration")
                .description("Request duration")
                .register(meterRegistry);
    }

    @Bean
    public Counter rateLimitCounter(MeterRegistry meterRegistry) {
        return Counter.builder("smartwatts.rate_limit.exceeded")
                .description("Number of rate limit exceeded")
                .register(meterRegistry);
    }
}
