package com.cpt202.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Centralized Jackson mapper used for Redis JSON serialization.
 */
public class JacksonObjectMapper extends ObjectMapper {

    public JacksonObjectMapper() {
        super();
        findAndRegisterModules();
        registerModule(new JavaTimeModule());
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
