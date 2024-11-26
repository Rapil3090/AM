package apiMonitering.handler;

import apiMonitering.domain.ApiResponse;
import apiMonitering.exception.ApiEndPointException;
import apiMonitering.repository.ApiResponseRepository;
import apiMonitering.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
public class ApiResponseHandler {

    private final ApiResponseRepository apiResponseRepository;

    public Mono<ApiResponse> apiResponseErrorHandler(
            Throwable throwable, ApiResponse apiResponse, Instant startTime) {
        Long responseTime = Duration.between(startTime, Instant.now()).toMillis();

        apiResponse.setResponseTime(responseTime);
        apiResponse.set_success(false);

        if (throwable instanceof TimeoutException timeout) {

            apiResponse.setStatusCode(408);
            apiResponse.setErrorMessage("타임아웃");
            log.error("타임아웃 발생 : {}", timeout.getMessage());

            return Mono.just(apiResponseRepository.save(apiResponse));
        }

        if (throwable instanceof WebClientResponseException error) {
            int statusCode = error.getStatusCode().value();
            apiResponse.setStatusCode(statusCode);
            apiResponse.setErrorMessage(error.getMessage());

            if (statusCode == 408) {
                log.error("타임아웃 발생 : 상태코드 = {}, 메시지 = {}", statusCode, error.getMessage());
                throw new ApiEndPointException(ErrorCode.TIMEOUT_ERROR);
            }

            if (statusCode == 500) {
                log.error("서버 내부 오류 발생 : 상태코드 = {}, 메시지 = {}", statusCode, error.getMessage());
                throw new ApiEndPointException(ErrorCode.SERVER_ERROR);
            }

            if (error.getStatusCode().is4xxClientError()) {
                log.error("4xx 클라이언트 에러 발생 : 상태코드 = {}, 메시지 = {}", statusCode, error.getMessage());
                throw new ApiEndPointException(ErrorCode.CLIENT_ERROR, "클라이언트 오류 : " + error.getMessage());
            }

            if (error.getStatusCode().is5xxServerError()) {
                log.error("5xx 서버 에러 발생 : 상태코드 = {}, 메시지 = {}", statusCode, error.getMessage());
                throw new ApiEndPointException(ErrorCode.SERVER_ERROR, "서버 오류 : " + error.getMessage());
            }
        }

        apiResponse.setStatusCode(500);
        apiResponse.setErrorMessage("알 수 없는 에러 발생");
        log.error("알 수 없는 에러 발생 : {}", throwable.getMessage());

        return Mono.just(apiResponseRepository.save(apiResponse));
    }
}
