package apiMonitering.DTO.ApiEndpointDTO;

import lombok.*;

import java.time.LocalDateTime;

public class RequestApiEndpointDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        private String url;

        private String serviceKey;

        private String param;

        private String status;

        private LocalDateTime startTime;

    }


}
