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
                // Public endpoints
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/v1/users/register").permitAll()
                .pathMatchers("/api/v1/users/login").permitAll()
                .pathMatchers("/api/v1/users/refresh-token").permitAll()
                .pathMatchers("/api/v1/users/forgot-password").permitAll()
                .pathMatchers("/api/v1/users/reset-password").permitAll()
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .pathMatchers("/fallback/**").permitAll()
                .pathMatchers("/hello").permitAll()
                .pathMatchers("/admin/**").permitAll()
                .pathMatchers("/api/proxy/**").permitAll()
                // Protected endpoints
                .pathMatchers("/api/v1/**").authenticated()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwkSetUri("http://localhost:8081/oauth2/jwks"))
            );
        
        return http.build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 