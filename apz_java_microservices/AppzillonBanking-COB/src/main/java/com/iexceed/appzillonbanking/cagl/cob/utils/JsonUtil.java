package com.iexceed.appzillonbanking.cagl.cob.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private JsonUtil() {
        // Prevent instantiation
    }

    /**
     * Converts any Java object to its JSON representation.
     *
     * @param object Object to serialize
     * @return JSON string or null if object is null
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON.", e);
        }
    }

    /**
     * Converts a JSON string into the specified Java type.
     *
     * @param json JSON string
     * @param typeReference Target class
     * @param <T> Target type
     * @return Deserialized object
     */
    public static <T> T fromJsonString(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object.", e);
        }
    }

    /**
     * Returns the shared ObjectMapper instance.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}