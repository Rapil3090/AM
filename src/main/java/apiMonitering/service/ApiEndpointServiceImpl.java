package apiMonitering.service;

import apiMonitering.domain.ApiResponse;
import apiMonitering.dto.create.CreateApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.exception.ApiEndPointException;
import apiMonitering.repository.ApiEndpointRepository;
import apiMonitering.repository.ApiResponseRepository;
import apiMonitering.type.ErrorCode;
import apiMonitering.type.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ApiEndpointServiceImpl implements ApiEndpointService {

    private final ApiEndpointRepository apiEndpointRepository;
    private final ApiResponseRepository apiResponseRepository;


    @Scheduled(fixedRate = 6000)
    public void scheduledApiCall() {

        apiEndpointRepository.findAll().forEach(apiEndpoint -> {


            getApi(apiEndpoint)
                    .subscribe(
                            response -> System.out.println("응답: " + response),
                            error -> System.out.println("에러: " + error.getMessage())
                    );
        });
    }


    public Mono<ApiResponse> getApi(ApiEndpoint apiEndpoint) {

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiEndpoint.getUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        List<ApiEndpoint.Parameter> parameters = apiEndpoint.getParameters();

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(apiEndpoint.getUrl())
                .build();

        String apiKeyValue = parameters.stream()
                .filter(param -> "apiKey".equalsIgnoreCase(param.getType()))
                .map(ApiEndpoint.Parameter::getValue)
                .findFirst()
                .orElseThrow(() -> new ApiEndPointException(ErrorCode.INVALID_SERVICEKEY));

        URI encodedServiceKey;

        try {
            encodedServiceKey = new URI(apiKeyValue);
        } catch (URISyntaxException e) {
            throw new ApiEndPointException(ErrorCode.INVALID_SERVICEKEY);
        }

        Instant startTime = Instant.now();

        Long responseTime = Duration.between(startTime, Instant.now()).toMillis();

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setApiEndpoint(apiEndpoint);

        return webClient.get()

                .uri(uriBuilder -> {
                    parameters.stream()
                                    .filter(param -> "query".equalsIgnoreCase(param.getType()))
                                    .forEach(param ->
                                                uriBuilder.queryParam(param.getKey(), param.getValue()));

                    boolean hasQueryType = parameters.stream()
                            .anyMatch(param -> "query".equalsIgnoreCase(param.getType()));

                    if (hasQueryType) {
                        parameters.stream()
                                .filter(param -> "apiKey".equalsIgnoreCase(param.getType()))
                                .findFirst()
                                .ifPresent(apiKeyParam ->
                                        uriBuilder.queryParam(apiKeyParam.getKey(), encodedServiceKey));
                    }
                        return uriBuilder.build();
                })

                .headers(headers -> {

                    boolean hasQueryType = parameters.stream()
                                    .anyMatch(param -> "query".equalsIgnoreCase(param.getType()));

                    if (!hasQueryType) {
                        parameters.stream()
                                .filter(param -> "apiKey".equalsIgnoreCase(param.getType()))
                                .findFirst()
                                .ifPresent(apiKeyParam -> headers.add(apiKeyParam.getKey(), String.valueOf(encodedServiceKey)));
                    }

                    parameters.stream()
                            .filter(param -> "header".equalsIgnoreCase(param.getType()))
                            .forEach(param -> headers.add(param.getKey(), param.getValue()));
                })
                .exchangeToMono(response -> {
//                    Long responseTime = Duration.between(startTime, Instant.now()).toMillis();

                    if (response.statusCode().is2xxSuccessful()) {

                        if (responseTime >= 500) {
                            apiResponse.setResponseTimeOut("응답시간지연");
                        } else apiResponse.setResponseTimeOut("정상");

                        apiResponse.setResponseTime(responseTime);

                        return response.bodyToMono(String.class)
                                .map(body -> {
                                    apiResponse.setBody(body.length() > 255 ? body.substring(0, 255) : body);
                                    apiResponse.setStatusCode(response.statusCode().value());
                                    apiResponse.setResponseTimeOut("정상");
                                    apiResponse.set_success(true);
                                    return apiResponseRepository.save(apiResponse);
                                });


                    }
                    return Mono.error(new ApiEndPointException(ErrorCode.UNKNOWN_ERROR));

                    })
                                .timeout(Duration.ofSeconds(3))
                                .retryWhen(
                                        Retry.backoff(3, Duration.ofSeconds(3))
                                                .filter(throwable ->
                                                        throwable instanceof WebClientResponseException &&
                                                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                                                .doBeforeRetry(retrySignal -> log.warn(
                                                        "재시도 : {}회차, 에러 메시지 : {}", retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()))
                                                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
                                                    log.error("최종 실패 : 모든 재시도가 실패했습니다. 에러 메시지 : {}", retrySignal.failure().getMessage());
                                                    apiResponse.setErrorMessage("최종실패");

                                                    return new ApiEndPointException(ErrorCode.RETRY_EXHAUSTED);
                                                }))
                                )

                                .onErrorResume(WebClientResponseException.class, error -> {

                                        apiResponse.setStatusCode(error.getStatusCode().value());
                                        apiResponse.setResponseTime(responseTime);
                                        apiResponse.setErrorMessage(error.getMessage());
                                        apiResponse.set_success(false);

                                        if (error.getStatusCode().is4xxClientError()) {
                                            log.error("4xx 클라이언트 에러 발생: 상태코드={}", error.getStatusCode());
                                        } else if (error.getStatusCode().is5xxServerError()) {
                                            log.error("5xx 서버 에러 발생: 상태코드={}", error.getStatusCode());
                                        }

                                    return Mono.just(apiResponseRepository.save(apiResponse));
                                })

                                .onErrorResume(TimeoutException.class, timeout -> {
                                    apiResponse.setStatusCode(408);
                                    apiResponse.setResponseTime(responseTime);
                                    apiResponse.setErrorMessage("타임아웃");
                                    log.error("타임아웃 발생 : {}", timeout.getMessage());

                                    return Mono.just(apiResponseRepository.save(apiResponse));
                                })

                                .onErrorResume(throwable -> {
//                                    long responseTime = Duration.between(startTime, Instant.now()).toMillis();
                                    apiResponse.setStatusCode(500);
                                    apiResponse.setResponseTime(responseTime);
                                    apiResponse.set_success(false);
                                    log.error("알 수 없는 에러 발생 : {}", throwable.getMessage());

                                    return Mono.just(apiResponseRepository.save(apiResponse));
                                });
                    }





    public ApiEndpoint createApi(CreateApiEndpointDTO.Request request) {

        return apiEndpointRepository.save(ApiEndpoint.builder()
                .url(request.getUrl())
                        .parameters(request.getParameters())
                .build());
    }

    public String setServiceKey(CreateApiEndpointDTO.Request request) {

        List<ApiEndpoint.Parameter> newParameters = request.getParameters();

        List<ApiEndpoint> apiEndpoints = apiEndpointRepository.findAll();

        apiEndpoints.forEach(apiEndpoint -> {
            apiEndpoint.setParameters(newParameters);
        });

        apiEndpointRepository.saveAll(apiEndpoints);

        return "ok";
    }
}
