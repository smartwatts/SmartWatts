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

import com.smartwatts.userservice.service.JwtService;
import java.util.Map;

@SpringBootApplication
@EnableJpaAuditing
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Autowired
    private ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void logBeanInfo() {
        System.out.println("=== BEAN DIAGNOSTICS ===");
        

        
        // Check JwtService beans
        Map<String, JwtService> jwtServices = applicationContext.getBeansOfType(JwtService.class);
        System.out.println("JwtService beans: " + jwtServices.size());
        jwtServices.forEach((name, service) -> System.out.println("  " + name + " : " + service.getClass().getName()));
        
        // Check UserDetailsService beans
        Map<String, UserDetailsService> userDetailsServices = applicationContext.getBeansOfType(UserDetailsService.class);
        System.out.println("UserDetailsService beans: " + userDetailsServices.size());
        userDetailsServices.forEach((name, service) -> System.out.println("  " + name + " : " + service.getClass().getName()));
        
        // Check OncePerRequestFilter beans
        Map<String, OncePerRequestFilter> filters = applicationContext.getBeansOfType(OncePerRequestFilter.class);
        System.out.println("OncePerRequestFilter beans: " + filters.size());
        filters.forEach((name, filter) -> System.out.println("  " + name + " : " + filter.getClass().getName()));
        
        System.out.println("=== END BEAN DIAGNOSTICS ===");
    }
} 