package apiMonitering.dto.create;

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

        private List<String> param;

        private List<String> param2;

        private String status;

        private LocalDateTime startTime;

    }


}
