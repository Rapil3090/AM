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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    Long responseTime = Duration.between(startTime, Instant.now()).toMillis();

                    if (responseTime >= 500) {
                        apiResponse.setResponseTimeOut("응답시간지연");
                    } else apiResponse.setResponseTimeOut("정상");

                    apiResponse.setResponseTime(responseTime);

                    return response.bodyToMono(String.class)
                            .doOnNext(body -> {
                                apiResponse.setBody(body.length() > 255 ? body.substring(0, 255) : body);
                                apiResponse.setStatusCode(response.statusCode().value());
                            })
                            .flatMap(responseBody -> Mono.just(apiResponseRepository.save(apiResponse)));
                })

                .onErrorResume(WebClientResponseException.class, error -> {
                    long responseTime = Duration.between(startTime, Instant.now()).toMillis();
                    apiResponse.setStatusCode(error.getStatusCode().value());
                    apiResponse.setResponseTime(responseTime);
                    apiResponse.set_success(false);

                    return Mono.just(apiResponseRepository.save(apiResponse));
                });
    }


    public ApiEndpoint createApi(CreateApiEndpointDTO.Request request) {

        return apiEndpointRepository.save(ApiEndpoint.builder()
                .url(request.getUrl())
                        .parameters(request.getParameters())
                .build());
    }
}
