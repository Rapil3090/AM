package apiMonitering.utils;

import apiMonitering.domain.ApiEndpoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Converter
public class ParamListToJsonConverter implements AttributeConverter<List<ApiEndpoint.Parameter>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ApiEndpoint.Parameter> attribute) {

        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("변환 중 에러 발생", e);
        }
    }

    @Override
    public List<ApiEndpoint.Parameter> convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(dbData, new TypeReference<List<ApiEndpoint.Parameter>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 변환 에러", e);
        }
    }

}
