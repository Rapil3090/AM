package apiMonitering.service;

import apiMonitering.domain.ApiResponse;
import apiMonitering.dto.create.CreateApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.exception.ApiEndPointException;
import apiMonitering.repository.ApiEndpointRepository;
import apiMonitering.repository.ApiResponseRepository;
import apiMonitering.type.ErrorCode;
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


    @Scheduled(fixedRate = 10000)
    public void scheduledApiCall() {

        Long id = 7L;
        System.out.println("출력 완료");
        getApi(id)
                .subscribe(response -> System.out.println("응답" + response),
                        error -> System.out.println("에러" + error.getMessage()));
    }


    public Mono<ApiResponse> getApi(Long id) {

        ApiEndpoint apiEndpoint =apiEndpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("에러발생"));

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiEndpoint.getUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        Map<String, String> queryParams = apiEndpoint.getQuery();

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(apiEndpoint.getUrl())
                .build();

        URI encodedServiceKey;

        try {
            encodedServiceKey = new URI(apiEndpoint.getServiceKey());
        } catch (URISyntaxException e) {
            throw new ApiEndPointException(ErrorCode.INVALID_SERVICEKEY);
        }

        Instant startTime = Instant.now();

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setApiEndpoint(apiEndpoint);

        return webClient.get()

                .uri(uriBuilder -> {
                    queryParams.forEach(uriBuilder::queryParam);
                    uriBuilder.queryParam("serviceKey", encodedServiceKey);
                    return uriBuilder.build();
                })
                .exchangeToMono(response -> {
                    long responseTime = Duration.between(startTime, Instant.now()).toMillis();

//                    apiResponse.setStatusCode(response.statusCode().value());
                    apiResponse.setResponseTime((int) responseTime);

                    return response.bodyToMono(String.class)
                            .doOnNext(body -> {

                                String truncatedBody = body.length() > 255 ? body.substring(0, 255) : body;
                                apiResponse.setBody(truncatedBody);

                                if (body != null) {

                                    int finalStatusCode = 200;

                                    if (body.contains("INTERNAL_SERVER_ERROR")) {
                                        finalStatusCode = 500;
                                    } else if (body.contains("NO_MANDATORY_REQUEST_PARAMETER_ERROR")) {
                                        finalStatusCode = 400;
                                    } else if (body.contains("SERVICE_UNAVAILABLE")) {
                                        finalStatusCode = 503;
                                    } else if (body.contains("Unauthorized")) {
                                        finalStatusCode = 401;
                                    } else if (body.contains("Forbidden")) {
                                        finalStatusCode = 403;
                                    } else if (body.contains("Not_Found")) {
                                        finalStatusCode = 404;
                                    } else if (body.contains("Method_Not_Allowed")) {
                                        finalStatusCode = 405;
                                    } else if (body.contains("NORMAL_CODE")) {
                                        finalStatusCode = 00;
                                    } else if (body.contains("APPLICATION_ERROR")) {
                                        finalStatusCode = 01;
                                    } else if (body.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR")) {
                                        finalStatusCode = 30;
                                    }

                                    apiResponse.setStatusCode(finalStatusCode);
                                }
                            })
                            .flatMap(responseBody -> Mono.just(apiResponseRepository.save(apiResponse)));
                })

                .onErrorResume(WebClientResponseException.class, error -> {
                    long responseTime = Duration.between(startTime, Instant.now()).toMillis();
                    apiResponse.setStatusCode(error.getStatusCode().value());
                    apiResponse.setResponseTime((int) responseTime);

                    return Mono.just(apiResponseRepository.save(apiResponse));
                });
    }


    public ApiEndpoint createApi(CreateApiEndpointDTO.Request request) {

        Map<String, String> query = new HashMap<>();

        for (List<String> pair : request.getParam()) {
            if (pair.size() == 2) {
                String key = pair.get(0);
                String value = pair.get(1);
                query.put(key, value);
            } else {
                System.out.println("잘못된 형식의 param: " + pair);
            }
        }

        return apiEndpointRepository.save(ApiEndpoint.builder()
                .url(request.getUrl())
                        .serviceKey(request.getServiceKey())
                        .query(query)
                .build());
    }
}
