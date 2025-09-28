package com.smartwatts.userservice.config;

import com.smartwatts.userservice.service.JwtService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ApplicationContext applicationContext;

    public JwtAuthenticationFilter(JwtService jwtService, ApplicationContext applicationContext) {
        this.jwtService = jwtService;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        log.info("JWT Authentication Filter initialized");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        log.debug("JWT Filter: Processing request to {}", requestURI);

        // Skip JWT processing for login and register endpoints
        if (requestURI.contains("/login") || requestURI.contains("/register") || 
            requestURI.contains("/forgot-password") || requestURI.contains("/reset-password")) {
            log.debug("JWT Filter: Skipping JWT processing for authentication endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("JWT Filter: No valid Authorization header, continuing");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            log.debug("JWT Filter: Extracted username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetailsService userDetailsService = applicationContext.getBean(UserDetailsService.class);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("JWT Filter: Token is valid, setting authentication for user: {}", username);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT Filter: Authentication set successfully for user: {}", username);
                } else {
                    log.debug("JWT Filter: Token is not valid for user: {}", username);
                }
            } else {
                log.debug("JWT Filter: Username is null or authentication already exists. Username: {}, Existing auth: {}",
                         username, SecurityContextHolder.getContext().getAuthentication() != null);
            }
        } catch (Exception e) {
            log.error("JWT Filter: Error processing JWT token: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
} 