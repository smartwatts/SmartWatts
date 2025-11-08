package com.smartwatts.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - no authentication required (minimal set)
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/v1/users/login").permitAll()
                .pathMatchers("/api/v1/users/register").permitAll()
                .pathMatchers("/api/v1/users/forgot-password").permitAll()
                .pathMatchers("/api/v1/users/reset-password").permitAll()
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // All other endpoints require authentication
                .anyExchange().authenticated()
            );
        
        return http.build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 