package com.smartwatts.userservice.config;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"production", "staging"})
public class SentryConfig {

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @Value("${sentry.environment:production}")
    private String environment;

    @Value("${sentry.release:}")
    private String release;

    @Value("${sentry.traces-sample-rate:1.0}")
    private double tracesSampleRate;

    @Bean
    public SentryOptions sentryOptions() {
        SentryOptions options = new SentryOptions();
        
        if (sentryDsn != null && !sentryDsn.isEmpty()) {
            options.setDsn(sentryDsn);
            options.setEnvironment(environment);
            options.setRelease(release);
            options.setTracesSampleRate(tracesSampleRate);
            options.setSendDefaultPii(false);
            
            // Configure before send filter
            options.setBeforeSend((event, hint) -> {
                // Filter out non-critical errors
                if (event.getLevel() == io.sentry.SentryLevel.WARNING) {
                    return null;
                }
                return event;
            });
            
            // Initialize Sentry
            Sentry.init(options);
        }
        
        return options;
    }
}

