package com.cpt202.integration;

import com.cpt202.service.RedisCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Test-only in-memory Redis replacement for integration and acceptance suites. */
final class InMemoryRedisCacheService implements RedisCacheService {

    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, CacheEntry> entries = new ConcurrentHashMap<>();

    InMemoryRedisCacheService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        return readEntry(key).map(value -> clazz.isInstance(value)
                ? clazz.cast(value)
                : objectMapper.convertValue(value, clazz));
    }

    @Override
    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        return readEntry(key).map(value -> objectMapper.convertValue(value, typeReference));
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        Instant expiresAt = ttl == null ? null : Instant.now().plus(ttl);
        entries.put(key, new CacheEntry(value, expiresAt));
    }

    @Override
    public void delete(String key) {
        entries.remove(key);
    }

    void clear() {
        entries.clear();
    }

    private Optional<Object> readEntry(String key) {
        CacheEntry cacheEntry = entries.get(key);
        if (cacheEntry == null) {
            return Optional.empty();
        }
        if (cacheEntry.expiresAt() != null && cacheEntry.expiresAt().isBefore(Instant.now())) {
            entries.remove(key);
            return Optional.empty();
        }
        return Optional.ofNullable(cacheEntry.value());
    }

    private record CacheEntry(Object value, Instant expiresAt) {
    }
}