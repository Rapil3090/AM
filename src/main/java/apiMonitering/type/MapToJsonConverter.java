package apiMonitering.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

@Converter(autoApply = true)
public class MapToJsonConverter implements AttributeConverter<Map<String, String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
//            throw  new RuntimeException("변환 실패", e);
            return attribute.toString();
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {

        if (dbData == null) {
            return null;
        }

        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
//            throw new RuntimeException("변환 실패", e);
            Map<String, String> result = new HashMap<>();
            result.put("error", dbData);
            return result;
        }
    }
}
