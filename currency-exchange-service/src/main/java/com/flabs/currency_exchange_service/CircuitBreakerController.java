package com.flabs.currency_exchange_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class CircuitBreakerController {

    private Logger logger = LoggerFactory.getLogger(CircuitBreakerController.class);

    @GetMapping("/sample-api")
    // @Retry(name = "sample-api", fallbackMethod = "hardcodedResponse")
    @CircuitBreaker(name = "sample-api", fallbackMethod = "hardcodedResponse")
    @RateLimiter(name = "default")
    // 10s => 10000 calls to sample api
    public String sampleApi() {
        logger.info("Sample Api call received");
        ResponseEntity<String> forEntity = new RestTemplate().getForEntity("http://localhost8080/some-dummy-url",
                String.class);
        return forEntity.getBody();
    }

    public String hardcodedResponse(Exception ex) {
        return "fallback-response";
    }
}
