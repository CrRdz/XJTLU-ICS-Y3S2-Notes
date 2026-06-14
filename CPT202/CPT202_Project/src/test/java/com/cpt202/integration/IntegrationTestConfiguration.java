package com.cpt202.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Test-only infrastructure overrides for integration and acceptance suites. */
@TestConfiguration
class IntegrationTestConfiguration {

    /** Replaces Redis with an in-memory cache for deterministic test runs. */
    @Bean
    @Primary
    InMemoryRedisCacheService redisCacheService(ObjectMapper objectMapper) {
        return new InMemoryRedisCacheService(objectMapper);
    }
}
