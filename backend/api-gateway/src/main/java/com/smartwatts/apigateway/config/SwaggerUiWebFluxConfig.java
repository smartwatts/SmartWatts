package com.smartwatts.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import java.net.URI;
import java.util.Objects;

@Configuration
public class SwaggerUiWebFluxConfig {
    
    @Bean
    public RouterFunction<ServerResponse> swaggerUiRouter() {
        URI redirectUri = Objects.requireNonNull(URI.create("/swagger-ui/index.html"), 
            "Redirect URI cannot be null");
        return route(GET("/swagger-ui.html"),
            req -> ServerResponse.temporaryRedirect(redirectUri).build());
    }
} 