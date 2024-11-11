package com.vinilemess.graphqljavaplayground.graphql.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for creating and configuring Jackson's {@link ObjectMapper} instances.
 * <p>
 * This utility class provides a method to create an {@link ObjectMapper} with specific configurations.
 * Disabled {@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} and {@link JavaTimeModule}
 * to handle Java 8 date and time API types.
 */
public class ObjectMapperUtils {

    private ObjectMapperUtils() {
    }

    /**
     * Creates and configures an instance of {@link ObjectMapper} with specific settings.
     *
     * @return a configured instance of {@link ObjectMapper}.
     */
    public static ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
