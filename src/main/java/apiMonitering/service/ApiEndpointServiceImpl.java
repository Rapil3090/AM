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
            Long id = apiEndpoint.getId();

            getApi(id)
                    .subscribe(
                            response -> System.out.println("응답: " + response),
                            error -> System.out.println("에러: " + error.getMessage())
                    );
        });
    }


    public Mono<ApiResponse> getApi(Long id) {

        ApiEndpoint apiEndpoint =apiEndpointRepository.findById(id)
                .orElseThrow(() -> new ApiEndPointException(ErrorCode.ENDPOINT_ID_NOT_FOUND));

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiEndpoint.getUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        Map<String, String> queryParams = apiEndpoint.getQueryParameters();

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
                    Long responseTime = Duration.between(startTime, Instant.now()).toMillis();

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
                        .queryParameters(query)
                .build());
    }
}
