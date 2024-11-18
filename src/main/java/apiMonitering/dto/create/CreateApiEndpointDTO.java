package apiMonitering.dto.create;

import apiMonitering.domain.ApiEndpoint;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CreateApiEndpointDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        private String url;

        private String serviceKey;

        private List<List<String>> param;

        private String status;

        private LocalDateTime startTime;

        private List<ApiEndpoint.Parameter> parameters;

    }


}
