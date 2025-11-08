package com.smartwatts.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartwatts.userservice.service.JwtService;
import java.util.Map;

@SpringBootApplication
@EnableJpaAuditing
public class UserServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Autowired
    private ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void logBeanInfo() {
        logger.info("=== BEAN DIAGNOSTICS ===");
        
        // Check JwtService beans
        Map<String, JwtService> jwtServices = applicationContext.getBeansOfType(JwtService.class);
        logger.info("JwtService beans: {}", jwtServices.size());
        jwtServices.forEach((name, service) -> logger.info("  {} : {}", name, service.getClass().getName()));
        
        // Check UserDetailsService beans
        Map<String, UserDetailsService> userDetailsServices = applicationContext.getBeansOfType(UserDetailsService.class);
        logger.info("UserDetailsService beans: {}", userDetailsServices.size());
        userDetailsServices.forEach((name, service) -> logger.info("  {} : {}", name, service.getClass().getName()));
        
        // Check OncePerRequestFilter beans
        Map<String, OncePerRequestFilter> filters = applicationContext.getBeansOfType(OncePerRequestFilter.class);
        logger.info("OncePerRequestFilter beans: {}", filters.size());
        filters.forEach((name, filter) -> logger.info("  {} : {}", name, filter.getClass().getName()));
        
        // Check InventoryController beans
        try {
            Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(org.springframework.web.bind.annotation.RestController.class);
            logger.info("RestController beans: {}", controllers.size());
            controllers.forEach((name, controller) -> logger.info("  {} : {}", name, controller.getClass().getName()));
        } catch (Exception e) {
            logger.error("Error checking RestController beans: {}", e.getMessage());
        }
        
        logger.info("=== END BEAN DIAGNOSTICS ===");
    }
} 