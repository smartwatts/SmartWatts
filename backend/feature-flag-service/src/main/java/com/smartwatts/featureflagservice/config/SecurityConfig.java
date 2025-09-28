package com.smartwatts.featureflagservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/feature-flags/features/**").permitAll()
                .requestMatchers("/api/feature-flags/check/**").permitAll()
                .requestMatchers("/api/feature-flags/user-access/**").permitAll()
                .requestMatchers("/api/feature-flags/toggle/**").hasRole("ADMIN")
                .requestMatchers("/api/feature-flags/update/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
