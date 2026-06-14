package com.cpt202.service.impl;

import com.cpt202.json.JacksonObjectMapper;
import com.cpt202.service.RedisCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements RedisCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JacksonObjectMapper objectMapper;

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception ex) {
            log.warn("Redis read failed for key {}, falling back to database", key, ex);
            safeDelete(key);
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, typeReference));
        } catch (Exception ex) {
            log.warn("Redis read failed for key {}, falling back to database", key, ex);
            safeDelete(key);
            return Optional.empty();
        }
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException ex) {
            log.warn("Redis serialization failed for key {}, skipping cache write", key, ex);
        } catch (Exception ex) {
            log.warn("Redis write failed for key {}, skipping cache write", key, ex);
        }
    }

    @Override
    public void delete(String key) {
        safeDelete(key);
    }

    private void safeDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.warn("Redis delete failed for key {}, continuing without cache eviction", key, ex);
        }
    }
}
