package com.iexceed.appzillonbanking.cagl.loan.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.loan.payload.DisbursementPayload;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DisbursementPayloadConverter
        implements AttributeConverter<DisbursementPayload, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(DisbursementPayload attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Error converting DisbursementPayload to JSON", e);
        }
    }

    @Override
    public DisbursementPayload convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, DisbursementPayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Error converting JSON to DisbursementPayload", e);
        }
    }
}