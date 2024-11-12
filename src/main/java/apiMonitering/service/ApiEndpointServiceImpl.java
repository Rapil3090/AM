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
                    queryParams.forEach(uriBuilder::queryParam);  // 쿼리 파라미터 추가
                    uriBuilder.queryParam("serviceKey", encodedServiceKey);  // 추가적인 파라미터도 가능
                    return uriBuilder.build();
                })

                .exchangeToMono(response -> {
                    long responseTime = Duration.between(startTime, Instant.now()).toMillis();
                    apiResponse.setResponseTime((int) responseTime);

                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> {
                                    // 응답 바디 처리 및 저장
                                    apiResponse.setBody(body.length() > 255 ? body.substring(0, 255) : body);
                                })
                                .then(Mono.just(apiResponse)); // Mono<ApiResponse> 반환
                    } else if (response.statusCode().is4xxClientError()) {
                        // 클라이언트 오류 처리
                        apiResponse.setStatusCode(response.statusCode().value());
                        return Mono.just(apiResponse); // 오류 발생 시에도 ApiResponse 반환
                    } else if (response.statusCode().is5xxServerError()) {
                        // 서버 오류 처리
                        apiResponse.setStatusCode(response.statusCode().value());
                        return Mono.just(apiResponse); // 오류 발생 시에도 ApiResponse 반환
                    } else {
                        // 알 수 없는 오류 처리
                        apiResponse.setStatusCode(response.statusCode().value());
                        return Mono.just(apiResponse); // 오류 발생 시에도 ApiResponse 반환
                    }
                })
                .doOnTerminate(() -> {
                    System.out.println("응답 완료");
                })
                .doOnError(e -> {
                    System.out.println("에러 발생: " + e.getMessage());
                });





//                .uri(uriBuilder -> {
//                    queryParams.forEach(uriBuilder::queryParam);
//                    uriBuilder.queryParam("serviceKey", encodedServiceKey);
//                    return uriBuilder.build();
//                })
//                .exchangeToMono(response -> {
//                    long responseTime = Duration.between(startTime, Instant.now()).toMillis();
//
//                    apiResponse.setStatusCode(response.statusCode().value());
//                    apiResponse.setResponseTime((int) responseTime);
//
//                    return response.bodyToMono(String.class)
//                            .doOnNext(body -> {
//
//                                String truncatedBody = body.length() > 255 ? body.substring(0, 255) : body;
//                                apiResponse.setBody(truncatedBody);
//                            })
//                            .flatMap(responseBody -> Mono.just(apiResponseRepository.save(apiResponse)));
//                })
////                .onErrorResume(e -> e instanceof WebClientResponseException, e -> {
////                    WebClientResponseException error = (WebClientResponseException) e;
////
////                    long responseTime = Duration.between(startTime, Instant.now()).toMillis();
////
////                    apiResponse.setStatusCode(error.getStatusCode().value());
////
////                    return Mono.fromCallable(() -> apiResponseRepository.save(apiResponse))
////                            .subscribeOn(Schedulers.boundedElastic());
////                });
//
//                .onErrorResume(WebClientResponseException.class, error -> {
//                    long responseTime = Duration.between(startTime, Instant.now()).toMillis();
//                    apiResponse.setStatusCode(error.getStatusCode().value());
//                    apiResponse.setResponseTime((int) responseTime);
//
//                    return Mono.just(apiResponseRepository.save(apiResponse));
//                });
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
