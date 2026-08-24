package com.iexceed.appzillonbanking.cagl.cob.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Objects;

/** Small helpers to read the flexible JsonNode payloads without NPEs / verbose null-checks. */
public final class JsonNodeUtil {

    private JsonNodeUtil() {
    }

    public static String text(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        return node.get(field).asText();
    }

    public static String text(JsonNode node, String field, String defaultValue) {
        String value = text(node, field);
        return value != null ? value : defaultValue;
    }

    public static Boolean bool(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        return node.get(field).asBoolean();
    }

    public static JsonNode array(JsonNode node, String field) {
        if (node == null || !node.has(field) || !node.get(field).isArray()) {
            return null;
        }
        return node.get(field);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(ObjectMapper mapper, JsonNode node) {
        if (node == null || node.isNull()) {
            return Map.of();
        }
        return mapper.convertValue(node, Map.class);
    }

    /** True if oldValue and newValue differ, treating null and blank-string as equivalent "empty". */
    public static boolean differs(Object oldValue, Object newValue) {
        String oldStr = oldValue == null ? "" : oldValue.toString();
        String newStr = newValue == null ? "" : newValue.toString();
        return !Objects.equals(oldStr, newStr);
    }
}
