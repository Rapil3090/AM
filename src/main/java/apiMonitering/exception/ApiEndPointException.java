package apiMonitering.exception;

import apiMonitering.type.ErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiEndPointException extends RuntimeException {

    private ErrorCode errorCode;
    private String errorMessage;

    public ApiEndPointException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
