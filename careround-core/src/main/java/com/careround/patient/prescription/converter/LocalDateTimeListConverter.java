package com.careround.patient.prescription.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Converter
public class LocalDateTimeListConverter implements AttributeConverter<List<LocalDateTime>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(List<LocalDateTime> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize administration times", e);
        }
    }

    @Override
    public List<LocalDateTime> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(dbData, new TypeReference<List<LocalDateTime>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize administration times", e);
        }
    }
}
