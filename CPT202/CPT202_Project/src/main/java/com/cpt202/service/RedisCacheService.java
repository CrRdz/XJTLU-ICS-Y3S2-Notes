package com.cpt202.service;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.Optional;

public interface RedisCacheService {

    <T> Optional<T> get(String key, Class<T> clazz);

    <T> Optional<T> get(String key, TypeReference<T> typeReference);

    void set(String key, Object value, Duration ttl);

    void delete(String key);
}
