package com.smartwatts.apigateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.function.Predicate;
import org.springframework.web.server.ServerWebExchange;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteLocator routeLocator;

    @Test
    void getGatewayInfo_Success_ReturnsInfo() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("SmartWatts API Gateway"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.status").value("running"))
                .andExpect(jsonPath("$.port").value(8080));
    }

    @Test
    void getRoutes_Success_ReturnsRoutes() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        Predicate<ServerWebExchange> mockPredicate = mock(Predicate.class);
        Route mockRoute = Route.async()
                .id("user-service-route")
                .uri(URI.create("http://localhost:8081"))
                .predicate(mockPredicate)
                .build();
        
        when(routeLocator.getRoutes()).thenReturn(Flux.just(mockRoute));

        // When & Then
        mockMvc.perform(get("/admin/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("user-service-route"));

        verify(routeLocator).getRoutes();
    }

    @Test
    void getHealth_Success_ReturnsHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.gateway").value("running"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}

