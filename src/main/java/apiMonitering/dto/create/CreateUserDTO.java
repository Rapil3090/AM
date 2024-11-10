package apiMonitering.dto.create;

import lombok.*;

import java.time.LocalDateTime;

public class CreateUserDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        private Long id;

    }

}
