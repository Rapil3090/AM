package apiMonitering.exception;

import apiMonitering.dto.error.ErrorResponse;
import apiMonitering.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationExceptionException(DataIntegrityViolationException e) {

        log.error("DataIntegrityViolationException is occurred !", e);
        return new ErrorResponse(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDescription());
    }

    @ExceptionHandler
    public ErrorResponse handleException(Exception e) {
        log.error("Exception is occurred !", e);
        return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getDescription());
    }
}
