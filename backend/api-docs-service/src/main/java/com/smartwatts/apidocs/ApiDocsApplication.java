package com.smartwatts.apidocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiDocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiDocsApplication.class, args);
    }
} 