package com.smartwatts.apidocs.controller;

import com.smartwatts.apidocs.config.ServiceDiscoveryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v3/api-docs")
public class DocsProxyController {

    @Autowired
    private ServiceDiscoveryConfig serviceDiscoveryConfig;

    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/{service}")
    public Mono<ResponseEntity<String>> proxyDocs(@PathVariable String service) {
        String url = null;
        List<Map<String, Object>> services = serviceDiscoveryConfig.getServices();
        for (Map<String, Object> svc : services) {
            if (service.equals(svc.get("name"))) {
                url = (String)svc.get("url") + (String)svc.get("docs-path");
                break;
            }
        }
        if (url == null) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        return webClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("")
    public Map<String, Object> listServices() {
        Map<String, Object> result = new HashMap<>();
        result.put("services", serviceDiscoveryConfig.getServices());
        return result;
    }
} 