package com.iexceed.appzillonbanking.cagl.cob.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Shared helper for services whose entities carry pre-serialized JSON strings in TEXT columns.
 * Walks the ENTIRE object graph - not just the top level - swapping any String value that is
 * itself valid JSON array/object text for its parsed tree, so it serializes as nested JSON instead
 * of double-escaped text. Recurses into Maps, Lists, and the JsonNode trees produced by earlier
 * inlining, so JSON-text columns at any depth (documentList[].documentDetails.payload,
 * addresses[].addrPayload, incomeDetails.incomePayload, ...) are all picked up regardless of how
 * deeply nested they are. Plain text fields ("APPROVED", "10:00", Map#toString() text like
 * addrPayload, etc.) fail to parse as JSON and are left as-is. No field names are hardcoded, so
 * newly added JSON-text columns are picked up automatically.
 */
@Component
public class JsonInliningUtil {

    private final ObjectMapper objectMapper;

    public JsonInliningUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void inlineStoredJsonStrings(Map<String, Object> responseMap) {
        walk(responseMap);
    }

    @SuppressWarnings("unchecked")
    private void walk(Object node) {
        if (node instanceof Map<?, ?> rawMap) {
            Map<Object, Object> map = (Map<Object, Object>) rawMap;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                entry.setValue(inlineAndRecurse(entry.getValue()));
            }
        } else if (node instanceof List<?> rawList) {
            List<Object> list = (List<Object>) rawList;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, inlineAndRecurse(list.get(i)));
            }
        } else if (node instanceof ObjectNode objectNode) {
            objectNode.fieldNames().forEachRemaining(field ->
                    objectNode.set(field, inlineAndRecurseNode(objectNode.get(field))));
        } else if (node instanceof ArrayNode arrayNode) {
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, inlineAndRecurseNode(arrayNode.get(i)));
            }
        }
    }

    private Object inlineAndRecurse(Object value) {
        Object result = tryInline(value);
        walk(result);
        return result;
    }

    private JsonNode inlineAndRecurseNode(JsonNode value) {
        if (value.isTextual()) {
            JsonNode parsed = tryParse(value.asText());
            if (parsed != null) {
                walk(parsed);
                return parsed;
            }
            return value;
        }
        walk(value);
        return value;
    }

    private Object tryInline(Object value) {
        if (value instanceof String str) {
            JsonNode parsed = tryParse(str);
            if (parsed != null) {
                return parsed;
            }
        }
        return value;
    }

    private JsonNode tryParse(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(str);
            return node.isContainerNode() ? node : null;
        } catch (JsonProcessingException ignored) {
            return null; // not JSON text (e.g. "APPROVED", "10:00", "Village Community Hall")
        }
    }
}